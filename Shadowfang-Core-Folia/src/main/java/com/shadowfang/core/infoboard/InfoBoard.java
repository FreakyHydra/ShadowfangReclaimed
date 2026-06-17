package com.shadowfang.core.infoboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfoBoard {
    private String id;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private List<String> programNames = new ArrayList<>();
    private int currentProgramIndex;
    private int currentPage;
    private int updateIntervalTicks = 20;
    private transient UUID textDisplayUuid;
    private transient UUID interactionUuid;
    private transient long lastUpdateTick;

    public InfoBoard() {}

    public InfoBoard(String id, String world, double x, double y, double z, float yaw, float pitch) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.lastUpdateTick = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public List<String> getProgramNames() { return programNames; }
    public void setProgramNames(List<String> programNames) { this.programNames = programNames; }
    public int getCurrentProgramIndex() { return currentProgramIndex; }
    public void setCurrentProgramIndex(int i) { this.currentProgramIndex = i; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int p) { this.currentPage = p; }
    public int getUpdateIntervalTicks() { return updateIntervalTicks; }
    public void setUpdateIntervalTicks(int t) { this.updateIntervalTicks = t; }
    public UUID getTextDisplayUuid() { return textDisplayUuid; }
    public void setTextDisplayUuid(UUID u) { this.textDisplayUuid = u; }
    public UUID getInteractionUuid() { return interactionUuid; }
    public void setInteractionUuid(UUID u) { this.interactionUuid = u; }
    public long getLastUpdateTick() { return lastUpdateTick; }
    public void setLastUpdateTick(long t) { this.lastUpdateTick = t; }
}
