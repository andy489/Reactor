package com.relax.reactor.service.gamelogic.core.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class GridPosition implements Comparable<GridPosition>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected Integer reelInd;

    protected Integer rowInd;

    public static GridPosition of(Integer reelInd, Integer rowInd) {
        return new GridPosition(reelInd, rowInd);
    }

    public GridPosition() {
        this.reelInd = -1;
        this.rowInd = -1;
    }

    public GridPosition(Integer reelInd, Integer rowInd) {
        if (reelInd < 0 || rowInd < 0) {
            throw new IllegalArgumentException("[ERR Reel ind and row ind must be non-negative");
        }

        this.reelInd = reelInd;
        this.rowInd = rowInd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GridPosition other = (GridPosition) o;

        if (!Objects.equals(reelInd, other.reelInd)) {
            return false;
        }

        return (Objects.equals(rowInd, other.rowInd));
    }

    @Override
    public int hashCode() {
        int result = reelInd != null ? reelInd.hashCode() : 0;
        result = 31 * result * (rowInd != null ? rowInd.hashCode() : 0);

        return result;
    }

    @Override
    public int compareTo(GridPosition o) {
        if (this.reelInd.equals(o.reelInd)) {
            return this.rowInd.compareTo(o.rowInd);
        }

        return this.reelInd.compareTo(o.reelInd);
    }
}