package com.relax.reactor.service.gamelogic.core.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class SlotGameStickyData implements Serializable {

    private List<Integer> stickyReels;
    private List<Integer> stickyPos;
    private List<Integer> stickyTileIds;

    public SlotGameStickyData() {
        this.stickyReels = new ArrayList<>();
        this.stickyPos = new ArrayList<>();
        this.stickyTileIds = new ArrayList<>();
    }

    public SlotGameStickyData(SlotGameStickyData slotGameStickyData) {
        if (slotGameStickyData != null && slotGameStickyData.stickyReels != null) {
            this.stickyReels = new ArrayList<>(slotGameStickyData.stickyReels);
            this.stickyPos = new ArrayList<>(slotGameStickyData.stickyPos);
            this.stickyTileIds = new ArrayList<>(slotGameStickyData.stickyTileIds);
        } else {
            this.stickyReels = new ArrayList<>();
            this.stickyPos = new ArrayList<>();
            this.stickyTileIds = new ArrayList<>();
        }
    }

    public void addStickyDataEntry(Integer stickyReel, Integer stickyPos, Integer stickyTileId) {
        this.stickyReels.add(stickyReel);
        this.stickyPos.add(stickyPos);
        this.stickyTileIds.add(stickyTileId);
    }

    public void insertStickyDataEntry(Integer pos, Integer stickyReel, Integer stickyPos, Integer stickyTileId) {
        this.stickyReels.add(pos, stickyReel);
        this.stickyPos.add(pos, stickyPos);
        this.stickyTileIds.add(pos, stickyTileId);
    }

    public int findSticky(int stickyReel, int stickyPos) {
        for (int i = 0; i < stickyReels.size(); i++) {
            if (this.stickyReels.get(i) == stickyReel && this.stickyPos.get(i) == stickyPos) {
                return i;
            }
        }

        return -1;
    }

    public int findSticky(int stickyReel, int stickyPos, int stickyTileId) {
        int i = findSticky(stickyReel, stickyPos);

        if (i >= 0 && this.stickyTileIds.get(i) == stickyTileId) {
            return i;
        }

        return -1;
    }

    public Boolean contains(int stickyReel, int stickyPos, int stickyTileId) {
        return findSticky(stickyReel, stickyPos, stickyTileId) >= 0;
    }

    public Integer getSticky(int stickyReel, int stickyPos) {
        int ind = findSticky(stickyReel, stickyPos);

        if (ind < 0) {
            return null;
        } else {
            return stickyTileIds.get(ind);
        }
    }

    public boolean setSticky(int stickyReel, int stickyPos, int stickySymbolId) {
        int ind = findSticky(stickyReel, stickyPos);

        if (ind < 0) {
            addStickyDataEntry(stickyReel, stickyPos, stickySymbolId);
            return true;
        } else {
            stickyTileIds.set(ind, stickySymbolId);
            return false;
        }
    }

    public void removeSticky(int i) {
        this.stickyReels.remove(i);
        this.stickyPos.remove(i);
        this.stickyTileIds.remove(i);
    }

    public int size() {
        return this.stickyReels.size();
    }

    private void swap(int i, int j, List<?>... lists) {
        for (List<?> list : lists) {
            if (list != null) {
                Collections.swap(list, i, j);
            }
        }
    }

    @SafeVarargs
    public final <T extends Comparable<? super T>> void relativeSort(List<T>... lists) {
        if (lists == null || lists.length == 0) return;

        List<T> primaryList = lists[0];
        if (primaryList == null || primaryList.size() <= 1) return;

        int size = primaryList.size();

        // Validate all non-null lists have the same size
        for (List<T> list : lists) {
            if (list != null && list.size() != size) {
                throw new IllegalArgumentException("All lists must have the same size");
            }
        }

        // Selection sort
        for (int i = 0; i < size - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < size; j++) {
                T current = primaryList.get(j);
                T min = primaryList.get(minIndex);
                if (current.compareTo(min) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swap(i, minIndex, lists);
            }
        }
    }
}
