package com.shadowfang.core.faction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.Location;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FactionManager {
    private static final int CLAIM_RADIUS = 1;
    private static final int BUFFER_CHUNKS = 10;
    private static final int SPAWN_DISTANCE = 1000;

    private final Map<UUID, Faction> factions = new ConcurrentHashMap<>();
    private final Map<String, Faction> factionsByName = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerFactions = new ConcurrentHashMap<>();
    private final Map<FactionChunk, UUID> claimedChunks = new ConcurrentHashMap<>();
    private final Map<UUID, FactionPendingInvite> pendingInvites = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path dataFile;
    private Path chunksFile;

    public void load() {
        Path dataDir = Path.of("config/shadowfang-core");
        try { Files.createDirectories(dataDir); } catch (IOException e) {}
        
        dataFile = dataDir.resolve("factions.json");
        chunksFile = dataDir.resolve("faction_chunks.json");
        
        if (Files.exists(dataFile)) {
            try (Reader reader = Files.newBufferedReader(dataFile)) {
                Type type = new TypeToken<List<Faction>>(){}.getType();
                List<Faction> list = gson.fromJson(reader, type);
                if (list != null) {
                    for (Faction f : list) {
                        factions.put(f.getId(), f);
                        factionsByName.put(f.getName().toLowerCase(), f);
                        for (UUID memberId : f.getMembers().keySet()) {
                            playerFactions.put(memberId, f.getId());
                        }
                    }
                }
            } catch (Exception e) {
                ShadowfangCorePlugin.getInstance().getLogger().severe("Failed to load factions");
            }
        }

        if (Files.exists(chunksFile)) {
            try (Reader reader = Files.newBufferedReader(chunksFile)) {
                Type type = new TypeToken<Map<String, UUID>>(){}.getType();
                Map<String, UUID> map = gson.fromJson(reader, type);
                if (map != null) {
                    for (Map.Entry<String, UUID> entry : map.entrySet()) {
                        String[] parts = entry.getKey().split(";");
                        if (parts.length == 3) {
                            FactionChunk chunk = new FactionChunk(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), entry.getValue());
                            claimedChunks.put(chunk, entry.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                ShadowfangCorePlugin.getInstance().getLogger().severe("Failed to load faction chunks");
            }
        }
    }

    public void save() {
        if (dataFile == null) return;
        try (Writer writer = Files.newBufferedWriter(dataFile)) {
            gson.toJson(new ArrayList<>(factions.values()), writer);
        } catch (IOException e) {
            ShadowfangCorePlugin.getInstance().getLogger().severe("Failed to save factions");
        }

        try (Writer writer = Files.newBufferedWriter(chunksFile)) {
            Map<String, UUID> mapToSave = new HashMap<>();
            for (Map.Entry<FactionChunk, UUID> entry : claimedChunks.entrySet()) {
                FactionChunk c = entry.getKey();
                String key = c.dimension + ";" + c.x + ";" + c.z;
                mapToSave.put(key, entry.getValue());
            }
            gson.toJson(mapToSave, writer);
        } catch (IOException e) {
            ShadowfangCorePlugin.getInstance().getLogger().severe("Failed to save faction chunks");
        }
    }

    public Faction createFaction(String name, UUID alpha) {
        if (factionsByName.containsKey(name.toLowerCase())) {
            return null;
        }
        Faction faction = new Faction(UUID.randomUUID(), name, alpha);
        factions.put(faction.getId(), faction);
        factionsByName.put(name.toLowerCase(), faction);
        playerFactions.put(alpha, faction.getId());
        return faction;
    }

    public void disbandFaction(UUID factionId) {
        Faction f = factions.remove(factionId);
        if (f != null) {
            factionsByName.remove(f.getName().toLowerCase());
            for (UUID memberId : f.getMembers().keySet()) {
                playerFactions.remove(memberId);
            }
            claimedChunks.entrySet().removeIf(entry -> entry.getValue().equals(factionId));
        }
    }

    public Faction getFaction(UUID factionId) {
        return factions.get(factionId);
    }

    public Faction getFactionByName(String name) {
        return factionsByName.get(name.toLowerCase());
    }

    public Faction getPlayerFaction(UUID playerId) {
        UUID factionId = playerFactions.get(playerId);
        if (factionId != null) {
            return factions.get(factionId);
        }
        return null;
    }

    public void addPlayerToIndex(UUID playerId, UUID factionId) {
        playerFactions.put(playerId, factionId);
    }

    public void removePlayerFromIndex(UUID playerId) {
        playerFactions.remove(playerId);
    }

    public void addPendingInvite(UUID targetId, UUID factionId, String factionName, String inviterName) {
        pendingInvites.put(targetId, new FactionPendingInvite(factionId, factionName, inviterName));
    }

    public FactionPendingInvite getPendingInvite(UUID targetId) {
        return pendingInvites.get(targetId);
    }

    public boolean hasPendingInvite(UUID targetId) {
        return pendingInvites.containsKey(targetId);
    }

    public void removePendingInvite(UUID targetId) {
        pendingInvites.remove(targetId);
    }

    public Faction getFactionAt(String dimension, int x, int z) {
        FactionChunk key = new FactionChunk(dimension, x, z, null);
        UUID factionId = claimedChunks.get(key);
        if (factionId != null) {
            return getFaction(factionId);
        }
        return null;
    }

    public boolean claimChunk(Faction faction, String dimension, int x, int z) {
        FactionChunk key = new FactionChunk(dimension, x, z, faction.getId());
        if (claimedChunks.containsKey(key)) {
            return false;
        }
        claimedChunks.put(key, faction.getId());
        return true;
    }

    public boolean unclaimChunk(String dimension, int x, int z) {
        FactionChunk key = new FactionChunk(dimension, x, z, null);
        if (claimedChunks.containsKey(key)) {
            claimedChunks.remove(key);
            return true;
        }
        return false;
    }

    public Collection<Faction> getAllFactions() {
        return factions.values();
    }

    public boolean isWithinSpawnRadius(Location location, int blocks) {
        if (location.getWorld() == null) return false;
        Location spawn = location.getWorld().getSpawnLocation();
        return spawn.distance(location) < blocks;
    }

    public boolean isWithinBuffer(String dimension, int chunkX, int chunkZ) {
        for (FactionChunk claimed : claimedChunks.keySet()) {
            if (!claimed.dimension.equals(dimension)) continue;
            int dx = Math.abs(claimed.x - chunkX);
            int dz = Math.abs(claimed.z - chunkZ);
            if (dx <= BUFFER_CHUNKS && dz <= BUFFER_CHUNKS) {
                return true;
            }
        }
        return false;
    }

    public boolean canClaimArea(String dimension, int centerChunkX, int centerChunkZ) {
        for (int dx = -CLAIM_RADIUS; dx <= CLAIM_RADIUS; dx++) {
            for (int dz = -CLAIM_RADIUS; dz <= CLAIM_RADIUS; dz++) {
                int checkX = centerChunkX + dx;
                int checkZ = centerChunkZ + dz;
                FactionChunk key = new FactionChunk(dimension, checkX, checkZ, null);
                if (claimedChunks.containsKey(key)) {
                    return false;
                }
            }
        }
        return !isWithinBuffer(dimension, centerChunkX, centerChunkZ);
    }

    public boolean claimBellArea(Faction faction, String dimension, int centerChunkX, int centerChunkZ) {
        if (!canClaimArea(dimension, centerChunkX, centerChunkZ)) {
            return false;
        }
        for (int dx = -CLAIM_RADIUS; dx <= CLAIM_RADIUS; dx++) {
            for (int dz = -CLAIM_RADIUS; dz <= CLAIM_RADIUS; dz++) {
                int claimX = centerChunkX + dx;
                int claimZ = centerChunkZ + dz;
                FactionChunk key = new FactionChunk(dimension, claimX, claimZ, faction.getId());
                claimedChunks.put(key, faction.getId());
            }
        }
        return true;
    }

    public int claimArea(Faction faction, String dimension, int centerChunkX, int centerChunkZ, int radius) {
        int claimed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int claimX = centerChunkX + dx;
                int claimZ = centerChunkZ + dz;
                FactionChunk key = new FactionChunk(dimension, claimX, claimZ, faction.getId());
                if (!claimedChunks.containsKey(key)) {
                    claimedChunks.put(key, faction.getId());
                    claimed++;
                }
            }
        }
        return claimed;
    }

    public int getClaimedChunkCount(UUID factionId) {
        int count = 0;
        for (UUID owner : claimedChunks.values()) {
            if (owner.equals(factionId)) count++;
        }
        return count;
    }

    public boolean isAreaClaimable(String dimension, int centerChunkX, int centerChunkZ, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int checkX = centerChunkX + dx;
                int checkZ = centerChunkZ + dz;
                FactionChunk key = new FactionChunk(dimension, checkX, checkZ, null);
                if (claimedChunks.containsKey(key)) {
                    return false;
                }
            }
        }
        return !isWithinBuffer(dimension, centerChunkX, centerChunkZ);
    }

    public void unclaimBellArea(String dimension, int centerChunkX, int centerChunkZ) {
        for (int dx = -CLAIM_RADIUS; dx <= CLAIM_RADIUS; dx++) {
            for (int dz = -CLAIM_RADIUS; dz <= CLAIM_RADIUS; dz++) {
                int claimX = centerChunkX + dx;
                int claimZ = centerChunkZ + dz;
                FactionChunk key = new FactionChunk(dimension, claimX, claimZ, null);
                claimedChunks.remove(key);
            }
        }
    }

    public int[] findNearestSafeChunk(String dimension, int fromChunkX, int fromChunkZ) {
        for (int radius = 1; radius <= 100; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int checkX = fromChunkX + dx;
                    int checkZ = fromChunkZ + dz;
                    if (canClaimArea(dimension, checkX, checkZ)) {
                        return new int[]{checkX, checkZ};
                    }
                }
            }
        }
        return null;
    }
}
