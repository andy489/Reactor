# Slot Game API Documentation
## Overview
### API Endpoints

Interactive API Documentation is Live
Swagger UI: `/swagger-ui/index.html`

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

**Example Response (SETTINGS):**

```json
{
  "gameName": "Relax Gaming Cluster Reactor",
  "slotId": 1,
  "version": 95,
  "sharpRtp": 419.7701,
  "stateNum": 1,
  "hasChoice": true,
  "gridDim": [8,8],
  "tileIds": {
    "high": [1,2,3,4], "low": [5,6,7,8], "wild": [9], "blocker": [10]
  },
  "tileNames": {
    "1": "H1", "2": "H2", "3": "H3", "4": "H4", 
    "5": "L1", "6": "L2", "7": "L3", "8": "L4", 
    "9": "WILD", "10": "BLOCKER"
  },
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
      "setName": "0:MAIN SPIN:BLOCKERS INCLUDED:STACKS SIZES[1,2,3,4,5,6,7,8]:STACK WEIGHTS[26,43,16,7,4,2,1,1]: MIN DISTANCE BETWEEN 2 STACKS WITH SAME TILE IDS[1]",
      "reelSet": [
        [10,10,10,10,3,3,8,8,3,3,4,4,4,4,3,2,3,3,6,6,3,3,9,9,3,3,5,5,3,2,2,2,8,8,10,10,8,8,2,2,2,9,9,4,7,7,4,10,10,8,...],
        [2,2,3,3,2,1,2,8,8,2,2,3,2,2,10,10,2,2,2,1,2,2,2,2,3,3,2,8,2,2,6,6,2,2,7,7,2,10,10,10,2,4,4,4,4,2,2,2,2,2,2,2,...],
        [4,4,5,5,4,4,7,7,4,4,3,3,4,4,4,4,9,9,9,4,3,3,4,6,6,6,4,4,10,10,10,4,5,5,2,2,5,5,4,7,7,8,8,8,8,8,8,6,2,4,9,10,...],
        [8,8,2,3,3,3,3,3,3,3,9,6,6,8,8,8,2,3,3,8,8,6,6,1,1,1,4,4,4,2,2,4,4,3,3,7,7,7,7,7,7,6,6,6,6,6,10,10,1,8,8,8,5,...],
        [6,6,9,9,3,6,6,9,9,8,8,8,5,5,5,5,3,8,8,2,2,2,2,2,2,2,3,3,2,5,5,10,1,1,4,9,9,6,6,3,3,3,3,3,3,8,5,5,5,5,5,6,6,6,...],
        [2,2,10,10,2,2,7,7,2,2,8,2,2,6,2,2,2,2,2,3,3,2,2,1,1,4,4,4,4,4,9,9,10,10,10,10,10,10,6,1,1,9,6,6,6,1,1,8,8,3,...],
        [10,10,10,10,3,6,6,6,6,6,6,6,3,3,10,3,3,3,3,7,7,3,3,2,2,7,10,10,10,2,2,2,2,2,2,8,3,3,3,1,6,10,10,10,10,10,10,...],
        [3,3,3,3,3,5,5,5,2,2,2,2,5,5,3,3,5,2,2,3,1,1,1,10,10,3,3,6,6,6,5,5,5,5,5,5,4,4,4,9,9,6,6,6,6,6,6,6,4,3,2,3,3,...],
      ]
    },
    {
      "setName": "0:REACTIONS: NO BLOCKERS:STACKS SIZES[1,2,3,4,5,6,7,8]:STACK WEIGHTS[26,43,16,7,4,2,1,1]: MIN DISTANCE BETWEEN 2 STACKS WITH SAME TILE IDS[1]",
      "reelSet": [
        [4,4,4,4,9,9,7,7,7,9,9,5,5,5,4,4,7,7,5,5,5,5,5,7,7,7,6,6,4,4,4,2,2,2,2,3,3,8,6,6,6,4,3,3,3,3,3,3,3,3,1,1,1,1,...],
        [3,3,3,3,3,3,4,4,2,4,4,1,4,7,7,7,4,1,1,4,2,6,6,2,2,1,1,4,4,3,3,1,1,7,7,7,7,7,7,7,1,1,8,7,7,6,7,7,7,7,3,1,1,5,...],
        [4,4,4,2,3,3,2,2,2,2,5,5,2,2,1,1,1,1,1,1,2,2,3,3,3,8,8,8,6,6,6,6,8,8,8,7,6,4,4,4,4,2,2,6,6,5,5,3,3,3,3,3,2,2,...],
        [3,3,3,3,7,4,4,4,7,6,5,5,6,6,3,5,7,7,9,9,3,3,3,9,9,3,6,6,2,2,4,4,4,4,1,9,9,9,9,2,6,6,6,6,8,7,7,2,2,2,8,8,8,5,...],
        [6,4,4,5,5,4,4,4,4,9,9,5,5,5,5,1,1,1,1,1,9,9,5,5,5,7,2,2,1,1,6,6,6,9,1,1,6,6,1,2,2,2,9,3,1,1,5,3,3,3,4,4,8,8,...],
        [2,5,5,7,7,5,5,5,2,2,5,5,8,8,5,5,7,7,5,7,5,8,8,8,8,8,8,2,2,2,2,2,2,2,7,7,7,7,7,6,6,7,6,6,6,8,8,8,7,7,9,9,5,5,...],
        [4,4,4,9,9,9,8,3,3,3,8,8,2,2,6,6,8,8,8,8,8,2,2,5,5,5,6,6,3,3,3,9,2,7,7,7,3,3,1,1,2,9,9,9,3,3,3,3,3,3,3,3,9,7,...],
        [2,2,4,4,6,6,2,2,2,2,4,6,6,6,6,6,1,1,1,5,5,3,3,3,3,3,8,8,6,6,4,4,7,6,6,6,6,8,5,5,8,8,8,8,8,5,5,5,5,3,3,6,1,1,...]
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
[
  {
    "type": "slot_game",
    "_links": {
      "COLLECT": "/slot/gamble?choice=1",
      "GAMBLE": "/slot/gamble?choice=2"
    },
    "stake": 10,
    "cumulativeWinAmount": 0,
    "stashedCumulativeWinAmountBeforeGambleChoice": 9,
    "accumulatedWinAmount": 4,
    "winAmount": 4,
    "gambleMultiplier": -1,
    "spinNum": 1,
    "totalSpins": 3,
    "reelsSetIndex": 0,
    "reelStopPositions": [909,246,172,175,109,47,110,930],
    "gridDim": [8,8],
    "grid": [
      [7,7,9,6,6,9,9,9],
      [9,4,10,10,8,8,10,8],
      [3,3,2,1,1,1,2,2],
      [1,1,1,3,3,6,10,5],
      [7,7,7,10,10,8,8,2],
      [8,8,3,3,7,7,8,8],
      [1,6,5,1,5,5,9,9],
      [4,8,8,8,8,8,7,2]
    ],
    "slotGameStickyData": {
      "stickyReels": [],
      "stickyPos": [],
      "stickyTileIds": []
    },
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 0,
    "payoutData": [
      {
        "type": "contact",
        "winAmount": 4,
        "floatIds": [0,1,2,3],
        "payoutSymbols": [6,8,8,8],
        "contactSizes": [6,6,6,5],
        "contactStarts": [
          {"reelInd": 0,"rowInd": 2},
          {"reelInd": 0,"rowInd": 5},
          {"reelInd": 4,"rowInd": 5},
          {"reelInd": 7,"rowInd": 1}
        ],
        "matchType": "CLUSTERS",
        "contact1DimPositions": [
          [16,24,32,40,48,56],
          [33,40,41,48,56,57],
          [44,52,53,54,61,62],
          [15,23,31,39,47]
        ],
        "contact1DimSymbols": [
          [9,6,6,9,9,9],
          [8,9,8,9,9,8],
          [8,8,8,9,8,9],
          [8,8,8,8,8]
        ],
        "contact2DimPositions": [
          [[2,3,4,5,6,7],[],[],[],[],[],[],[]],
          [[5,6,7],[4,5,7],[],[],[],[],[],[]],
          [[],[],[],[],[5,6],[6,7],[6,7],[]],
          [[],[],[],[],[],[],[],[1,2,3,4,5]]
        ],
        "contact2DimSymbols": [
          [[9,6,6,9,9,9],[],[],[],[],[],[],[]],
          [[9,9,9],[8,8,8],[],[],[],[],[],[]],
          [[],[],[],[],[8,8],[8,8],[9,9],[]],
          [[],[],[],[],[],[],[],[8,8,8,8,8]]
        ],
        "localMultipliers": [1,1,1,1],
        "globalMultiplier": 1,
        "payouts": [1,1,1,1],
        "multipliedPayouts": [1,1,1,1],
        "stashedTotalWinAmount": 0
      },
      {
        "type": "explode_fall",
        "winAmount": 0,
        "holdReels": [2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,4,7,7],
        "holdPositions": [7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,7,6],
        "holdSymbolIds": [2,2,1,1,1,2,3,3,5,10,6,3,3,1,1,1,2,2,7],
        "explodeReels": [0,0,0,0,0,0,5,7,4,7,4,7,6,1,7,1,1,6,5,7],
        "explodePositions": [2,3,4,5,6,7,6,4,6,3,5,2,7,7,1,5,4,6,7,5],
        "explodeSymbolIds": [9,6,6,9,9,9,8,8,8,8,8,8,9,8,8,8,8,9,8,8],
        "fallReels": [0,0,1,1,1,1,1,4,4,4,4,4,5,5,5,5,5,5,6,6,6,6,6,6,7],
        "fallStarts": [1,0,6,3,2,1,0,4,3,2,1,0,5,4,3,2,1,0,5,4,3,2,1,0,0],
        "fallStops": [7,6,7,6,5,4,3,6,5,4,3,2,7,6,5,4,3,2,7,6,5,4,3,2,5],
        "fallSymbolIds": [7,7,10,10,10,4,9,10,10,7,7,7,7,7,3,3,8,8,5,5,1,5,6,1,4]
      }
    ]
  },
  {
    "type": "slot_game",
    "cumulativeWinAmount": 5,
    "accumulatedWinAmount": 9,
    "winAmount": 5,
    "gambleMultiplier": -1,
    "spinNum": 2,
    "totalSpins": 3,
    "reelsSetIndex": 1,
    "reelStopPositions": [262,696,310,104,318,809,830,461],
    "gridDim": [8,8],
    "grid": [
      [1,3,3,2,5,4,7,7],
      [5,5,9,9,4,10,10,10],
      [3,3,2,1,1,1,2,2],
      [1,1,1,3,3,6,10,5],
      [6,2,7,7,7,10,10,2],
      [4,4,8,8,3,3,7,7],
      [1,1,1,6,5,1,5,5],
      [1,2,2,7,7,4,7,2]
    ],
    "slotGameStickyData": {
      "stickyReels": [0,0,1,1,1,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,5,5,5,5,5,5,6,6,6,6,6,6,7,7,7],
      "stickyPos": [7,6,7,6,5,4,3,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,7,6,5,4,3,2,7,6,5,4,3,2,7,6,5],
      "stickyTileIds": [7,7,10,10,10,4,9,2,2,1,1,1,2,3,3,5,10,6,3,3,1,1,1,2,10,10,7,7,7,7,7,3,3,8,8,5,5,1,5,6,1,2,7,4]
    },
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 1,
    "payoutData": [
      {
        "type": "contact",
        "winAmount": 5,
        "floatIds": [0],
        "payoutSymbols": [1],
        "contactSizes": [5],
        "contactStarts": [
          {"reelInd": 1,"rowInd": 2}
        ],
        "matchType": "CLUSTERS",
        "contact1DimPositions": [
          [17,25,26,34,42]
        ],
        "contact1DimSymbols": [
          [9,9,1,1,1]
        ],
        "contact2DimPositions": [
          [[],[2,3],[3,4,5],[],[],[],[],[]]
        ],
        "contact2DimSymbols": [
          [[],[9,9],[1,1,1],[],[],[],[],[]]
        ],
        "localMultipliers": [1],
        "globalMultiplier": 1,
        "payouts": [5],
        "multipliedPayouts": [5],
        "stashedTotalWinAmount": 0
      },
      {
        "type": "explode_fall",
        "winAmount": 0,
        "holdReels": [0,0,0,0,0,0,0,0,1,1,1,1,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7],
        "holdPositions": [7,6,5,4,3,2,1,0,7,6,5,4,7,6,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0],
        "holdSymbolIds": [7,7,4,5,2,3,3,1,10,10,10,4,2,2,5,10,6,3,3,1,1,1,2,10,10,7,7,7,2,6,7,7,3,3,8,8,4,4,5,5,1,5,6,1,1,1,2,7,4,7,7,2,2,1],
        "explodeReels": [2,2,2,1,1],
        "explodePositions": [5,4,3,3,2],
        "explodeSymbolIds": [1,1,1,9,9],
        "fallReels": [1,1,2,2,2],
        "fallStarts": [1,0,2,1,0],
        "fallStops": [3,2,5,4,3],
        "fallSymbolIds": [5,5,2,3,3]
      }
    ]
  },
  {
    "type": "slot_game",
    "cumulativeWinAmount": 0,
    "accumulatedWinAmount": 9,
    "winAmount": 0,
    "gambleMultiplier": -1,
    "spinNum": 3,
    "totalSpins": 3,
    "reelsSetIndex": 1,
    "reelStopPositions": [21,616,10,245,814,731,799,296],
    "gridDim": [8,8],
    "grid": [
      [1,3,3,2,5,4,7,7],
      [7,7,5,5,4,10,10,10],
      [5,5,2,3,3,2,2,2],
      [1,1,1,3,3,6,10,5],
      [6,2,7,7,7,10,10,2],
      [4,4,8,8,3,3,7,7],
      [1,1,1,6,5,1,5,5],
      [1,2,2,7,7,4,7,2]
    ],
    "slotGameStickyData": {
      "stickyReels": [0,0,0,0,0,0,0,0,1,1,1,1,1,1,2,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7],
      "stickyPos": [7,6,5,4,3,2,1,0,7,6,5,4,3,2,7,6,5,4,3,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0,7,6,5,4,3,2,1,0],
      "stickyTileIds": [7,7,4,5,2,3,3,1,10,10,10,4,5,5,2,2,2,3,3,5,10,6,3,3,1,1,1,2,10,10,7,7,7,2,6,7,7,3,3,8,8,4,4,5,5,1,5,6,1,1,1,2,7,4,7,7,2,2,1]
    },
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 2,
    "payoutData": []
  }
]
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
  "rtp": "419.7701%",
  "maxStakeMultiplier": 41.2,
  "hitRate": 0.998,
  "winRate": 0.9405,
  "stake": 1,
  "median": 3.7,
  "variance": 6.4704,
  "standardDeviation": 2.5437,
  "randomVariable": "STAKE_MULTIPLIER",
  "timeElapsed": "9 min 4 sec",
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
GET /slot/spin/sequence?sequence=0,1,2,3,4,5,6,7,8,1,11,12,13,14,15,16,17,18&stake=2.00
```

Forces exact reel stops in order.
Sequence format:

- Comma-separated integers (0-based symbol index per reel)
- Example: `0,1,2,3,4,5,6,7,8,1,11,12,13,14,15,16,17,18` → 
  - 0 = choose reels set with index 0 (the only one available currently for base spin (since it is with 100% chance))
  - 1,2,3,4,5,6,7,8 = reel stop positions for base spin
  - 1 = choose reels set with index 1 (the only one available currently for reaction re-spin (since it is with 100% chance)) 
  - 11,12,13,14,15,16,17,18 = reel stop positions for re-spin
  - continue randomly with RNGs, since the predefined sequence is exhausted

Useful for:

- Reproducing specific wins
- Creating demo videos
- Testing paytable accuracy