# Slot Game API Documentation
## Overview

<a href="./assets/Relax_Gaming_Avalanche.pdf" target="_blank">Relax Gaming Avalanche Mechanics</a>

## Prerequisites

- **Java 17** (required by the project configuration)
- **Gradle** (the project uses Gradle Wrapper, so you don't need to install it separately)

## Project Dependencies

This project includes:
- Spring Boot Web MVC
- Spring Boot Validation
- Spring Boot HATEOAS
- SpringDoc OpenAPI (Swagger UI) for API documentation
- Apache Commons RNG for random number generation
- Lombok for reducing boilerplate code
- Jackson for JSON processing

## How to Run

### Option 1: Run directly with Gradle (Development)

```bash
# On Linux/Mac
./gradlew bootRun

# On Windows
gradlew bootRun

### Option 2: Build and run as JAR (Production)
bash
# Build the executable JAR
./gradlew build

# Run the JAR file
java -jar build/libs/reactor-0.0.1-SNAPSHOT.jar

### API Endpoints

Interactive API Documentation is Live
Swagger UI: `/swagger-ui/index.html`

Base URL: `/slot`

All endpoints return JSON and use HTTP GET for simplicity (demo/simulation purposes).

| Method | Endpoint        | Description                                               | Parameters                                                                 | Response Type       |
|-------|------------------|-----------------------------------------------------------|----------------------------------------------------------------------------|---------------------|
| GET   | `/settings`      | Get game settings (reels, symbols, paytable, RTP, etc.)   | None                                                                       | `SettingsDto`       |
| GET   | `/spin`          | Perform one or more spins with optional state persistence | `stake` (required)<br>`states` (optional)                                  | `List<SlotGameDto>` |
| GET   | `/gamble`        | Gamble the last win (if available)                        | `choice` (required: 1=Collect, 2=Gamble)                                   | `List<SlotGameDto>` |
| GET   | `/stats`         | Run long-term simulation and return statistics            | `spins` (default: 10,000 (min=1, max=3,000,000)<br>`stake` (required)      | `SlotStatsDto`|
| GET   | `/spin/sequence` | Replay or force a predefined reel outcome sequence        | `stake` (required)<br>`sequence` (required)<br>`states` (optional, string) | `PredefinedSequenceResponse` |

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
  "wildMultipliers": {
    "9": 0
  },
  "wildMultipliersAggregations": {
    "9": "NONE"
  },
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

### 2. Normal Spin (AvalancheMode: REGENERATE)

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
      [9, 4, 9, 7, 8, 8, 6, 9],
      [6, 2, 5, 8, 9, 8, 2, 9],
      [1, 1, 10, 3, 6, 6, 10, 5],
      [5, 8, 8, 5, 7, 8, 8, 10],
      [5, 7, 3, 2, 2, 8, 3, 5],
      [10, 2, 6, 10, 5, 6, 2, 8],
      [1, 6, 6, 9, 10, 8, 1, 2],
      [7, 5, 7, 10, 6, 7, 9, 8]
    ],
    "slotGameStickyData": {"stickyReels": [], "stickyPos": [], "stickyTileIds": []},
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
        "contactStarts": [{"reelInd": 0, "rowInd": 4}],
        "matchType": "CLUSTERS",
        "contact1DimPositions": [[25, 32, 33, 40, 41]],
        "contact1DimSymbols": [[8, 8, 9, 8, 8]],
        "contact2DimPositions": [[[4, 5], [3, 4, 5], [], [], [], [], [], []]],
        "contact2DimSymbols": [[[8, 8], [8, 9, 8], [], [], [], [], [], []]],
        "localMultipliers": [1],
        "globalMultiplier": 1,
        "payouts": [0.1],
        "multipliedPayouts": [0.1],
        "stashedTotalWinAmount": 0
      },
      {
        "type": "explode_fall",
        "winAmount": 0,
        "holdReels": [0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7],
        "holdPositions": [7, 6, 7, 6, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0],
        "holdSymbolIds": [9, 6, 9, 2, 5, 10, 6, 6, 3, 10, 1, 1, 10, 8, 8, 7, 5, 8, 8, 5, 5, 3, 8, 2, 2, 3, 7, 5, 8, 2, 6, 5, 10, 6, 2, 10, 2, 1, 8, 10, 9, 6, 6, 1, 8, 9, 7, 6, 10, 7, 5, 7],
        "explodeReels": [0, 0, 1, 1, 1],
        "explodePositions": [4, 5, 5, 4, 3],
        "explodeSymbolIds": [8, 8, 8, 9, 8],
        "fallReels": [0, 0, 0, 0, 1, 1, 1],
        "fallStarts": [3, 2, 1, 0, 2, 1, 0],
        "fallStops": [5, 4, 3, 2, 5, 4, 3],
        "fallSymbolIds": [7, 9, 4, 9, 5, 2, 6]
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
      [3, 8, 9, 4, 9, 7, 6, 9],
      [9, 4, 2, 6, 2, 5, 2, 9],
      [1, 1, 10, 3, 6, 6, 10, 5],
      [5, 8, 8, 5, 7, 8, 8, 10],
      [5, 7, 3, 2, 2, 8, 3, 5],
      [10, 2, 6, 10, 5, 6, 2, 8],
      [1, 6, 6, 9, 10, 8, 1, 2],
      [7, 5, 7, 10, 6, 7, 9, 8]
    ],
    "slotGameStickyData": {
      "stickyReels": [0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7],
      "stickyPos": [7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0],
      "stickyTileIds": [9, 6, 7, 9, 4, 9, 9, 2, 5, 2, 6, 5, 10, 6, 6, 3, 10, 1, 1, 10, 8, 8, 7, 5, 8, 8, 5, 5, 3, 8, 2, 2, 3, 7, 5, 8, 2, 6, 5, 10, 6, 2, 10, 2, 1, 8, 10, 9, 6, 6, 1, 8, 9, 7, 6, 10, 7, 5, 7]
    },
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 1,
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
      [9, 4, 9, 7, 8, 8, 6, 9],
      [6, 2, 5, 8, 9, 8, 2, 9],
      [1, 1, 10, 3, 6, 6, 10, 5],
      [5, 8, 8, 5, 7, 8, 8, 10],
      [5, 7, 3, 2, 2, 8, 3, 5],
      [10, 2, 6, 10, 5, 6, 2, 8],
      [1, 6, 6, 9, 10, 8, 1, 2],
      [7, 5, 7, 10, 6, 7, 9, 8]
    ],
    "slotGameStickyData": {"stickyReels": [], "stickyPos": [], "stickyTileIds": []},
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
        "contactStarts": [{"reelInd": 0, "rowInd": 4}],
        "matchType": "CLUSTERS",
        "contact1DimPositions": [[25, 32, 33, 40, 41]],
        "contact1DimSymbols": [[8, 8, 9, 8, 8]],
        "contact2DimPositions": [[[4, 5], [3, 4, 5], [], [], [], [], [], []]],
        "contact2DimSymbols": [[[8, 8], [8, 9, 8], [], [], [], [], [], []]],
        "localMultipliers": [1],
        "globalMultiplier": 1,
        "payouts": [0.1],
        "multipliedPayouts": [0.1],
        "stashedTotalWinAmount": 0
      },
      {
        "type": "explode_fall",
        "winAmount": 0,
        "holdReels": [0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7],
        "holdPositions": [7, 6, 7, 6, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0],
        "holdSymbolIds": [9, 6, 9, 2, 5, 10, 6, 6, 3, 10, 1, 1, 10, 8, 8, 7, 5, 8, 8, 5, 5, 3, 8, 2, 2, 3, 7, 5, 8, 2, 6, 5, 10, 6, 2, 10, 2, 1, 8, 10, 9, 6, 6, 1, 8, 9, 7, 6, 10, 7, 5, 7],
        "explodeReels": [0, 0, 1, 1, 1],
        "explodePositions": [4, 5, 5, 4, 3],
        "explodeSymbolIds": [8, 8, 8, 9, 8],
        "fallReels": [0, 0, 0, 0, 1, 1, 1],
        "fallStarts": [3, 2, 1, 0, 2, 1, 0],
        "fallStops": [5, 4, 3, 2, 5, 4, 3],
        "fallSymbolIds": [7, 9, 4, 9, 5, 2, 6]
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
      [3, 8, 9, 4, 9, 7, 6, 9],
      [9, 4, 2, 6, 2, 5, 2, 9],
      [1, 1, 10, 3, 6, 6, 10, 5],
      [5, 8, 8, 5, 7, 8, 8, 10],
      [5, 7, 3, 2, 2, 8, 3, 5],
      [10, 2, 6, 10, 5, 6, 2, 8],
      [1, 6, 6, 9, 10, 8, 1, 2],
      [7, 5, 7, 10, 6, 7, 9, 8]
    ],
    "slotGameStickyData": {
      "stickyReels": [0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7],
      "stickyPos": [7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0, 7, 6, 5, 4, 3, 2, 1, 0],
      "stickyTileIds": [9, 6, 7, 9, 4, 9, 9, 2, 5, 2, 6, 5, 10, 6, 6, 3, 10, 1, 1, 10, 8, 8, 7, 5, 8, 8, 5, 5, 3, 8, 2, 2, 3, 7, 5, 8, 2, 6, 5, 10, 6, 2, 10, 2, 1, 8, 10, 9, 6, 6, 1, 8, 9, 7, 6, 10, 7, 5, 7]
    },
    "preSpinStates": [0],
    "postSpinStates": [0],
    "stakeMultiplier": 1,
    "recursionLevel": 1,
    "payoutData": []
  }
]
```

### 4. Run Simulation / Stats

```http
GET /slot/stats?spins=50000&stake=1.00
```

```json
{
  "rtp": "55.8916%",
  "maxStakeMultiplier": 4.8,
  "hitRate": 0.7566,
  "winRate": 0.1745,
  "stake": 1,
  "median": 0.4,
  "variance": 0.3676,
  "standardDeviation": 0.6063,
  "avalancheMode": "REGENERATE",
  "randomVariable": "STAKE_MULTIPLIER",
  "timeElapsed": "8.873 seconds",
  "totalSpinsCount": "50,000"
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
GET /slot/spin/sequence?sequence=1,2,3,4,5,6,7,8,2,3,4,5,6,7,8,1,3,4,5,6,7,8,1,2,4,5,6,7,8,1,2,3,5,6,7,8,1,2,3,4,6,7,8,1,2,3,4,5,7,8,1,2,3,4,5,6,8,1,2,3,4,5,6,7&stake=2.00
```

Forces exact reel stops in order.
Sequence format:

- Comma-separated integers (0-based symbol index per reel)
- Example **[REGENERATE]**: `1,2,3,4,5,6,7,8,2,3,4,5,6,7,8,1,3,4,5,6,7,8,1,2,4,5,6,7,8,1,2,3,5,6,7,8,1,2,3,4,6,7,8,1,2,3,4,5,7,8,1,2,3,4,5,6,8,1,2,3,4,5,6,7` →
  - 1,2,3,4,... tile ids column by column, row by row
- Example **[REROLL]**: `0,1,2,3,4,5,6,7,8,1,11,12,13,14,15,16,17,18` → 
  - 0 = choose reels set with index 0 (the only one available currently for base spin (since it is with 100% chance))
  - 1,2,3,4,5,6,7,8 = reel stop positions for base spin
  - 1 = choose reels set with index 1 (the only one available currently for reaction re-spin (since it is with 100% chance)) 
  - 11,12,13,14,15,16,17,18 = reel stop positions for re-spin
  - continue randomly with RNGs, since the predefined sequence is exhausted

Useful for:

- Reproducing specific wins
- Creating demo videos
- Testing pay-table accuracy
