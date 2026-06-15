package com.shadowfang.core.faction;

import java.util.UUID;

public class FactionPendingInvite {
    private final UUID factionId;
    private final String factionName;
    private final String inviterName;
    private final long timestamp;

    public FactionPendingInvite(UUID factionId, String factionName, String inviterName) {
        this.factionId = factionId;
        this.factionName = factionName;
        this.inviterName = inviterName;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getFactionId() { return factionId; }
    public String getFactionName() { return factionName; }
    public String getInviterName() { return inviterName; }
    public long getTimestamp() { return timestamp; }
}
