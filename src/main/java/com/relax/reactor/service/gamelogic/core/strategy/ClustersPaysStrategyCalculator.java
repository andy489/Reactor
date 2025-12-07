package com.relax.reactor.service.gamelogic.core.strategy;

import ch.qos.logback.core.util.AggregationType;
import com.relax.reactor.service.gamelogic.core.data.GridPosition;
import com.relax.reactor.service.gamelogic.core.data.Pair;
import com.relax.reactor.service.gamelogic.dto.payout.SlotContactDto;
import com.relax.reactor.service.gamelogic.enumerated.MatchType;
import com.relax.reactor.service.gamelogic.enumerated.Strategy;
import com.relax.reactor.service.gamelogic.enumerated.WildMultipliersAggregationType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.copyGrid;
import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.extractMatch1DimPos;
import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.extractMatch1DimSym;
import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.extractMatch2DimSym;
import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.extractMatchStart;
import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.isValid;
import static com.relax.reactor.service.gamelogic.core.util.HelperGameLogicMethods.roundToPrecision;
import static com.relax.reactor.service.gamelogic.core.util.UtilConstants.VISITED_GRID_CELL;
import static java.util.Collections.sort;

/**
 * Two tiles are part in one cluster if their adjacency function is satisfied (returns true).
 * We will use an adjacency (neighbors) feature to determine whether two cells are adjacent.
 * N or more identical symbols appearing in a cluster award a win.
 */
@Getter
@Setter
@Accessors(chain = true)
public class ClustersPaysStrategyCalculator {

    private Map<Integer, TreeMap<Integer, Double>> payouts;

    private Strategy strategy;
    private Integer minMatch;
    private Map<Integer, Double> wildMultipliers;
    private Map<Integer, WildMultipliersAggregationType> wildMultipliersAggregations;
    private WildMultipliersAggregationType currWildMultipliersAggregation = WildMultipliersAggregationType.NONE;
    private List<Integer> excludeMatchSymbols;
    private List<List<Integer>> neighbors;

    public SlotContactDto calc(List<List<Integer>> grid, double stake) {

        SlotContactDto contacts = new SlotContactDto()
                .setFloatIds(new ArrayList<>())
                .setPayoutSymbols(new ArrayList<>())

                .setContactSizes(new ArrayList<>())
                .setContactStarts(new ArrayList<>())

                .setMatchType(MatchType.CLUSTERS)

                .setContact1DimPositions(new ArrayList<>())
                .setContact1DimSymbols(new ArrayList<>())

                .setContact2DimPositions(new ArrayList<>())
                .setContact2DimSymbols(new ArrayList<>())

                .setLocalMultipliers(new ArrayList<>())
                .setGlobalMultiplier(1.0)

                .setPayouts(new ArrayList<>())
                .setMultipliedPayouts(new ArrayList<>())

                .setStashedTotalWinAmount(0.0d);

        int REELS_CNT = grid.size();
        int ROWS_CNT = grid.get(0).size();

        // extract symbol that pay by pay table
        Set<Integer> normalTiles = payouts.keySet();

        // create contacts
        int floatId = 0;
        for (Integer currNormalSym : normalTiles) {
            List<List<Integer>> copyScreen = copyGrid(grid);

            int reel = 0;
            for (; reel < REELS_CNT; reel++) {
                int row = 0;
                for (; row < ROWS_CNT; row++) {
                    Integer currSymbol = copyScreen.get(reel).get(row);

                    if (currSymbol.equals(currNormalSym)) {

                        // init 2 dimensional properties
                        Pair<Integer, Double> cellsAndWildMultipliers = new Pair<>(0, 0.0d);
                        List<List<Integer>> contact2DimPos = new ArrayList<>();

                        for (int i = 0; i < REELS_CNT; i++) {
                            contact2DimPos.add(new ArrayList<>());
                        }
                        // EO: init 2 dimensional properties

                        // depth-first search to traverse the grid
                        dfs(reel, row, copyScreen, currNormalSym, cellsAndWildMultipliers, contact2DimPos);

                        currWildMultipliersAggregation = WildMultipliersAggregationType.NONE;

                        if (cellsAndWildMultipliers.getKey() >= minMatch) {

                            // arrange attributes
                            Integer contactSize = cellsAndWildMultipliers.getKey();
                            MatchType matchType = strategy.getMatchType();

                            for (int i = 0; i < REELS_CNT; i++) {
                                sort(contact2DimPos.get(i));
                            }

                            GridPosition contactStart = extractMatchStart(contact2DimPos);

                            double wildMultipliersAggregated = 1.0d;
                            if (cellsAndWildMultipliers.getValue() > 0.0d) {
                                wildMultipliersAggregated = cellsAndWildMultipliers.getValue();
                            }

                            double singularPay = payouts.get(currNormalSym).floorEntry(contactSize).getValue();
                            singularPay = roundToPrecision(singularPay * stake);

                            double winAmount = singularPay * wildMultipliersAggregated;
                            winAmount = roundToPrecision(winAmount);

                            List<Integer> contact1DimPos = extractMatch1DimPos(contact2DimPos, REELS_CNT);
                            List<Integer> contact1DimSym = extractMatch1DimSym(contact1DimPos, grid);
                            List<List<Integer>> contact2DimSym = extractMatch2DimSym(contact2DimPos, grid);
                            // EO: arrange attributes

                            // fill contacts
                            contacts.getFloatIds().add(floatId++);
                            contacts.getPayoutSymbols().add(currNormalSym);

                            contacts.getContactSizes().add(contactSize);
                            contacts.getContactStarts().add(contactStart);

                            contacts.setMatchType(matchType);

                            contacts.getContact1DimPositions().add(contact1DimPos);
                            contacts.getContact1DimSymbols().add(contact1DimSym);

                            contacts.getContact2DimPositions().add(contact2DimPos);
                            contacts.getContact2DimSymbols().add(contact2DimSym);

                            contacts.getLocalMultipliers().add(wildMultipliersAggregated);

                            contacts.getPayouts().add(singularPay);
                            contacts.getMultipliedPayouts().add(winAmount);
                        }
                    }
                }
            }
        }

        double totalWinAmount = contacts.getMultipliedPayouts().stream().mapToDouble(Double::doubleValue).sum();

        // from base class
        contacts.setWinAmount(roundToPrecision(totalWinAmount));

        if (!contacts.getContactSizes().isEmpty()) {
            return contacts;
        } else {
            return null; // no win combinations
        }
    }

    private void dfs(Integer reel, Integer row, List<List<Integer>> screen, Integer trackedSymbol,
                     Pair<Integer, Double> cellsAndWildMultipliers, List<List<Integer>> contact2DimPos) {

        cellsAndWildMultipliers.setKey(cellsAndWildMultipliers.getKey() + 1);
        Integer currSym = screen.get(reel).get(row);

        // calculate wilds multipliers
        if (wildMultipliers.containsKey(currSym)) {
            Double multiplierValue = wildMultipliers.get(currSym);

            if (!wildMultipliersAggregations.containsKey(currSym)) {
                throw new IllegalArgumentException("Undefined wilds aggregation");
            }

            WildMultipliersAggregationType aggregationType = wildMultipliersAggregations.get(currSym);

            if (multiplierValue >= 1.0d &&
                    !wildMultipliersAggregations.get(currSym).equals(WildMultipliersAggregationType.NONE)) {

                switch (aggregationType) {
                    case ADDITIVE: {
                        if (currWildMultipliersAggregation == WildMultipliersAggregationType.NONE) {
                            currWildMultipliersAggregation = WildMultipliersAggregationType.ADDITIVE;
                            cellsAndWildMultipliers.setValue(multiplierValue);
                            break;
                        } else if (currWildMultipliersAggregation == WildMultipliersAggregationType.ADDITIVE) {
                            cellsAndWildMultipliers.setValue(cellsAndWildMultipliers.getValue() + multiplierValue);
                            break;
                        }

                        throw new IllegalStateException("Only wilds with same multiplier aggregation function" +
                                "can participate in one cluster");
                    }
                    case MULTIPLICATIVE: {
                        if (currWildMultipliersAggregation == WildMultipliersAggregationType.NONE) {
                            currWildMultipliersAggregation = WildMultipliersAggregationType.MULTIPLICATIVE;
                            cellsAndWildMultipliers.setValue(multiplierValue);
                            break;
                        } else if (currWildMultipliersAggregation == WildMultipliersAggregationType.MULTIPLICATIVE) {
                            cellsAndWildMultipliers.setValue(cellsAndWildMultipliers.getValue() * multiplierValue);
                            break;
                        }
                        throw new IllegalStateException("Only wilds with same multiplier aggregation function" +
                                "can participate in one cluster");
                    }
                    case WAYS: {
                        throw new IllegalArgumentException("Invalid wild multiplier aggregation function. " +
                                "WAYS is not supported for cluster strategy");
                    }
                    case NONE:
                    default:
                }
            }
        }
        // EO: calculate wilds multipliers

        if (currSym.equals(VISITED_GRID_CELL)) {
            return;
        }

        screen.get(reel).set(row, VISITED_GRID_CELL);

        contact2DimPos.get(reel).add(row); // add position

        int i = 0;
        for (; i < neighbors.size(); i++) {
            int nextReel = reel + neighbors.get(i).get(0);
            int nextRow = row + neighbors.get(i).get(1);

            if (isValid(nextReel, nextRow, screen) && isTrackedSymbol(nextReel, nextRow, screen, trackedSymbol)) {
                dfs(nextReel, nextRow, screen, trackedSymbol, cellsAndWildMultipliers, contact2DimPos);
            }
        }
    }

    private Boolean isTrackedSymbol(Integer reel, Integer row, List<List<Integer>> screen, Integer trackedSymbol) {
        return screen.get(reel).get(row).equals(trackedSymbol) ||
                wildMultipliers.containsKey(screen.get(reel).get(row));
    }
}
