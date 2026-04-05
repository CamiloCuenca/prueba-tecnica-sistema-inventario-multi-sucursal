package com.camilocuenca.inventorysystem.events;

import java.util.UUID;

public class LowStockCheckEvent {
    private final UUID branchId;
    private final UUID actorId; // puede ser null para jobs

    public LowStockCheckEvent(UUID branchId, UUID actorId) {
        this.branchId = branchId;
        this.actorId = actorId;
    }

    public UUID getBranchId() { return branchId; }
    public UUID getActorId() { return actorId; }
}

