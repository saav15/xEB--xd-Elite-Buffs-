package org.xeb.xeb.medallion;

import org.xeb.xeb.buff.EliteBuff;
import java.util.UUID;

public class MedallionData {
    private final EliteBuff buff;
    private final MedallionType tier;
    private final UUID uniqueId;

    public MedallionData(EliteBuff buff, MedallionType tier, UUID uniqueId) {
        this.buff = buff;
        this.tier = tier;
        this.uniqueId = uniqueId;
    }

    public EliteBuff getBuff() {
        return buff;
    }

    public MedallionType getTier() {
        return tier;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }
}
