package com.shadowfang.talisman;

public enum TalismanMode {
    PRIMARY(0),
    SECONDARY(1),
    TERTIARY(2);

    public final int index;

    TalismanMode(int index) {
        this.index = index;
    }

    public TalismanMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static TalismanMode fromIndex(int i) {
        for (TalismanMode m : values()) {
            if (m.index == i) return m;
        }
        return PRIMARY;
    }
}
