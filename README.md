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

After a winning cluster explodes, the grid reacts. Three modes control how the vacated positions are refilled:

| Mode | Refill mechanism | RTP (sharp) |
|------|-----------------|-------------|
| `REGENERATE` | Empty cells (tile ID < 0) are filled individually by weighted random draw | 55.89% |
| `CASCADE` | Surviving tiles slide down; reel strips advance to fill the top | 94.06% |
| `REROLL` | Full 8-reel re-spin using the reaction reel set (index 1) | 97.46% |

Cascades repeat recursively until no new cluster forms. Each reaction round is a separate `SlotGameDto` node in the response tree, flattened by `linearize()`.

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

<details>
<summary>Example response</summary>

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
  "tileIds": {"high": [1,2,3,4], "low": [5,6,7,8], "wild": [9], "blocker": [10]},
  "tileNames": {"1":"H1","2":"H2","3":"H3","4":"H4","5":"L1","6":"L2","7":"L3","8":"L4","9":"WILD","10":"BLOCKER"},
  "payTableType": "INTERVAL_BASED",
  "payTable": {
    "1": {"5":0.5,"9":0.6,"13":0.7,"17":0.8,"21":1},
    "2": {"5":0.4,"9":0.5,"13":0.6,"17":0.7,"21":0.9},
    "3": {"5":0.4,"9":0.5,"13":0.6,"17":0.7,"21":0.9},
    "4": {"5":0.3,"9":0.4,"13":0.5,"17":0.6,"21":0.7},
    "5": {"5":0.1,"9":0.2,"13":0.3,"17":0.4,"21":0.5},
    "6": {"5":0.1,"9":0.2,"13":0.3,"17":0.4,"21":0.5},
    "7": {"5":0.1,"9":0.2,"13":0.3,"17":0.4,"21":0.5},
    "8": {"5":0.1,"9":0.2,"13":0.3,"17":0.4,"21":0.5}
  },
  "strategy": "CLUSTERS_PAYS",
  "avalancheMode": "REGENERATE",
  "minStake": 0.1,
  "minMatch": 5,
  "wildMultipliers": {"9": 0},
  "wildMultipliersAggregations": {"9": "NONE"}
}
```

</details>

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

## Error Responses

All error responses include a `timestamp`, `status`, and `message`. Additional fields vary by error type.

| HTTP | Scenario | Extra fields |
|------|----------|-------------|
| 400 | Invalid stake, bet amount, or gamble choice | field-level validation errors |
| 400 | Gamble called when none is pending | `_links` → spin |
| 409 | Spin attempted while gamble is pending | `stashedCumulativeWinAmount`, `_links` → collect/gamble |
| 404 | Unknown path | `path` |
