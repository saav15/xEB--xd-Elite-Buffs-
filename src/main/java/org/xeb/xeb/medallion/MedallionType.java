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

    public net.minecraft.network.chat.Component getDisplayName() {
        String tierKey = switch (this) {
            case COMMON -> "bronze";
            case RARE -> "silver";
            case LEGENDARY -> "gold";
        };
        return net.minecraft.network.chat.Component.translatable("xeb.medallion.tier." + tierKey);
    }
}
