package com.shadowfang.core.infoboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InfoBoardManager {

    private final ShadowfangCorePlugin plugin;
    private final Map<String, InfoBoard> boards = new ConcurrentHashMap<>();
    private final Map<String, CustomBoardProgram> customPrograms = new ConcurrentHashMap<>();
    private final Map<String, Program> builtinPrograms = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final BoardRenderer renderer;
    private Path dataFile;
    private long currentTick = 0;

    public InfoBoardManager(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
        this.renderer = new BoardRenderer(plugin);
    }

    public void load() {
        Path dataDir = plugin.getDataFolder().toPath();
        try { Files.createDirectories(dataDir); } catch (IOException e) {}
        dataFile = dataDir.resolve("info_boards.json");

        if (Files.exists(dataFile)) {
            try (Reader reader = Files.newBufferedReader(dataFile)) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> data = gson.fromJson(reader, type);
                if (data != null) {
                    Object boardsRaw = data.get("boards");
                    if (boardsRaw instanceof List) {
                        for (Object o : (List) boardsRaw) {
                            InfoBoard b = gson.fromJson(gson.toJsonTree(o), InfoBoard.class);
                            if (b != null) boards.put(b.getId(), b);
                        }
                    }
                    Object programsRaw = data.get("customPrograms");
                    if (programsRaw instanceof List) {
                        for (Object o : (List) programsRaw) {
                            CustomBoardProgram p = gson.fromJson(gson.toJsonTree(o), CustomBoardProgram.class);
                            if (p != null) customPrograms.put(p.getName().toLowerCase(), p);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load info boards: " + e.getMessage());
            }
        }

        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            for (InfoBoard board : boards.values()) {
                renderer.spawnEntities(board);
            }
        });

        startTickTask();
    }

    public void save() {
        if (dataFile == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("boards", new ArrayList<>(boards.values()));
        data.put("customPrograms", new ArrayList<>(customPrograms.values()));
        try (Writer writer = Files.newBufferedWriter(dataFile)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save info boards: " + e.getMessage());
        }
    }

    public void shutdown() {
        stopTickTask();
        for (InfoBoard board : boards.values()) {
            renderer.removeEntities(board);
        }
        save();
    }

    private void startTickTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            currentTick++;
            for (InfoBoard board : boards.values()) {
                boolean missing = false;
                if (board.getTextDisplayUuid() != null && Bukkit.getEntity(board.getTextDisplayUuid()) == null) missing = true;
                if (board.getInteractionUuid() != null && Bukkit.getEntity(board.getInteractionUuid()) == null) missing = true;
                if (missing) {
                    renderer.removeEntities(board);
                    renderer.spawnEntities(board);
                }
                if (currentTick - board.getLastUpdateTick() < board.getUpdateIntervalTicks()) continue;
                board.setLastUpdateTick(currentTick);
                renderBoard(board);
            }
        }, 10, 10);
    }

    private void stopTickTask() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
    }

    private void renderBoard(InfoBoard board) {
        List<String> programNames = board.getProgramNames();
        if (programNames.isEmpty()) {
            renderer.updateText(board, Collections.singletonList("§7No program assigned"));
            return;
        }
        int idx = board.getCurrentProgramIndex();
        if (idx < 0 || idx >= programNames.size()) {
            idx = 0;
            board.setCurrentProgramIndex(0);
        }
        String progName = programNames.get(idx);
        List<String> lines = getRenderedProgram(progName, board);
        if (lines.isEmpty()) {
            renderer.updateText(board, Collections.singletonList("§cProgram '" + progName + "' not found"));
            return;
        }
        renderer.updateText(board, lines);
    }

    private List<String> getRenderedProgram(String name, InfoBoard board) {
        Program builtin = builtinPrograms.get(name.toLowerCase());
        if (builtin != null) {
            return builtin.render(board);
        }
        CustomBoardProgram custom = customPrograms.get(name.toLowerCase());
        if (custom != null) {
            List<String> pages = custom.getPages();
            if (pages.isEmpty()) return Collections.singletonList("§7(empty)");
            int pg = board.getCurrentPage();
            if (pg < 0 || pg >= pages.size()) {
                pg = 0;
                board.setCurrentPage(0);
            }
            return Arrays.asList(pages.get(pg).split("\n"));
        }
        return Collections.emptyList();
    }

    public void setBuiltinPrograms(List<Program> programs) {
        builtinPrograms.clear();
        for (Program p : programs) {
            builtinPrograms.put(p.getName().toLowerCase(), p);
        }
    }

    public InfoBoard createBoard(String id, String world, double x, double y, double z, float yaw, float pitch) {
        if (boards.containsKey(id)) return null;
        InfoBoard board = new InfoBoard(id, world, x, y, z, yaw, pitch);
        boards.put(id, board);
        renderer.spawnEntities(board);
        save();
        return board;
    }

    public boolean removeBoard(String id) {
        InfoBoard board = boards.remove(id);
        if (board != null) {
            renderer.removeEntities(board);
            save();
            return true;
        }
        return false;
    }

    public InfoBoard getBoard(String id) {
        return boards.get(id);
    }

    public Collection<InfoBoard> getAllBoards() {
        return boards.values();
    }

    public boolean addProgramToBoard(String boardId, String programName) {
        InfoBoard board = boards.get(boardId);
        if (board == null) return false;
        if (!board.getProgramNames().contains(programName)) {
            board.getProgramNames().add(programName);
            save();
        }
        return true;
    }

    public boolean removeProgramFromBoard(String boardId, String programName) {
        InfoBoard board = boards.get(boardId);
        if (board == null) return false;
        boolean removed = board.getProgramNames().remove(programName);
        if (removed) save();
        return removed;
    }

    public boolean cycleProgram(String boardId) {
        InfoBoard board = boards.get(boardId);
        if (board == null || board.getProgramNames().isEmpty()) return false;
        int next = (board.getCurrentProgramIndex() + 1) % board.getProgramNames().size();
        board.setCurrentProgramIndex(next);
        board.setCurrentPage(0);
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> renderBoard(board));
        return true;
    }

    public void createCustomProgram(String name, List<String> pages) {
        customPrograms.put(name.toLowerCase(), new CustomBoardProgram(name, pages));
        save();
    }

    public boolean removeCustomProgram(String name) {
        return customPrograms.remove(name.toLowerCase()) != null;
    }

    public CustomBoardProgram getCustomProgram(String name) {
        return customPrograms.get(name.toLowerCase());
    }

    public Collection<CustomBoardProgram> getAllCustomPrograms() {
        return customPrograms.values();
    }

    public List<String> getAllProgramNames() {
        List<String> names = new ArrayList<>(builtinPrograms.keySet());
        names.addAll(customPrograms.keySet());
        return names;
    }

    public BoardRenderer getRenderer() { return renderer; }
}
