package com.relax.reactor.service.gamelogic.core.util;

import com.relax.reactor.service.gamelogic.core.data.GridPosition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class HelperGameLogicMethods {

    public static int fit(int reelStopPos, int reelSize) {
        if (reelStopPos < 0) {
            return fit(reelStopPos + reelSize, reelSize);
        }

        if (reelStopPos >= reelSize) {
            return reelStopPos % reelSize;
        }

        return reelStopPos;
    }

    public static List<List<Integer>> copyGrid(List<List<Integer>> grid) {
        List<List<Integer>> result = new ArrayList<>(grid.size());

        for (List<Integer> reel : grid) {
            result.add(new ArrayList<>(reel));
        }

        return result;
    }

    public static GridPosition extractMatchStart(List<List<Integer>> contact2DimPos) {

        for (int i = 0; i < contact2DimPos.size(); i++) {
            if (contact2DimPos.get(i).isEmpty()) {
                continue;
            }

            return new GridPosition(i, contact2DimPos.get(i).get(0));
        }

        throw new IllegalArgumentException("[ERR] invalid contact data");
    }

    public static double roundToPrecision(double value) {
        return roundToPrecision(value, 2);
    }

    public static double roundToPrecision(double value, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("[ERR] negative precision places");
        }

        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(precision, RoundingMode.HALF_UP);

        return bigDecimal.doubleValue();
    }

    public static List<Integer> extractMatch1DimPos(List<List<Integer>> contact2DimPos, int reelsCnt) {
        Set<Integer> line1DimPos = new TreeSet<>();

        for (int i = 0; i < contact2DimPos.size(); i++) {
            int INNER_SIZE = contact2DimPos.get(i).size();

            for (int j = 0; j < INNER_SIZE; j++) {
                int ROW = contact2DimPos.get(i).get(j);

                line1DimPos.add(reelsCnt * ROW + i);
            }
        }

        return new ArrayList<>(line1DimPos);
    }

    public static List<Integer> extractMatch1DimSym(List<Integer> contact1DimPos, List<List<Integer>> grid) {
        List<Integer> line1DimSym = new ArrayList<>();

        int reelsCnt = grid.size();

        for (Integer currPos : contact1DimPos) {
            int currReel = currPos % reelsCnt;
            int currRow = currPos / reelsCnt;

            line1DimSym.add(grid.get(currReel).get(currRow));
        }

        return line1DimSym;
    }

    public static List<List<Integer>> extractMatch2DimSym(List<List<Integer>> contact2DimPos, List<List<Integer>> screen) {
        List<List<Integer>> contact2DimSym = new ArrayList<>(contact2DimPos.size());

        for (int i = 0; i < contact2DimPos.size(); i++) {

            List<Integer> currSym = new ArrayList<>();

            for (int j = 0; j < contact2DimPos.get(i).size(); j++) {
                int ROW = contact2DimPos.get(i).get(j);
                currSym.add(screen.get(i).get(ROW));
            }

            contact2DimSym.add(currSym);
        }

        return contact2DimSym;
    }

    public static boolean isValid(int reel, int row, List<List<Integer>> grid) {
        return reel >= 0 && row >= 0 && reel < grid.size() && row < grid.get(0).size();
    }
}
