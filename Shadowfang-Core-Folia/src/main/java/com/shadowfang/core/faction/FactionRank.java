package com.shadowfang.core.faction;

public enum FactionRank {
    ALPHA(3),
    BETA(2),
    DELTA(1),
    OMEGA(0);

    private final int level;

    FactionRank(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isAtLeast(FactionRank other) {
        return this.level >= other.level;
    }
}
