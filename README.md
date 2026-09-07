# Cluster Reactor — Slot Game API

A Spring Boot implementation of **Relax Gaming Cluster Reactor** slot mechanics on an 8×8 grid.  
The engine supports three avalanche modes (REGENERATE / CASCADE / REROLL), cluster-pays win detection via DFS, a gamble feature, and a built-in Monte Carlo simulator.

📄 [Relax Gaming Avalanche Mechanics spec](./assets/Relax_Gaming_Avalanche.pdf)

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java | 17+ |
| Gradle | via wrapper (`./gradlew`) — no install needed |

---

## Running the Application

**Development (hot reload):**
```bash
./gradlew bootRun
```

**Production JAR:**
```bash
./gradlew build
java -jar build/libs/reactor-0.0.1-SNAPSHOT.jar
```

**Swagger UI** is available at [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html) once the server is running.

---

## Dependencies

| Library | Purpose |
|---------|---------|
| Spring Boot Web MVC | REST API |
| Spring Boot Validation | `@Validated` + custom constraint annotations |
| Spring Boot HATEOAS | `_links` in responses |
| SpringDoc OpenAPI (Swagger UI) | Interactive API docs |
| Apache Commons RNG | Mersenne Twister random number generation |
| Lombok | Boilerplate reduction |
| Jackson | JSON serialisation / polymorphic DTOs |

---

## API Endpoints

Base URL: `/slot`  
All endpoints use HTTP GET and return JSON.

| Method | Path | Description | Key Parameters |
|--------|------|-------------|----------------|
| GET | `/slot/settings` | Full game config (grid, symbols, pay table, reel strips) | — |
| GET | `/slot/spin` | Perform a spin | `stake` (required), `states` (optional) |
| GET | `/slot/gamble` | Resolve a pending gamble on the last win | `choice`: `1`=collect, `2`=double-or-nothing |
| GET | `/slot/stats` | Monte Carlo simulation | `spins` (1–3,000,000), `stake` (required) |
| GET | `/slot/spin/sequence` | Spin with a deterministic RNG sequence (debug/QA) | `stake`, `sequence`, `states` (optional) |

### Stake validation (applies to all spin endpoints)

- Must be positive
- Multiple of `$0.10`
- Maximum `$100.00`

---

## Game Mechanics

### Grid & Symbols

An **8×8 grid** (8 reels × 8 rows). Symbols are referenced by integer ID:

| ID | Name | Tier |
|----|------|------|
| 1 | H1 | High |
| 2 | H2 | High |
| 3 | H3 | High |
| 4 | H4 | High |
| 5 | L1 | Low |
| 6 | L2 | Low |
| 7 | L3 | Low |
| 8 | L4 | Low |
| 9 | WILD | Wild |
| 10 | BLOCKER | Blocker |

### Cluster Pays

A **cluster** is a group of 5 or more adjacent identical symbols (4-directional adjacency). WILD (9) substitutes into any cluster but has no own pay entry.

Pay table keys are **minimum cluster sizes** — the engine uses `floorEntry(clusterSize)` to find the multiplier:

| Cluster Size | H1 | H2/H3 | H4 | L1–L4 |
|---|---|---|---|---|
| 5 | 0.5× | 0.4× | 0.3× | 0.1× |
| 9 | 0.6× | 0.5× | 0.4× | 0.2× |
| 13 | 0.7× | 0.6× | 0.5× | 0.3× |
| 17 | 0.8× | 0.7× | 0.6× | 0.4× |
| 21 | 1.0× | 0.9× | 0.7× | 0.5× |

Payout = multiplier × stake.

### Avalanche / Cascade Modes

These three modes are the **core mechanical differentiator** of the engine. After every winning spin, matched cluster tiles explode. The vacated positions must be refilled to form a new grid — the mode defines exactly how.

The reaction loop is recursive: after refill, the new grid is evaluated again for clusters. If another win is found, tiles explode again and the mode's refill logic runs again. This continues until no winning cluster can be formed on the resulting grid. Every reaction round produces its own `SlotGameDto` node in the response.

| Mode | RTP (sharp) |
|------|-------------|
| `REGENERATE` | 55.89% |
| `CASCADE` | 94.06% |
| `REROLL` | 97.46% |

---

#### REGENERATE

> Exploded positions are refilled one tile at a time by independent weighted random draws.

**Initial spin:** The reel strips are not used. `reelSetIndex = -1`. Every cell on the 8×8 grid is populated by calling `rng.getWeightedIndex(tileIds, tileWeights[0])` — one RNG call per cell, 64 calls total for a fresh grid.

**After a win (recursion > 0):**
1. P01 runs **before** P02. It overlays the surviving (non-exploded) tiles from `SlotGameStickyData` onto the freshly initialised empty grid.
2. P02 then fills only the cells whose tile ID is still `< 0` (the exploded positions). Each such cell gets an independent weighted RNG draw using `tileWeights[recursionLevel]` — so the weight distribution can change with each cascade depth.
3. The result is a new grid where surviving tiles are exactly where they were and exploded positions have brand-new random tiles.

**Key properties:**
- Purely random refill — each exploded cell is independent, no reel strip involved.
- Surviving tiles never move — they stay in the same grid position.
- Weight table is keyed by recursion level, allowing deeper cascades to have different symbol frequencies.
- Blockers (tile ID 10) can appear on the initial spin; the reaction reel set (used by REROLL/CASCADE) excludes them.
- Lower RTP (55.89%) because surviving tiles don't slide — the grid can fragment, making it harder to form new large clusters.

---

#### CASCADE

> Surviving tiles gravity-fall downward; reel strips advance to fill vacated top positions.

**Initial spin:** `reelSetIndex` is chosen by weighted RNG from `reelSetIndexes[0]`. Stop positions for all 8 reels are generated: `reelStopPositions[i] = rng.getUniformIndex(0, reelLength)`. P02 reads each reel's strip starting at the stop position and fills the grid column-by-column (reel = column, row 0 = top).

**After a win (recursion > 0):**
1. P02 fills the entire grid from the reel strip at a **new stop position per reel**: for each reel that had at least one exploding tile, the stop position is decremented by 1 (wrapping). This simulates the reel strip advancing upward by one position — the "new" tiles fall in from the top.
2. P03 runs **after** P02. It overlays the surviving tiles from `SlotGameStickyData` onto the grid, overwriting what P02 placed in those positions. This restores all tiles that did not explode to their exact previous positions.
3. The net effect: surviving tiles are back in their slots; the exploded positions now hold whatever was one position higher on the reel strip.

**Key properties:**
- Tile movement is physically coherent — surviving tiles don't move, new tiles come from above on the reel strip.
- Only reels with at least one explosion get their stop position decremented; untouched reels keep the same slice.
- Uses the same reel set (index 0) for all cascade rounds — the main spin reel set is threaded through all recursion levels.
- Higher RTP (94.06%) because reel strips are designed with higher symbol density and no blockers in reaction rounds.

---

#### REROLL

> Every cascade is a full independent re-spin of all 8 reels using a dedicated reaction reel set.

**Initial spin:** Identical to CASCADE — weighted reel-set selection, random stop positions, grid populated from reel strips.

**After a win (recursion > 0):**
1. A **new reel-set index** is chosen by weighted RNG from `reelSetIndexes[recursionLevel]` (using `floorEntry` so the same table entry applies to all deeper levels if not explicitly overridden).
2. New random stop positions are generated for all 8 reels against the selected reaction reel set (index 1 — no blockers).
3. P02 populates the full 8×8 grid from scratch using these new stop positions.
4. P03 runs **after** P02 and overlays the surviving tiles from `SlotGameStickyData`, restoring all non-exploded tiles to their exact positions.
5. The remaining cells (exploded positions) are whatever the new reel spin placed there.

**Key properties:**
- Each cascade is a completely fresh spin — the reaction grid is drawn from the reaction reel set (no blockers) with independent random stops.
- Unlike CASCADE, the stop positions are not derived from the previous spin — there is no "advancing" of a strip. It is a full re-roll.
- Surviving tiles are still preserved and overlaid (same P03 mechanism as CASCADE).
- The reaction reel set is configured separately from the main spin reel set, allowing tuned win frequency for cascade rounds.
- Highest RTP (97.46%) because every cascade round is a full re-spin against a high-density, blocker-free reel set.

### Gamble Feature

After any winning spin, `gambleMultiplier = -1` signals a pending gamble decision.  
`_links` in the response provide the two choices:

- `GET /slot/gamble?choice=1` → **Collect**: receive the win as-is
- `GET /slot/gamble?choice=2` → **Double or nothing**: 50/50 RNG — win doubles (`gambleMultiplier=2`) or is lost (`gambleMultiplier=0`)

Attempting a new spin while a gamble is pending returns **HTTP 409 Conflict**.

---

## Endpoint Reference

### 1. Get Settings

```http
GET /slot/settings
```

Returns the full game configuration including grid dimensions, symbol definitions, pay table, reel strips, and configured avalanche mode.

```json
{
  "gameName": "Relax Gaming Cluster Reactor",
  "slotId": 1,
  "version": 96,
  "sharpRtp": {
    "REGENERATE": 55.8916,
    "REROLL": 97.461,
    "CASCADE": 94.062
  },
  "stateNum": 1,
  "hasChoice": true,
  "gridDim": [8, 8],
  "tileIds": {"high": [1, 2, 3, 4], "low": [5, 6, 7, 8], "wild": [9], "blocker": [10]},
  "tileNames": {"1": "H1", "2": "H2", "3": "H3", "4": "H4", "5": "L1", "6": "L2", "7": "L3", "8": "L4", "9": "WILD", "10": "BLOCKER"},
  "payTableType": "INTERVAL_BASED",
  "payTable": {
    "1": {"5": 0.5, "9": 0.6, "13": 0.7, "17": 0.8, "21": 1},
    "2": {"5": 0.4, "9": 0.5, "13": 0.6, "17": 0.7, "21": 0.9},
    "3": {"5": 0.4, "9": 0.5, "13": 0.6, "17": 0.7, "21": 0.9},
    "4": {"5": 0.3, "9": 0.4, "13": 0.5, "17": 0.6, "21": 0.7},
    "5": {"5": 0.1, "9": 0.2, "13": 0.3, "17": 0.4, "21": 0.5},
    "6": {"5": 0.1, "9": 0.2, "13": 0.3, "17": 0.4, "21": 0.5},
    "7": {"5": 0.1, "9": 0.2, "13": 0.3, "17": 0.4, "21": 0.5},
    "8": {"5": 0.1, "9": 0.2, "13": 0.3, "17": 0.4, "21": 0.5},
    "10": {"5": 0}
  },
  "strategy": "CLUSTERS_PAYS",
  "avalancheMode": "REGENERATE",
  "minStake": 0.1,
  "minMatch": 5,
  "wildMultipliers": {"9": 0},
  "wildMultipliersAggregations": {"9": "NONE"},
  "reelSets": [
    {
      "setName": "0:MAIN SPIN:BLOCKERS INCLUDED:STACKS SIZES[1,2,3]:STACK WEIGHTS[181,18,1]:MIN DISTANCE BETWEEN 2 STACKS WITH SAME TILE IDS[0]",
      "reelSet": [
        [8, 6, 10, 9, 6, 6, 6, 6, 6, 1, 4, 10, 10, 4, 5, 6, 9, 9, 10, 10, 3, 9, 2, 2, 3, 10, 4, 6, 9, 2, 4, 3, 2, 6, 1, 1, 10, 5, 1, 10, 10, 6, 9, 7, 4, 8, 10, 5, 3, 9, 3, 6, 7, 1, 1, 7, 7, 3, 4, 1, 1, 8, 5, 2, 2, 8, 7, 2, 2, 8, 4, 5, 9, 5, 5, 8, 8, 6, 9, 2, 5, 10, 6, 6, 1, 1, 4, 3, 2, 7, 7, 8, 10, 10, 3, 5, 1, 7, 7, 4, 1, 5, 5, 1, 7, 1, 8, 10, 1, 4, 5, 5, 8, 7, 9, 9, 4, 2, 7, 7, 8, 9, 5, 5, 3, 10, 5, 10, 3, 9, 3, 4, 10, 2, 6, 8, 2, 7, 2, 4, 10, 10, 10, 7, 6, 4, 1, 2, 6, 9, 1, 8, 8, 5, 5, 3, 2, 8, 5, 8, 6, 9, 1, 5, 1, 9, 1, 3, 9, 2, 2, 2, 3, 3, 3, 3, 7, 4, 7, 6, 4, 4, 4, 3, 4, 3, 8, 2, 4, 6, 8, 9, 8, 8, 9, 9, 7, 3, 7, 7],
        [7, 6, 8, 3, 3, 8, 7, 3, 9, 9, 9, 9, 4, 4, 7, 7, 10, 3, 2, 2, 5, 6, 5, 4, 2, 9, 4, 8, 3, 3, 2, 1, 9, 1, 10, 10, 2, 1, 7, 5, 7, 9, 8, 5, 2, 5, 4, 4, 1, 2, 10, 4, 6, 6, 4, 5, 5, 3, 8, 4, 8, 4, 5, 3, 5, 2, 6, 6, 6, 8, 6, 7, 5, 5, 2, 2, 7, 1, 7, 5, 4, 3, 3, 8, 6, 2, 2, 2, 7, 8, 5, 8, 8, 8, 8, 8, 6, 6, 3, 2, 1, 6, 6, 6, 9, 7, 10, 8, 7, 3, 10, 10, 10, 3, 5, 9, 10, 8, 2, 7, 4, 7, 7, 2, 5, 9, 10, 4, 7, 1, 1, 2, 5, 3, 2, 6, 6, 3, 2, 1, 8, 10, 5, 5, 5, 5, 10, 2, 4, 8, 8, 9, 3, 10, 3, 4, 7, 7, 8, 9, 6, 3, 3, 10, 6, 9, 4, 6, 3, 7, 6, 9, 7, 10, 4, 1, 1, 1, 1, 9, 9, 4, 4, 9, 1, 10, 9, 4, 9, 1, 1, 9, 10, 1, 1, 10, 10, 10, 1, 1],
        [3, 3, 4, 4, 1, 1, 8, 5, 3, 3, 5, 6, 4, 3, 10, 10, 6, 4, 1, 5, 6, 6, 9, 5, 7, 3, 4, 5, 1, 8, 7, 9, 6, 4, 6, 10, 1, 5, 6, 8, 5, 3, 3, 5, 5, 4, 4, 4, 9, 10, 9, 2, 6, 7, 10, 8, 6, 6, 8, 7, 8, 10, 4, 4, 9, 10, 7, 7, 7, 3, 6, 6, 6, 10, 9, 10, 4, 7, 7, 4, 3, 4, 4, 9, 10, 3, 1, 2, 8, 1, 10, 6, 8, 8, 8, 2, 6, 5, 7, 10, 10, 4, 4, 4, 10, 3, 6, 3, 5, 5, 7, 9, 9, 3, 3, 6, 6, 5, 3, 4, 9, 6, 2, 4, 8, 7, 8, 9, 10, 2, 2, 2, 3, 10, 9, 5, 7, 10, 8, 1, 2, 6, 9, 8, 5, 1, 7, 9, 2, 7, 5, 7, 2, 7, 7, 5, 9, 9, 9, 8, 9, 10, 2, 3, 3, 5, 5, 10, 10, 5, 8, 1, 8, 8, 7, 1, 2, 7, 1, 1, 1, 1, 9, 9, 1, 8, 3, 2, 8, 2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 2],
        [6, 7, 10, 8, 4, 9, 1, 10, 1, 8, 3, 3, 6, 8, 6, 9, 7, 5, 1, 1, 7, 7, 5, 7, 4, 5, 4, 9, 2, 2, 4, 4, 1, 8, 6, 8, 5, 8, 8, 1, 4, 5, 4, 5, 1, 8, 8, 6, 6, 7, 4, 9, 9, 6, 6, 8, 8, 1, 5, 1, 4, 1, 7, 2, 6, 6, 6, 6, 9, 6, 7, 10, 10, 8, 4, 5, 5, 1, 1, 5, 8, 7, 8, 5, 10, 4, 1, 2, 6, 8, 3, 1, 2, 5, 6, 6, 7, 9, 3, 4, 5, 4, 8, 3, 2, 3, 2, 7, 1, 8, 7, 7, 3, 3, 5, 5, 10, 6, 7, 7, 4, 10, 7, 8, 9, 10, 10, 8, 7, 8, 1, 1, 4, 2, 4, 1, 7, 3, 5, 4, 3, 3, 2, 2, 10, 1, 9, 2, 9, 4, 7, 2, 3, 1, 2, 10, 10, 6, 6, 3, 3, 7, 5, 3, 10, 2, 5, 5, 4, 4, 2, 6, 5, 9, 10, 2, 10, 3, 10, 10, 2, 3, 9, 2, 9, 9, 2, 9, 9, 10, 3, 10, 10, 9, 3, 9, 2, 9, 3, 9],
        [4, 7, 7, 7, 1, 9, 7, 7, 8, 2, 5, 4, 5, 2, 2, 2, 2, 6, 1, 1, 1, 8, 6, 7, 7, 10, 8, 3, 10, 7, 7, 7, 2, 9, 8, 3, 4, 7, 4, 8, 10, 2, 9, 7, 7, 8, 9, 2, 10, 6, 2, 7, 8, 3, 6, 10, 5, 2, 9, 8, 8, 1, 9, 7, 9, 8, 3, 5, 1, 3, 5, 6, 3, 9, 6, 1, 1, 1, 7, 4, 10, 9, 10, 10, 4, 8, 3, 6, 4, 8, 6, 3, 4, 5, 6, 9, 4, 1, 2, 6, 3, 2, 9, 6, 7, 2, 2, 2, 5, 10, 7, 5, 1, 8, 2, 6, 9, 5, 5, 9, 1, 7, 7, 2, 5, 8, 3, 3, 10, 3, 1, 5, 1, 10, 5, 5, 9, 9, 6, 10, 2, 5, 4, 3, 3, 3, 10, 4, 2, 10, 3, 9, 9, 2, 10, 9, 4, 10, 8, 4, 4, 1, 3, 3, 3, 10, 9, 9, 4, 5, 6, 6, 10, 5, 10, 10, 4, 4, 4, 8, 5, 5, 5, 8, 4, 6, 1, 4, 8, 3, 8, 1, 6, 6, 8, 6, 1, 6, 1, 1],
        [8, 7, 10, 4, 1, 3, 3, 9, 3, 5, 5, 3, 8, 7, 7, 1, 10, 3, 9, 9, 10, 7, 9, 2, 7, 1, 6, 6, 7, 7, 10, 4, 1, 1, 9, 4, 10, 10, 9, 3, 3, 3, 4, 7, 4, 2, 4, 2, 10, 1, 10, 7, 1, 7, 6, 6, 1, 7, 6, 9, 1, 1, 8, 2, 2, 8, 9, 6, 5, 5, 2, 5, 5, 4, 3, 6, 1, 3, 3, 6, 5, 10, 10, 7, 10, 9, 4, 4, 9, 9, 10, 8, 4, 3, 10, 10, 10, 8, 9, 1, 1, 8, 9, 9, 2, 2, 6, 10, 9, 9, 3, 3, 9, 3, 7, 7, 1, 2, 2, 1, 1, 8, 8, 2, 2, 4, 4, 6, 9, 8, 3, 9, 3, 4, 4, 7, 7, 5, 1, 8, 8, 8, 9, 8, 2, 4, 6, 2, 6, 7, 5, 2, 7, 3, 7, 6, 2, 10, 10, 6, 3, 10, 2, 3, 6, 10, 2, 8, 2, 4, 4, 6, 5, 1, 1, 1, 4, 8, 2, 6, 7, 6, 5, 6, 6, 5, 5, 4, 5, 4, 8, 5, 8, 5, 8, 8, 5, 5, 5, 5],
        [9, 9, 9, 5, 8, 10, 8, 10, 10, 3, 10, 6, 5, 8, 2, 5, 10, 6, 10, 4, 4, 3, 10, 8, 4, 1, 1, 6, 6, 6, 4, 6, 8, 3, 2, 9, 6, 9, 1, 5, 3, 7, 1, 4, 1, 1, 7, 9, 3, 7, 4, 2, 8, 1, 5, 10, 1, 10, 10, 2, 3, 4, 3, 1, 4, 4, 6, 2, 2, 5, 1, 4, 8, 3, 8, 8, 7, 4, 2, 8, 4, 5, 3, 7, 9, 10, 3, 3, 5, 1, 4, 1, 1, 10, 1, 8, 7, 4, 7, 5, 5, 3, 3, 7, 7, 5, 9, 2, 9, 9, 2, 2, 2, 4, 2, 2, 10, 2, 8, 8, 5, 2, 6, 2, 10, 10, 2, 6, 1, 10, 3, 3, 3, 8, 1, 10, 6, 9, 7, 9, 5, 7, 4, 7, 5, 9, 2, 2, 6, 7, 1, 9, 10, 5, 10, 10, 1, 7, 4, 4, 1, 1, 4, 7, 4, 7, 7, 2, 8, 8, 8, 7, 6, 7, 7, 6, 8, 6, 6, 5, 9, 9, 6, 6, 3, 9, 9, 9, 9, 8, 6, 8, 5, 5, 3, 5, 3, 3, 5, 6],
        [3, 3, 3, 5, 3, 1, 1, 7, 7, 2, 8, 3, 2, 3, 3, 6, 6, 1, 10, 10, 5, 3, 10, 7, 9, 4, 1, 7, 10, 9, 9, 5, 9, 9, 9, 1, 4, 5, 5, 5, 10, 9, 7, 2, 1, 1, 3, 1, 7, 5, 1, 9, 9, 3, 3, 2, 7, 4, 6, 1, 8, 8, 8, 2, 3, 9, 4, 2, 1, 6, 6, 6, 8, 5, 9, 10, 2, 1, 8, 9, 10, 9, 7, 7, 10, 7, 3, 7, 7, 3, 5, 8, 6, 5, 5, 3, 3, 2, 2, 9, 6, 9, 3, 5, 9, 9, 9, 10, 4, 6, 3, 8, 9, 4, 2, 6, 1, 6, 6, 3, 3, 6, 9, 10, 7, 2, 2, 5, 7, 4, 7, 4, 4, 10, 10, 5, 8, 7, 6, 8, 8, 8, 7, 7, 7, 8, 2, 2, 10, 8, 2, 1, 6, 6, 7, 4, 8, 8, 2, 6, 5, 4, 8, 8, 4, 2, 1, 2, 8, 10, 10, 4, 6, 4, 4, 1, 2, 4, 10, 8, 1, 2, 5, 5, 10, 10, 6, 4, 1, 5, 5, 5, 1, 1, 4, 4, 10, 10, 6, 4],
        [5, 1, 7, 7, 7, 5, 10, 10, 10, 10, 9, 5, 9, 9, 1, 7, 8, 10, 2, 1, 1, 6, 6, 5, 5, 5, 10, 1, 8, 9, 8, 8, 8, 4, 3, 9, 9, 9, 1, 10, 1, 3, 2, 8, 2, 3, 6, 6, 2, 3, 9, 6, 1, 5, 1, 5, 5, 3, 3, 3, 8, 10, 3, 2, 9, 7, 7, 1, 6, 7, 2, 6, 8, 6, 9, 3, 3, 3, 8, 8, 8, 5, 3, 3, 5, 9, 1, 1, 6, 2, 3, 4, 3, 7, 7, 7, 4, 1, 10, 1, 10, 6, 9, 10, 8, 2, 6, 4, 4, 2, 8, 8, 2, 6, 5, 6, 3, 1, 1, 1, 2, 1, 8, 8, 7, 8, 3, 3, 6, 5, 10, 10, 3, 1, 5, 5, 4, 6, 10, 3, 7, 10, 8, 2, 1, 7, 10, 6, 6, 6, 9, 10, 4, 4, 6, 7, 4, 10, 2, 7, 9, 9, 9, 9, 5, 5, 8, 2, 8, 7, 2, 2, 10, 2, 7, 6, 5, 4, 2, 4, 5, 2, 2, 10, 4, 4, 7, 9, 4, 4, 5, 9, 9, 7, 7, 4, 4, 4, 4, 4]
      ]
    },
    {
      "setName": "1:REACTIONS: NO BLOCKERS:STACKS SIZES[1,2,3]:STACK WEIGHTS[181,18,1]:MIN DISTANCE BETWEEN 2 STACKS WITH SAME TILE IDS[0]",
      "reelSet": [
        [8, 7, 6, 8, 1, 1, 5, 5, 9, 9, 3, 9, 9, 9, 4, 4, 8, 2, 8, 8, 4, 4, 7, 4, 6, 1, 5, 1, 5, 2, 3, 3, 3, 5, 8, 7, 4, 5, 3, 3, 3, 3, 2, 8, 4, 4, 8, 8, 9, 5, 8, 3, 8, 3, 4, 8, 5, 8, 5, 2, 4, 9, 4, 7, 5, 5, 4, 1, 1, 6, 5, 8, 2, 3, 8, 8, 1, 1, 7, 8, 9, 4, 7, 1, 1, 4, 9, 7, 7, 9, 3, 1, 3, 2, 1, 9, 3, 2, 3, 4, 6, 9, 8, 6, 1, 7, 9, 9, 9, 6, 2, 8, 1, 2, 3, 5, 8, 4, 1, 1, 6, 6, 2, 7, 5, 5, 5, 4, 2, 1, 2, 9, 9, 1, 5, 9, 4, 5, 1, 9, 4, 3, 6, 1, 2, 4, 6, 6, 5, 7, 7, 7, 2, 2, 5, 3, 6, 7, 2, 6, 6, 2, 7, 3, 3, 9, 6, 6, 7, 2, 2, 2, 7, 7, 7, 6, 7, 6, 6, 6],
        [8, 2, 6, 8, 3, 8, 5, 9, 7, 2, 8, 3, 7, 8, 5, 2, 7, 4, 5, 5, 4, 3, 5, 7, 9, 6, 9, 9, 8, 3, 9, 8, 3, 9, 8, 8, 8, 2, 8, 4, 5, 5, 3, 2, 6, 7, 1, 9, 1, 5, 6, 5, 5, 6, 1, 1, 1, 3, 3, 8, 8, 3, 2, 4, 1, 4, 4, 9, 2, 3, 1, 4, 6, 4, 1, 6, 4, 2, 2, 5, 2, 1, 6, 9, 4, 3, 1, 7, 2, 5, 4, 6, 7, 2, 9, 3, 4, 2, 6, 3, 6, 4, 1, 7, 2, 5, 3, 3, 1, 9, 2, 4, 4, 9, 3, 4, 8, 8, 1, 8, 9, 9, 2, 7, 9, 8, 5, 5, 6, 1, 4, 1, 4, 6, 6, 5, 5, 9, 5, 8, 8, 9, 6, 4, 4, 5, 5, 8, 3, 3, 9, 6, 6, 9, 9, 1, 3, 7, 3, 1, 2, 2, 2, 7, 1, 6, 7, 1, 1, 2, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7],
        [3, 3, 7, 2, 9, 4, 1, 9, 8, 9, 5, 7, 2, 7, 2, 6, 2, 6, 2, 2, 6, 3, 3, 4, 5, 8, 6, 6, 1, 2, 5, 4, 1, 5, 5, 7, 5, 7, 9, 2, 4, 2, 2, 1, 3, 9, 7, 5, 8, 9, 6, 6, 5, 8, 6, 4, 7, 6, 6, 6, 8, 7, 7, 7, 1, 3, 7, 5, 6, 1, 6, 8, 9, 6, 8, 7, 4, 6, 4, 1, 4, 2, 2, 2, 6, 6, 4, 2, 1, 1, 8, 2, 9, 3, 3, 7, 8, 9, 1, 2, 7, 7, 3, 5, 5, 6, 8, 8, 4, 4, 1, 8, 4, 2, 5, 2, 7, 7, 5, 7, 9, 9, 9, 4, 4, 5, 5, 8, 7, 2, 9, 4, 5, 7, 6, 1, 6, 8, 8, 4, 5, 1, 1, 2, 5, 8, 3, 4, 4, 4, 8, 1, 1, 5, 1, 5, 1, 1, 3, 4, 3, 3, 3, 8, 9, 3, 1, 9, 8, 3, 8, 3, 9, 9, 9, 3, 3, 9, 3, 9],
        [5, 4, 9, 8, 8, 1, 1, 2, 4, 8, 3, 5, 5, 5, 7, 5, 1, 1, 9, 1, 2, 9, 6, 4, 1, 3, 7, 4, 5, 7, 5, 9, 3, 6, 5, 1, 1, 1, 7, 4, 3, 9, 9, 6, 1, 6, 6, 6, 6, 6, 1, 5, 5, 6, 6, 3, 7, 4, 4, 4, 8, 9, 9, 8, 1, 8, 5, 2, 1, 4, 7, 5, 5, 5, 8, 8, 1, 2, 1, 9, 9, 5, 5, 4, 4, 2, 1, 9, 5, 2, 3, 6, 6, 2, 3, 3, 2, 3, 3, 5, 5, 7, 7, 7, 9, 9, 5, 6, 2, 3, 3, 3, 1, 9, 4, 9, 8, 2, 4, 2, 7, 3, 2, 2, 1, 3, 6, 2, 8, 8, 8, 4, 1, 3, 2, 7, 4, 4, 8, 8, 7, 8, 9, 3, 2, 3, 1, 4, 8, 9, 3, 6, 9, 4, 3, 9, 2, 7, 7, 7, 4, 4, 7, 6, 8, 8, 7, 7, 8, 9, 7, 7, 2, 2, 8, 2, 6, 6, 6, 6],
        [6, 3, 7, 5, 2, 4, 4, 7, 9, 5, 7, 8, 9, 4, 8, 8, 4, 5, 5, 5, 4, 7, 2, 6, 2, 2, 9, 8, 2, 8, 6, 3, 2, 7, 2, 6, 6, 7, 9, 1, 6, 5, 2, 6, 5, 1, 4, 3, 2, 1, 2, 4, 2, 2, 4, 7, 6, 5, 2, 7, 9, 3, 3, 1, 1, 1, 3, 4, 5, 3, 7, 5, 7, 1, 3, 7, 7, 2, 9, 7, 3, 9, 2, 2, 1, 1, 9, 9, 6, 8, 9, 7, 5, 5, 7, 7, 7, 7, 3, 3, 5, 8, 7, 5, 5, 5, 8, 2, 9, 7, 6, 1, 6, 9, 1, 6, 6, 2, 6, 2, 3, 6, 3, 3, 6, 2, 1, 8, 8, 4, 4, 4, 6, 4, 4, 8, 8, 9, 8, 5, 1, 8, 3, 3, 9, 1, 5, 1, 5, 9, 8, 9, 5, 9, 8, 8, 8, 1, 3, 8, 8, 1, 6, 6, 6, 1, 3, 9, 4, 4, 4, 4, 4, 1, 4, 3, 9, 1, 9, 3],
        [9, 9, 7, 7, 7, 6, 9, 4, 8, 5, 1, 9, 3, 3, 5, 8, 8, 6, 7, 9, 6, 3, 5, 2, 1, 6, 3, 9, 8, 1, 8, 6, 1, 7, 6, 1, 6, 1, 4, 4, 5, 6, 6, 2, 8, 8, 3, 6, 6, 6, 1, 1, 8, 9, 8, 8, 7, 7, 4, 3, 3, 3, 2, 5, 4, 4, 8, 2, 7, 7, 7, 4, 2, 3, 8, 6, 7, 5, 2, 3, 9, 8, 5, 4, 4, 4, 6, 6, 3, 3, 7, 9, 9, 5, 5, 7, 5, 9, 3, 6, 9, 9, 2, 4, 1, 7, 7, 9, 9, 9, 3, 9, 7, 6, 9, 9, 2, 2, 3, 2, 4, 6, 3, 4, 1, 9, 2, 4, 2, 5, 1, 1, 1, 6, 4, 2, 6, 4, 4, 5, 3, 3, 4, 4, 4, 5, 8, 8, 8, 3, 2, 1, 1, 2, 5, 1, 7, 5, 5, 5, 5, 2, 8, 1, 1, 5, 7, 7, 2, 1, 7, 1, 2, 2, 3, 2, 8, 8, 8, 5],
        [6, 7, 1, 6, 4, 4, 4, 2, 9, 2, 2, 8, 9, 7, 4, 2, 7, 8, 3, 7, 6, 6, 1, 5, 8, 2, 2, 6, 7, 7, 9, 6, 4, 4, 8, 5, 9, 9, 1, 1, 1, 3, 1, 5, 4, 8, 8, 4, 4, 4, 6, 7, 5, 7, 4, 9, 9, 4, 8, 9, 1, 8, 5, 7, 4, 2, 5, 5, 6, 3, 8, 8, 8, 9, 1, 2, 8, 4, 3, 2, 6, 3, 3, 9, 7, 9, 6, 8, 9, 5, 2, 8, 4, 8, 8, 8, 5, 4, 6, 1, 1, 5, 5, 8, 1, 9, 5, 6, 5, 6, 1, 2, 6, 5, 5, 9, 4, 7, 7, 3, 3, 1, 3, 3, 4, 5, 1, 6, 3, 8, 5, 5, 1, 6, 7, 9, 1, 6, 6, 3, 8, 1, 5, 6, 2, 2, 4, 6, 7, 3, 1, 3, 7, 7, 7, 4, 2, 9, 1, 7, 7, 7, 3, 5, 1, 9, 3, 2, 9, 3, 3, 3, 9, 9, 3, 2, 2, 2, 2, 2],
        [2, 2, 9, 6, 3, 8, 5, 3, 1, 8, 5, 8, 8, 9, 1, 5, 3, 9, 1, 4, 5, 1, 5, 4, 9, 2, 3, 3, 1, 9, 5, 2, 3, 7, 7, 7, 1, 1, 5, 6, 9, 1, 5, 6, 6, 7, 3, 8, 8, 6, 2, 1, 8, 8, 2, 5, 3, 4, 4, 4, 8, 4, 6, 6, 2, 2, 4, 4, 6, 5, 5, 2, 6, 7, 6, 2, 3, 7, 4, 7, 7, 5, 8, 5, 8, 5, 6, 2, 9, 9, 6, 3, 4, 2, 1, 3, 3, 3, 9, 7, 7, 5, 9, 8, 8, 1, 1, 4, 3, 9, 8, 6, 9, 6, 3, 5, 9, 1, 2, 2, 7, 3, 1, 8, 9, 1, 1, 3, 6, 8, 1, 5, 9, 3, 6, 6, 6, 8, 3, 5, 8, 4, 4, 1, 7, 3, 1, 7, 2, 9, 4, 7, 9, 6, 4, 5, 5, 6, 1, 2, 2, 4, 8, 8, 4, 4, 9, 9, 7, 2, 2, 9, 4, 7, 2, 4, 7, 7, 7, 7],
        [7, 1, 1, 8, 7, 9, 8, 8, 3, 9, 9, 4, 4, 3, 3, 6, 8, 8, 7, 4, 5, 6, 2, 8, 2, 2, 4, 4, 8, 9, 3, 3, 1, 5, 3, 3, 4, 5, 3, 7, 2, 1, 7, 5, 6, 4, 5, 8, 3, 9, 3, 4, 5, 5, 5, 5, 5, 9, 9, 6, 6, 3, 1, 7, 3, 7, 3, 2, 5, 9, 9, 3, 8, 1, 4, 7, 5, 2, 2, 7, 5, 4, 2, 2, 8, 9, 7, 7, 1, 4, 2, 2, 7, 5, 7, 4, 2, 4, 5, 6, 3, 2, 9, 6, 8, 5, 5, 2, 2, 3, 6, 8, 7, 9, 9, 8, 8, 7, 6, 2, 9, 2, 4, 8, 3, 9, 1, 5, 4, 9, 2, 4, 6, 7, 3, 9, 5, 8, 5, 9, 8, 8, 7, 4, 4, 2, 2, 9, 1, 9, 6, 4, 4, 7, 8, 6, 1, 1, 1, 1, 7, 6, 7, 6, 3, 1, 6, 3, 6, 1, 6, 6, 1, 6, 6, 1, 8, 1, 1, 1]
      ]
    }
  ]
}
```

---

### 2. Spin

```http
GET /slot/spin?stake=1.00
GET /slot/spin?stake=1.00&states=0
```

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `stake` | Double | Yes | Bet amount — positive, multiple of $0.10, max $100.00 |
| `states` | List\<Integer\> | No | Internal game state IDs (for session continuity) |

**Response:** `List<SlotGameDto>` — linearised list of all spin rounds (main spin first, cascade rounds following).

Key fields per spin node:

| Field | Description |
|-------|-------------|
| `spinNum` / `totalSpins` | Position in the cascade chain |
| `recursionLevel` | `0` = main spin, `1+` = cascade reaction |
| `winAmount` | Win from this round only |
| `accumulatedWinAmount` | Running sum from round 1 to current |
| `cumulativeWinAmount` | Sum from current round to last (set only on root after gamble resolves) |
| `gambleMultiplier` | `-1` pending, `0` lost, `1` collected, `2` doubled |
| `payoutData` | Array of `contact` (cluster wins) and `explode_fall` (animation data) entries |
| `_links` | Present on root spin if gamble is available |

<details>
<summary>Example response (REGENERATE, 1 cascade)</summary>

```json
[
  {
    "type": "slot_game",
    "_links": {"COLLECT": "/slot/gamble?choice=1", "GAMBLE": "/slot/gamble?choice=2"},
    "stake": 1,
    "cumulativeWinAmount": 0,
    "stashedCumulativeWinAmountBeforeGambleChoice": 0.1,
    "accumulatedWinAmount": 0.1,
    "winAmount": 0.1,
    "gambleMultiplier": -1,
    "spinNum": 1,
    "totalSpins": 2,
    "reelsSetIndex": -1,
    "reelStopPositions": [],
    "gridDim": [8, 8],
    "grid": [
      [9,4,9,7,8,8,6,9],
      [6,2,5,8,9,8,2,9],
      [1,1,10,3,6,6,10,5],
      [5,8,8,5,7,8,8,10],
      [5,7,3,2,2,8,3,5],
      [10,2,6,10,5,6,2,8],
      [1,6,6,9,10,8,1,2],
      [7,5,7,10,6,7,9,8]
    ],
    "slotGameStickyData": {"stickyReels":[],"stickyPos":[],"stickyTileIds":[]},
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 0,
    "payoutData": [
      {
        "type": "contact",
        "winAmount": 0.1,
        "floatIds": [0],
        "payoutSymbols": [8],
        "contactSizes": [5],
        "contactStarts": [{"reelInd":0,"rowInd":4}],
        "matchType": "CLUSTERS",
        "contact1DimPositions": [[25,32,33,40,41]],
        "contact1DimSymbols": [[8,8,9,8,8]],
        "contact2DimPositions": [[[4,5],[3,4,5],[],[],[],[],[],[]]],
        "contact2DimSymbols": [[[8,8],[8,9,8],[],[],[],[],[],[]]],
        "localMultipliers": [1],
        "globalMultiplier": 1,
        "payouts": [0.1],
        "multipliedPayouts": [0.1],
        "stashedTotalWinAmount": 0
      },
      {
        "type": "explode_fall",
        "winAmount": 0,
        "explodeReels": [0,0,1,1,1],
        "explodePositions": [4,5,5,4,3],
        "explodeSymbolIds": [8,8,8,9,8],
        "fallReels": [0,0,0,0,1,1,1],
        "fallStarts": [3,2,1,0,2,1,0],
        "fallStops": [5,4,3,2,5,4,3],
        "fallSymbolIds": [7,9,4,9,5,2,6],
        "holdReels": [0,0,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7],
        "holdPositions": [7,6,7,6,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0],
        "holdSymbolIds": [9,6,9,2,5,10,6,6,3,10,1,1,10,8,8,7,5,8,8,5,5,3,8,2,2,3,7,5,8,2,6,5,10,6,2,10,2,1,8,10,9,6,6,1,8,9,7,6,10,7,5,7]
      }
    ]
  },
  {
    "type": "slot_game",
    "cumulativeWinAmount": 0,
    "accumulatedWinAmount": 0.1,
    "winAmount": 0,
    "gambleMultiplier": -1,
    "spinNum": 2,
    "totalSpins": 2,
    "reelsSetIndex": -1,
    "reelStopPositions": [],
    "gridDim": [8, 8],
    "grid": [
      [3,8,9,4,9,7,6,9],
      [9,4,2,6,2,5,2,9],
      [1,1,10,3,6,6,10,5],
      [5,8,8,5,7,8,8,10],
      [5,7,3,2,2,8,3,5],
      [10,2,6,10,5,6,2,8],
      [1,6,6,9,10,8,1,2],
      [7,5,7,10,6,7,9,8]
    ],
    "slotGameStickyData": {
      "stickyReels": [0,0,0,0,0,0,1,1,1,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7],
      "stickyPos": [7,6,5,4,3,2,7,6,5,4,3,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0],
      "stickyTileIds": [9,6,7,9,4,9,9,2,5,2,6,5,10,6,6,3,10,1,1,10,8,8,7,5,8,8,5,5,3,8,2,2,3,7,5,8,2,6,5,10,6,2,10,2,1,8,10,9,6,6,1,8,9,7,6,10,7,5,7]
    },
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 1,
    "payoutData": []
  }
]
```

</details>

---

### 3. Gamble

```http
GET /slot/gamble?choice=1
GET /slot/gamble?choice=2
```

Only available after a winning spin (`gambleMultiplier = -1`).

| Choice | Outcome |
|--------|---------|
| `1` | Collect — win is confirmed, `gambleMultiplier = 1` |
| `2` | Double or nothing — 50/50: `gambleMultiplier = 2` (win × 2) or `0` (win = 0) |

Response structure is the same as `/spin` (linearised spin list), with `_links` pointing to `/slot/spin` and `/slot/settings`.

<details>
<summary>Example response (choice=2, won)</summary>

```json
[
  {
    "type": "slot_game",
    "_links": {"SPIN": "/slot/spin?stake=0.10", "SETTINGS": "/slot/settings"},
    "stake": 1,
    "userChoice": 2,
    "cumulativeWinAmount": 0.2,
    "accumulatedWinAmount": 0.1,
    "winAmount": 0.1,
    "gambleMultiplier": 2,
    "spinNum": 1,
    "totalSpins": 2,
    "reelsSetIndex": -1,
    "reelStopPositions": [],
    "gridDim": [8, 8],
    "grid": [
      [9,4,9,7,8,8,6,9],
      [6,2,5,8,9,8,2,9],
      [1,1,10,3,6,6,10,5],
      [5,8,8,5,7,8,8,10],
      [5,7,3,2,2,8,3,5],
      [10,2,6,10,5,6,2,8],
      [1,6,6,9,10,8,1,2],
      [7,5,7,10,6,7,9,8]
    ],
    "slotGameStickyData": {"stickyReels":[],"stickyPos":[],"stickyTileIds":[]},
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 0,
    "payoutData": [
      {
        "type": "contact",
        "winAmount": 0.1,
        "floatIds": [0],
        "payoutSymbols": [8],
        "contactSizes": [5],
        "contactStarts": [{"reelInd":0,"rowInd":4}],
        "matchType": "CLUSTERS",
        "contact1DimPositions": [[25,32,33,40,41]],
        "contact1DimSymbols": [[8,8,9,8,8]],
        "contact2DimPositions": [[[4,5],[3,4,5],[],[],[],[],[],[]]],
        "contact2DimSymbols": [[[8,8],[8,9,8],[],[],[],[],[],[]]],
        "localMultipliers": [1],
        "globalMultiplier": 1,
        "payouts": [0.1],
        "multipliedPayouts": [0.1],
        "stashedTotalWinAmount": 0
      }
    ]
  }
]
```

</details>

---

### 4. Simulation / Stats

```http
GET /slot/stats?spins=50000&stake=1.00
```

Runs a headless Monte Carlo simulation and returns statistical metrics.  
The random variable tracked is the **stake multiplier** per spin (total win / stake).

**Parameters:**

| Name | Default | Range | Description |
|------|---------|-------|-------------|
| `spins` | 10,000 | 1 – 3,000,000 | Number of spins to simulate |
| `stake` | 0.10 | > 0, multiple of $0.10 | Bet amount per simulated spin |

**Response:**

| Field | Description |
|-------|-------------|
| `rtp` | Mean of the stake multiplier distribution (= Return to Player) |
| `maxStakeMultiplier` | Largest single-spin multiplier observed |
| `hitRate` | Fraction of spins with win > 0 |
| `winRate` | Fraction of spins with win > stake |
| `median` | Median stake multiplier |
| `variance` / `standardDeviation` | Volatility measures |
| `avalancheMode` | Mode used for the simulation |
| `timeElapsed` | Wall-clock time |
| `totalSpinsCount` | Formatted spin count |

<details>
<summary>Example response</summary>

```json
{
  "rtp": "55.8916%",
  "maxStakeMultiplier": 4.8,
  "hitRate": 0.7566,
  "winRate": 0.1745,
  "stake": 1.0,
  "median": 0.4,
  "variance": 0.3676,
  "standardDeviation": 0.6063,
  "avalancheMode": "REGENERATE",
  "randomVariable": "STAKE_MULTIPLIER",
  "timeElapsed": "8.873 seconds",
  "totalSpinsCount": "50,000"
}
```

</details>

---

### 5. Spin with Predefined Sequence

```http
GET /slot/spin/sequence?stake=2.00&sequence=<comma-separated numbers>
```

Injects a deterministic RNG sequence into the spin engine. Once the sequence is exhausted, the engine falls back to the Mersenne Twister. Useful for QA, reproducing specific outcomes, and creating demo recordings.

**Sequence format by mode:**

**REGENERATE** — one integer per cell, column-major (reel 0 top-to-bottom, then reel 1, etc.), representing the **tile ID** to place in each empty cell:

```
GET /slot/spin/sequence?sequence=1,2,3,4,5,6,7,8,2,3,4,5,6,7,8,1,...&stake=2.00
```

**REROLL** — sequence encodes reel-set index selections and stop positions:

```
GET /slot/spin/sequence?sequence=0,1,2,3,4,5,6,7,8,1,11,12,13,14,15,16,17,18&stake=2.00
```

- `0` → select reel set index 0 for the base spin (100% chance pool → index 0)
- `1,2,3,4,5,6,7,8` → stop positions for each of the 8 reels on the base spin
- `1` → select reel set index 1 (reaction reel set, 100% chance pool)
- `11,12,13,14,15,16,17,18` → stop positions for each reel on the cascade re-spin
- Further rounds use random RNG (sequence exhausted)

**Response** (`PredefinedSequenceResponse`):

| Field | Description |
|-------|-------------|
| `spinResults` | Same linearised `List<SlotGameDto>` as `/spin` |
| `totalRNGCallsMade` | Total RNG calls during the spin |
| `predefinedRNGConsumed` | How many values were taken from the sequence |
| `predefinedRNGRemaining` | Values left unused |
| `randomRNGUsed` | Calls that fell back to Mersenne Twister |
| `sequenceCompleted` | `true` if sequence was fully consumed with no random fallback |
| `sequenceExhausted` | `true` if the sequence ran out mid-spin |

---

## Architecture

### Configuration File

The entire game configuration lives in a single JSON file:

```
src/main/resources/config/cluster-reactor-config.json
```

`SlotContextFactory` (`config/SlotContextFactory`) reads this file at startup and deserialises it into a singleton `SlotContext` Spring bean. Changing the config file and restarting the application is all that is needed to change game behaviour — no code changes required for things like adjusting reel strips, pay table values, tile weights, or avalanche mode.

Key top-level fields in the config:

| Field | Type | Description |
|-------|------|-------------|
| `gameName` | String | Display name |
| `slotId` / `version` | Integer | Game identifier and version |
| `sharpRtp` | Map\<AvalancheMode, Double\> | Theoretical RTP per mode |
| `gridDim` | `[reels, rows]` | Grid size — currently `[8, 8]` |
| `tileIds` | Map\<String, List\<Integer\>\> | Symbol ID groups: `high`, `low`, `wild`, `blocker` |
| `tileNames` | Map\<Integer, String\> | Human-readable symbol names |
| `tileWeights` | TreeMap\<Integer, List\<Double\>\> | Weighted draw probabilities per recursion level (REGENERATE mode) |
| `payTable` | Map\<Integer, TreeMap\<Integer, Double\>\> | Symbol ID → cluster-size thresholds → multiplier |
| `payTableType` | `INTERVAL_BASED` | How the pay table is looked up (`floorEntry`) |
| `minMatch` | Integer | Minimum cluster size for a win (5) |
| `strategy` | `CLUSTERS_PAYS` | Win detection strategy |
| `avalancheMode` | `REGENERATE` / `CASCADE` / `REROLL` | Cascade refill behaviour |
| `minStake` | Double | Minimum bet unit ($0.10) |
| `reelSets` | List\<ReelSet\> | Reel strip arrays — index 0 = main spin (blockers included), index 1 = reaction re-spin (no blockers) |
| `reelSetIndexes` | TreeMap\<Integer, List\<Integer\>\> | Reel set candidates per recursion level |
| `reelSetChances` | TreeMap\<Integer, List\<Double\>\> | Weighted selection chances for reel sets |
| `neighbors` | `List<List<Integer>>` | Adjacency offsets for DFS — `[[0,1],[0,-1],[1,0],[-1,0]]` (4-directional) |
| `wildMultipliers` | Map\<Integer, Double\> | Multiplier per wild symbol ID |
| `wildMultipliersAggregations` | Map\<Integer, WildMultipliersAggregationType\> | How wild multipliers combine: `NONE`, `ADDITIVE`, `MULTIPLICATIVE` |

---

### Processor Chain Pattern

The spin engine uses a **Chain of Responsibility** pattern. Every spin (main or cascade) runs the same ordered list of `SlotSpinProcessor` implementations — each one reads from and writes to the shared `SlotGameDto` state object.

```
SlotSpinProcessor (interface)
    └── processSpin(spinData, slotContext, states, reelSetIndex,
                   reelStopPositions, stake, stickyData, recursionLevel)
```

Processors are registered in `MySlotGame.initializeAndArrangeProcessors()`:

```
spinProcessors    → [P01, P02, P03, P04, P05, P06, P07]   (run every spin/cascade)
postSpinProcessors → [P08]                                  (run only by gamble flow)
```

The loop in `SlotContext.createSlotSpin()` iterates `spinProcessors` sequentially — each processor decides internally whether to act based on `recursionLevel` and `avalancheMode`:

```
for (SlotSpinProcessor processor : spinProcessors) {
    processor.processSpin(spinData, this, ...);
}
```

The cascade recursion is **not** an external loop — P05 calls `slotContext.createSlotSpin(...)` directly, embedding the child `SlotGameDto` as an entry inside the parent's `payoutData`. This makes the spin result a **tree**, not a list. `SlotGameDto.linearize()` DFS-traverses the tree into the flat `List<SlotGameDto>` returned by the API.

```
SlotGameDto (root, recursionLevel=0)
  └── payoutData
        ├── SlotContactDto   (cluster win data)
        ├── SlotExplodeFallDto (animation data)
        └── SlotGameDto (cascade, recursionLevel=1)   ← embedded child
              └── payoutData
                    ├── SlotContactDto
                    ├── SlotExplodeFallDto
                    └── SlotGameDto (recursionLevel=2)
                          └── ...
```

`linearize()` removes the embedded `SlotGameDto` children from `payoutData` after flattening, so each node in the response list only contains `contact` and `explode_fall` entries.

**Adding a new processor** requires only:
1. Implementing `SlotSpinProcessor`
2. Registering it in `MySlotGame.initializeAndArrangeProcessors()`

No other code changes are needed — the engine picks it up automatically.

---

## Error Responses

All error responses include a `timestamp`, `status`, and `message`. Additional fields vary by error type.

| HTTP | Scenario | Extra fields |
|------|----------|-------------|
| 400 | Invalid stake, bet amount, or gamble choice | field-level validation errors |
| 400 | Gamble called when none is pending | `_links` → spin |
| 409 | Spin attempted while gamble is pending | `stashedCumulativeWinAmount`, `_links` → collect/gamble |
| 404 | Unknown path | `path` |
