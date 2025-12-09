package com.relax.reactor.service.gamelogic.core.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Accessors(chain = true)
public class Pair<K, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private K x;

    private V y;

    public static <K, V> Pair<K, V> of(K x, V y) {
        return new Pair<>(x, y);
    }

    public Pair() {
        this.x = null;
        this.y = null;
    }

    public Pair(K x, V y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + "=" + y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair) {
            Pair<K, V> pair = (Pair<K, V>) o;
            if (!Objects.equals(x, pair.x)) return false;
            return Objects.equals(y, pair.y);
        }
        return false;
    }
}
