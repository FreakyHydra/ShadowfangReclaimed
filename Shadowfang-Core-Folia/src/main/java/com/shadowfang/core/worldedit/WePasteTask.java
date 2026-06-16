package com.shadowfang.core.worldedit;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WePasteTask {

    private final WorldEditManager manager;
    private final Map<UUID, AtomicInteger> busyTokens = new HashMap<>();
    private final Map<UUID, Location> lastPlayerBlock = new HashMap<>();

    public WePasteTask(WorldEditManager manager, ShadowfangCorePlugin plugin) {
        this.manager = manager;
    }

    public boolean tryPaste(Player player) {
        UUID id = player.getUniqueId();
        if (!manager.isWalkPasteActive(player)) {
            lastPlayerBlock.remove(id);
            return false;
        }

        AtomicInteger token = busyTokens.computeIfAbsent(id, k -> new AtomicInteger(0));
        if (!token.compareAndSet(0, 1)) return false;

        Location currentLoc = player.getLocation();
        Location lastBlock = lastPlayerBlock.get(id);

        if (lastBlock == null || isDifferentBlock(currentLoc, lastBlock)) {
            lastPlayerBlock.put(id, currentLoc.clone());
            manager.walkPasteTick(player, () -> token.set(0));
            return true;
        }

        token.set(0);
        return false;
    }

    private boolean isDifferentBlock(Location a, Location b) {
        return !a.getWorld().equals(b.getWorld())
                || a.getBlockX() != b.getBlockX()
                || a.getBlockZ() != b.getBlockZ();
    }
}
