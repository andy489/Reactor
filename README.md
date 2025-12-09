# Slot Game API Documentation
## Overview
### API Endpoints

Base URL: `/slot`

All endpoints return JSON and use HTTP GET for simplicity (demo/simulation purposes).

| Method | Endpoint                  | Description                                      | Parameters                                                                 | Response Type                  |
|-------|---------------------------|--------------------------------------------------|----------------------------------------------------------------------------|--------------------------------|
| GET   | `/settings`               | Get game settings (reels, symbols, paytable, RTP)| None                                                                       | `SettingsDto`                  |
| GET   | `/spin`                   | Perform one or more spins with optional state persistence | `stake` (required)<br>`states` (optional)                                  | `List<SlotGameDto>`            |
| GET   | `/gamble`                 | Gamble the last win (if available)               | `choice` (required: 1=Collect, 2=Gamble)                                   | `List<SlotGameDto>`            |
| GET   | `/stats`                  | Run long-term simulation and return statistics   | `spins` (default: 10000, 1–3,000,000)<br>`stake` (required)                | `SlotStatsDto`                 |
| GET   | `/spin/sequence`          | Replay or force a predefined reel outcome sequence | `stake` (required)<br>`sequence` (required)<br>`states` (optional, string) | `PredefinedSequenceResponse`   |

### 1. Get Game Settings
```http
GET /slot/settings
```

Returns reel layout, symbol definitions, paylines, RTP, volatility, etc.

**Example Response:**

```json
{
  "gameName": "Relax Gaming Cluster Reactor",
  "slotId": 1,
  "version": 95,
  "sharpRtp": 95.5,
  "stateNum": 1,
  "hasChoice": true,
  "gridDim": [8, 8],
  "tileIds": {"high": [1, 2, 3, 4], "low": [5, 6, 7, 8], "wild": [9], "blocker": [10]},
  "tileNames": {"1": "H1", "2": "H2", "3": "H3", "4": "H4", "5": "L1", "6": "L2", "7": "L3", "8": "L4", "9": "WILD", "10": "BLOCKER"},
  "payTableType": "INTERVAL_BASED",
  "payTable": {
    "1": {"5": 0.5, "9": 0.6, "13": 0.7, "17": 0.8, "21": 1.0},
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
  "minStake": 0.1,
  "minMatch": 5,
  "wildMultipliers": {
    "9": 0
  },
  "wildMultipliersAggregations": {
    "9": "NONE"
  },
  "reelSets": [
    {
      "setName": "0:MAIN SPIN:BLOCKERS INCLUDED:STACKS SIZES[1,2,3,4,5,6,7,8]:STACK WEIGHTS[15,20,25,20,15,2,2,1]: MIN DISTANCE BETWEEN 2 STACKS WITH SAME TILE IDS[1]",
      "reelSet": [
        [9,9,9,9,1,1,10,10,10,1,1,1,9,9,9,1,1,2,2,2,2,1,1,1,1,8,8,1,1,1], 
        [2,2,2,1,1,5,5,1,2,2,2,2,2,2,2,1,1,5,5,1,10,1,1,5,5,5,1,1,4,4,4], 
        [6,6,6,6,6,6,6,6,1,1,3,3,3,1,1,1,7,7,1,6,6,6,6,1,1,1,1,1,4,4,1,1], 
        [8,2,8,8,9,10,6,6,1,1,9,9,3,3,3,10,10,5,4,4,4,2,2,6,6,6,1,7,7,7],
        [5,5,4,4,9,9,9,9,9,9,4,4,4,4,4,2,2,2,2,4,4,4,3,3,4,9,9,9,9,4,4,1],
        [2,2,2,10,10,2,2,6,6,6,2,2,2,5,2,10,10,10,10,10,2,2,2,6,8,8,1,1],
        [9,5,6,6,1,1,1,1,1,1,1,1,6,6,6,6,6,1,1,5,5,1,1,1,5,5,9,9,9,9,9,9],
        [9,9,1,1,8,8,1,10,1,1,8,8,3,3,3,4,4,4,3,3,3,3,5,5,9,6,6,6,6,6,5]
      ]
    },
    {
      "setName": "0:REACTIONS: NO BLOCKERS:STACKS SIZES[1,2,3,4,5,6,7,8]:STACK WEIGHTS[15,20,25,20,15,2,2,1]: MIN DISTANCE BETWEEN 2 STACKS WITH SAME TILE IDS[1]",
      "reelSet": [
        [1,7,6,6,7,7,4,4,4,1,1,3,3,3,3,3,3,9,9,9,9,2,2,2,6,2,2,8,8,8,7,7],
        [8,8,9,9,9,9,9,6,6,6,9,4,4,9,6,6,9,2,9,4,9,9,7,7,7,7,7,7,7,7,8,4],
        [1,1,8,8,7,7,5,8,8,4,9,9,7,7,9,1,1,1,4,4,1,1,9,9,8,8,9,9,9,2,2,1],
        [1,1,5,6,6,7,7,7,3,1,1,3,3,8,8,8,2,9,9,2,2,2,2,2,2,5,5,6,6,8,8,8],
        [5,3,3,2,2,3,3,2,2,4,4,1,1,1,2,2,6,6,8,8,8,8,7,7,7,3,3,3,5,5,6,6],
        [3,3,7,7,7,2,2,2,7,7,4,4,7,9,7,7,7,2,2,8,8,8,5,5,5,5,5,5,3,1,1,8],
        [1,1,2,2,5,2,3,2,2,2,2,7,7,7,2,9,9,2,2,2,2,2,5,5,5,1,1,4,4,4,4,4],
        [6,6,6,8,8,5,5,5,8,8,8,8,8,6,6,6,8,8,8,5,5,5,5,8,2,2,2,2,2,8,8,5]
      ]
    }
  ]
}
```

### 2. Normal Spin

```http
GET /slot/spin?stake=1.00
```

- `stake`: Must be positive multiple of $0.10, max $100.00
- `states`: Optional list of internal game state IDs to continue from previous session

Returns a list containing the spin result (linearized spins), win, payout, new states, etc.

**Example Response:**

```json

```

### 3. Gamble Last Win

```http
GET /slot/gamble?choice=2
```

Choices:
- `1`: Collect
- `2`: Gamble

Only works if the previous spin had a gamble-able win.

**Example Response:**

```json

```

### 4. Run Simulation / Stats

```http
GET /slot/stats?spins=1000000&stake=1.00
```

```json
{
  "rtp": "502.2114%",
  "maxStakeMultiplier": 38.3,
  "hitRate": 0.9987,
  "winRate": 0.9593,
  "stake": 1,
  "median": 4.5,
  "variance": 9.1786,
  "standardDeviation": 3.0296,
  "randomVariable": "STAKE_MULTIPLIER",
  "timeElapsed": "10 min 37 sec",
  "totalSpinsCount": "3,000,000"
}
```

Runs the requested number of spins in fast simulation mode.
Returns detailed statistics:

- Sharp return to Player Coefficient (RTP)
- Max Stake Multiplier Awarded
- Hit Rate
- Win Rate
- Stake
- Median
- Variance
- Standard Deviation
- and more...

All metrics measure the multiplier by which the bet is multiplied. That is, the random variable we are considering is the slot machine that returns the multiplier by which the bet is multiplied.

### 5. Spin with Predefined Sequence (Debug / Demo Mode)

```http
GET /slot/spin/sequence?stake=2.00&sequence=0,2,1,3,1
```

Forces exact reel stops in order.
Sequence format:

- Comma-separated integers (0-based symbol index per reel)
- Example: `1,0,2,3,1` → 5-reel stop at symbols [1,0,2,3,1]

Useful for:

- Reproducing specific wins
- Creating demo videos
- Testing paytable accuracy