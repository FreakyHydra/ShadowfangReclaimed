package com.shadowfang.core.faction;

import java.util.Objects;
import java.util.UUID;

public class FactionChunk {
    public final String dimension;
    public final int x;
    public final int z;
    public final UUID factionId;

    public FactionChunk(String dimension, int x, int z, UUID factionId) {
        this.dimension = dimension;
        this.x = x;
        this.z = z;
        this.factionId = factionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionChunk that = (FactionChunk) o;
        return x == that.x && z == that.z && dimension.equals(that.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, x, z);
    }
}
