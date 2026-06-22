package org.xeb.xeb.medallion;

public enum MedallionType {
    COMMON(0xCD7F32, "Bronze"),
    RARE(0xC0C0C0, "Silver"),
    LEGENDARY(0xFFD700, "Gold");

    private final int color;
    private final String displayName;

    MedallionType(int color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public int getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }
}
