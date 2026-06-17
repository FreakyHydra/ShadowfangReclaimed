package com.shadowfang.core.terminal;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.elevator.ElevatorGroup;
import com.shadowfang.core.infoboard.CustomBoardProgram;
import com.shadowfang.core.infoboard.InfoBoard;
import com.shadowfang.core.infoboard.InfoBoardManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class WebTerminalServer {

    private final ShadowfangCorePlugin plugin;
    private HttpServer server;
    private final File logFile;

    public WebTerminalServer(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder().getParentFile().getParentFile(), "logs/latest.log");
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(56552), 0);
            server.createContext("/", new UIHandler());
            server.createContext("/logs", new LogsHandler());
            server.createContext("/execute", new ExecuteHandler());
            server.createContext("/config", new ConfigHandler());
            server.createContext("/api/stats", new ApiStatsHandler());
            server.createContext("/api/players", new ApiPlayersHandler());
            server.createContext("/api/worlds", new ApiWorldsHandler());
            server.createContext("/api/boards", new ApiBoardsHandler());
            server.createContext("/api/elevators", new ApiElevatorsHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            plugin.getLogger().info("Live Web Terminal started on http://localhost:56552");

            // Open browser using powershell
            openBrowser();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start Web Terminal: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Web Terminal stopped.");
        }
    }

    private void openBrowser() {
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Wait for Folia to finish startup prints
                Runtime.getRuntime().exec(new String[]{"powershell.exe", "-Command", "Start-Process 'http://localhost:56552'"});
            } catch (Exception e) {
                plugin.getLogger().warning("Could not automatically open browser: " + e.getMessage());
            }
        }).start();
    }

    class UIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Failed to load UI.";
            try (InputStream in = plugin.getResource("web/index.html")) {
                if (in != null) {
                    response = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to read web UI resource.");
            }
            
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            String response;
            if (logFile.exists()) {
                try {
                    List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
                    // Return last 300 lines for a good buffer
                    int start = Math.max(0, lines.size() - 300);
                    StringBuilder sb = new StringBuilder();
                    for(int i = start; i < lines.size(); i++) {
                        sb.append(lines.get(i)).append("\n");
                    }
                    response = sb.toString();
                } catch (Exception e) {
                    // Fallback to ISO_8859_1 if UTF-8 fails
                    try {
                        List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.ISO_8859_1);
                        int start = Math.max(0, lines.size() - 300);
                        StringBuilder sb = new StringBuilder();
                        for(int i = start; i < lines.size(); i++) {
                            sb.append(lines.get(i)).append("\n");
                        }
                        response = sb.toString();
                    } catch (Exception ex) {
                        response = "Error reading log: " + ex.getMessage();
                    }
                }
            } else {
                response = "Log file not found at " + logFile.getAbsolutePath();
            }

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    class ExecuteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                try (InputStream in = t.getRequestBody()) {
                    String command = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                    if (!command.isEmpty()) {
                        // Folia requires global commands to be dispatched on the global region scheduler
                        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                        });
                    }
                }

                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                String response = "OK";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                t.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            String fileName = "server.properties";
            if (query != null && query.startsWith("file=")) {
                // simple url decode
                fileName = query.substring(5).replace("%2F", "/").replace("%2f", "/");
            }

            if (fileName.contains("..")) {
                t.sendResponseHeaders(403, -1);
                return;
            }

            File targetFile = new File(plugin.getDataFolder().getParentFile().getParentFile(), fileName);
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                if (targetFile.exists()) {
                    byte[] bytes = Files.readAllBytes(targetFile.toPath());
                    t.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    t.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = t.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    String err = "File not found.";
                    t.sendResponseHeaders(404, err.length());
                    try (OutputStream os = t.getResponseBody()) { os.write(err.getBytes()); }
                }
            } else if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                try (InputStream in = t.getRequestBody()) {
                    byte[] newBytes = in.readAllBytes();
                    targetFile.getParentFile().mkdirs();
                    Files.write(targetFile.toPath(), newBytes);
                    
                    if (fileName.endsWith("config.yml")) {
                        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                            plugin.reloadConfig();
                            if (plugin.getFactionManager() != null) plugin.getFactionManager().load();
                            if (plugin.getLoreManager() != null) plugin.getLoreManager().load();
                            plugin.getLogger().info("Configuration hot-reloaded via Web Dashboard.");
                        });
                    }

                    String resp = "OK";
                    t.sendResponseHeaders(200, resp.length());
                    try (OutputStream os = t.getResponseBody()) { os.write(resp.getBytes()); }
                } catch (Exception e) {
                    String err = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    t.sendResponseHeaders(500, err.length());
                    try (OutputStream os = t.getResponseBody()) { os.write(err.getBytes()); }
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class ApiStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            Runtime runtime = Runtime.getRuntime();
            long maxMem = runtime.maxMemory() / 1048576L;
            long allocatedMem = runtime.totalMemory() / 1048576L;
            long freeMem = runtime.freeMemory() / 1048576L;
            long usedMem = allocatedMem - freeMem;
            
            double[] tpsArr = plugin.getServer().getTPS();
            double tps = tpsArr.length > 0 ? tpsArr[0] : 20.0;
            
            int players = plugin.getServer().getOnlinePlayers().size();
            
            String json = String.format("{\"tps\":%.2f,\"ram_used\":%d,\"ram_max\":%d,\"players\":%d}", tps, usedMem, maxMem, players);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
        }
    }

    class ApiPlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                if (!first) json.append(",");
                json.append(String.format("{\"name\":\"%s\",\"uuid\":\"%s\",\"ping\":%d}", p.getName(), p.getUniqueId().toString(), p.getPing()));
                first = false;
            }
            json.append("]");
            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
        }
    }

    class ApiWorldsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            java.util.Map<String, String> registered = com.shadowfang.core.verse.VerseManager.getInstance().getDisplayNames();
            for (java.util.Map.Entry<String, String> entry : registered.entrySet()) {
                if (!first) json.append(",");
                json.append(String.format("{\"name\":\"%s\",\"display\":\"%s\"}", entry.getKey(), entry.getValue()));
                first = false;
            }
            json.append("]");
            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
        }
    }

    class ApiBoardsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            var ibm = plugin.getInfoBoardManager();
            if (ibm == null) {
                String err = "{\"error\":\"InfoBoard not initialized\"}";
                t.sendResponseHeaders(503, err.length());
                try (OutputStream os = t.getResponseBody()) { os.write(err.getBytes()); }
                return;
            }

            String method = t.getRequestMethod().toUpperCase();
            String path = t.getRequestURI().getPath();
            String query = t.getRequestURI().getQuery();

            if (path.equals("/api/boards") || path.equals("/api/boards/")) {
                if (method.equals("GET")) {
                    handleListBoards(t, ibm);
                } else if (method.equals("POST")) {
                    handleCreateBoard(t, ibm);
                } else {
                    t.sendResponseHeaders(405, -1);
                }
            } else if (path.startsWith("/api/boards/")) {
                String rest = path.substring("/api/boards/".length());
                if (rest.equals("programs")) {
                    if (method.equals("GET")) {
                        handleListPrograms(t, ibm);
                    } else if (method.equals("POST")) {
                        handleCreateProgram(t, ibm);
                    } else {
                        t.sendResponseHeaders(405, -1);
                    }
                } else if (rest.startsWith("programs/")) {
                    String progName = URLDecoder.decode(rest.substring("programs/".length()), "UTF-8");
                    if (method.equals("GET")) {
                        handleGetProgram(t, ibm, progName);
                    } else if (method.equals("DELETE")) {
                        handleDeleteProgram(t, ibm, progName);
                    } else {
                        t.sendResponseHeaders(405, -1);
                    }
                } else {
                    String boardId = URLDecoder.decode(rest, "UTF-8");
                    if (method.equals("GET")) {
                        handleGetBoard(t, ibm, boardId);
                    } else if (method.equals("DELETE")) {
                        handleDeleteBoard(t, ibm, boardId);
                    } else if (method.equals("POST")) {
                        handleUpdateBoard(t, ibm, boardId);
                    } else {
                        t.sendResponseHeaders(405, -1);
                    }
                }
            }
        }

        private void handleListBoards(HttpExchange t, InfoBoardManager ibm) throws IOException {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (var board : ibm.getAllBoards()) {
                if (!first) json.append(",");
                json.append(boardToJson(board));
                first = false;
            }
            json.append("]");
            respond(t, json.toString());
        }

        private void handleGetBoard(HttpExchange t, InfoBoardManager ibm, String id) throws IOException {
            var board = ibm.getBoard(id);
            if (board == null) {
                respond(t, "{\"error\":\"Not found\"}", 404);
                return;
            }
            respond(t, boardToJson(board));
        }

        private void handleDeleteBoard(HttpExchange t, InfoBoardManager ibm, String id) throws IOException {
            boolean ok = ibm.removeBoard(id);
            respond(t, ok ? "{\"success\":true}" : "{\"error\":\"Not found\"}", ok ? 200 : 404);
        }

        private void handleCreateBoard(HttpExchange t, InfoBoardManager ibm) throws IOException {
            String body = new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var map = parseJsonMap(body);
            String id = map.get("id");
            String world = map.get("world");
            double x = Double.parseDouble(map.getOrDefault("x", "0"));
            double y = Double.parseDouble(map.getOrDefault("y", "64"));
            double z = Double.parseDouble(map.getOrDefault("z", "0"));
            float yaw = Float.parseFloat(map.getOrDefault("yaw", "0"));
            float pitch = Float.parseFloat(map.getOrDefault("pitch", "0"));
            if (id == null || id.isEmpty() || world == null || world.isEmpty()) {
                respond(t, "{\"error\":\"Missing id or world\"}", 400);
                return;
            }
            var board = ibm.createBoard(id, world, x, y, z, yaw, pitch);
            if (board == null) {
                respond(t, "{\"error\":\"Board already exists\"}", 409);
                return;
            }
            respond(t, boardToJson(board));
        }

        private void handleUpdateBoard(HttpExchange t, InfoBoardManager ibm, String id) throws IOException {
            String body = new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var map = parseJsonMap(body);
            var board = ibm.getBoard(id);
            if (board == null) {
                respond(t, "{\"error\":\"Not found\"}", 404);
                return;
            }
            if (map.containsKey("addProgram")) {
                ibm.addProgramToBoard(id, map.get("addProgram"));
            }
            if (map.containsKey("removeProgram")) {
                ibm.removeProgramFromBoard(id, map.get("removeProgram"));
            }
            if (map.containsKey("cycle")) {
                ibm.cycleProgram(id);
            }
            respond(t, "{\"success\":true}");
        }

        private void handleListPrograms(HttpExchange t, InfoBoardManager ibm) throws IOException {
            StringBuilder json = new StringBuilder("{\"builtin\":[");
            boolean first = true;
            for (String name : new String[]{"players", "factions", "server", "clock", "motd"}) {
                if (!first) json.append(",");
                json.append("\"").append(name).append("\"");
                first = false;
            }
            json.append("],\"custom\":[");
            first = true;
            for (var prog : ibm.getAllCustomPrograms()) {
                if (!first) json.append(",");
                json.append(programToJson(prog));
                first = false;
            }
            json.append("]}");
            respond(t, json.toString());
        }

        private void handleCreateProgram(HttpExchange t, InfoBoardManager ibm) throws IOException {
            String body = new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var map = parseJsonMap(body);
            String name = map.get("name");
            if (name == null || name.isEmpty()) {
                respond(t, "{\"error\":\"Missing name\"}", 400);
                return;
            }
            String rawPages = map.getOrDefault("pages", "");
            List<String> pages = new ArrayList<>();
            for (String p : rawPages.split("\\n---\\n")) {
                pages.add(p.trim());
            }
            if (pages.isEmpty()) pages.add("");
            ibm.createCustomProgram(name, pages);
            respond(t, programToJson(ibm.getCustomProgram(name)));
        }

        private void handleGetProgram(HttpExchange t, InfoBoardManager ibm, String name) throws IOException {
            var prog = ibm.getCustomProgram(name);
            if (prog == null) {
                respond(t, "{\"error\":\"Not found\"}", 404);
                return;
            }
            respond(t, programToJson(prog));
        }

        private void handleDeleteProgram(HttpExchange t, InfoBoardManager ibm, String name) throws IOException {
            boolean ok = ibm.removeCustomProgram(name);
            respond(t, ok ? "{\"success\":true}" : "{\"error\":\"Not found\"}", ok ? 200 : 404);
        }

        private String boardToJson(InfoBoard board) {
            return "{"
                + "\"id\":\"" + escapeJson(board.getId()) + "\","
                + "\"world\":\"" + escapeJson(board.getWorld()) + "\","
                + "\"x\":" + board.getX() + ","
                + "\"y\":" + board.getY() + ","
                + "\"z\":" + board.getZ() + ","
                + "\"yaw\":" + board.getYaw() + ","
                + "\"pitch\":" + board.getPitch() + ","
                + "\"programs\":" + listToJson(board.getProgramNames())
                + "}";
        }

        private String programToJson(CustomBoardProgram prog) {
            StringBuilder pages = new StringBuilder("[");
            boolean first = true;
            for (String p : prog.getPages()) {
                if (!first) pages.append(",");
                pages.append("\"").append(escapeJson(p)).append("\"");
                first = false;
            }
            pages.append("]");
            return "{\"name\":\"" + escapeJson(prog.getName()) + "\",\"pages\":" + pages.toString() + "}";
        }

        private java.util.Map<String, String> parseJsonMap(String json) {
            java.util.Map<String, String> map = new HashMap<>();
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                for (String pair : json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
                    int colon = pair.indexOf(':');
                    if (colon < 0) continue;
                    String key = pair.substring(0, colon).trim().replaceAll("^\"|\"$", "");
                    String val = pair.substring(colon + 1).trim().replaceAll("^\"|\"$", "");
                    map.put(key, val);
                }
            }
            return map;
        }

        private String listToJson(List<String> list) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (String s : list) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(s)).append("\"");
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        private String escapeJson(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }

        private void respond(HttpExchange t, String json) throws IOException {
            respond(t, json, 200);
        }

        private void respond(HttpExchange t, String json, int code) throws IOException {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
        }
    }

    class ApiElevatorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            var em = plugin.getElevatorManager();
            if (em == null) {
                respond(t, "{\"error\":\"ElevatorManager not initialized\"}", 503);
                return;
            }

            String method = t.getRequestMethod().toUpperCase();
            String path = t.getRequestURI().getPath();

            if (path.equals("/api/elevators") || path.equals("/api/elevators/")) {
                if (method.equals("GET")) {
                    handleListElevators(t, em);
                } else if (method.equals("POST")) {
                    handleCreateElevator(t, em);
                } else {
                    t.sendResponseHeaders(405, -1);
                }
            } else if (path.startsWith("/api/elevators/")) {
                String rest = path.substring("/api/elevators/".length());
                if (rest.contains("/")) {
                    String[] parts = rest.split("/", 2);
                    String groupName = URLDecoder.decode(parts[0], "UTF-8");
                    String action = parts[1];
                    if (action.startsWith("floors/") && action.contains("/name")) {
                        int idx;
                        try {
                            String idxStr = action.substring("floors/".length(), action.lastIndexOf("/name"));
                            idx = Integer.parseInt(idxStr) - 1;
                        } catch (Exception e) {
                            respond(t, "{\"error\":\"Invalid floor index\"}", 400);
                            return;
                        }
                        if (method.equals("POST")) {
                            handleRenameFloor(t, em, groupName, idx);
                        } else {
                            t.sendResponseHeaders(405, -1);
                        }
                    } else if (action.equals("floors") && method.equals("POST")) {
                        handleAddFloor(t, em, groupName);
                    } else if (action.startsWith("floors/")) {
                        int idx;
                        try {
                            idx = Integer.parseInt(action.substring("floors/".length())) - 1;
                        } catch (Exception e) {
                            respond(t, "{\"error\":\"Invalid floor index\"}", 400);
                            return;
                        }
                        if (method.equals("DELETE")) {
                            handleDeleteFloor(t, em, groupName, idx);
                        } else {
                            t.sendResponseHeaders(405, -1);
                        }
                    } else {
                        t.sendResponseHeaders(404, -1);
                    }
                } else {
                    String groupName = URLDecoder.decode(rest, "UTF-8");
                    if (method.equals("GET")) {
                        handleGetElevator(t, em, groupName);
                    } else if (method.equals("DELETE")) {
                        handleDeleteElevator(t, em, groupName);
                    } else {
                        t.sendResponseHeaders(405, -1);
                    }
                }
            } else {
                t.sendResponseHeaders(404, -1);
            }
        }

        private void handleListElevators(HttpExchange t, com.shadowfang.core.elevator.ElevatorManager em) throws IOException {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (ElevatorGroup g : em.getAllGroups()) {
                if (!first) json.append(",");
                json.append(groupToJson(g, em));
                first = false;
            }
            json.append("]");
            respond(t, json.toString());
        }

        private void handleGetElevator(HttpExchange t, com.shadowfang.core.elevator.ElevatorManager em, String name) throws IOException {
            ElevatorGroup g = em.getGroup(name);
            if (g == null) {
                respond(t, "{\"error\":\"Group not found\"}", 404);
                return;
            }
            respond(t, groupToJson(g, em));
        }

        private void handleCreateElevator(HttpExchange t, com.shadowfang.core.elevator.ElevatorManager em) throws IOException {
            String body = new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var map = parseJsonMap(body);
            String name = map.get("name");
            if (name == null || name.isEmpty()) {
                respond(t, "{\"error\":\"Missing group name\"}", 400);
                return;
            }
            if (em.createGroup(name)) {
                respond(t, groupToJson(em.getGroup(name), em));
            } else {
                respond(t, "{\"error\":\"Group already exists\"}", 409);
            }
        }

        private void handleDeleteElevator(HttpExchange t, com.shadowfang.core.elevator.ElevatorManager em, String name) throws IOException {
            if (em.removeGroup(name)) {
                respond(t, "{\"success\":true}");
            } else {
                respond(t, "{\"error\":\"Group not found\"}", 404);
            }
        }

        private void handleAddFloor(HttpExchange t, com.shadowfang.core.elevator.ElevatorManager em, String groupName) throws IOException {
            String body = new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var map = parseJsonMap(body);
            String world = map.get("world");
            int x = parseInt(map.get("x"), 0);
            int y = parseInt(map.get("y"), 64);
            int z = parseInt(map.get("z"), 0);
            String displayName = map.get("displayName");
            if (world == null || world.isEmpty()) {
                respond(t, "{\"error\":\"Missing world\"}", 400);
                return;
            }
            boolean ok;
            if (displayName != null && !displayName.isEmpty()) {
                ok = em.addFloor(groupName, world, x, y, z, displayName);
            } else {
                ok = em.addFloor(groupName, world, x, y, z);
            }
            respond(t, ok ? "{\"success\":true}" : "{\"error\":\"Failed to add floor\"}", ok ? 200 : 500);
        }

        private void handleDeleteFloor(HttpExchange t, com.shadowfang.core.elevator.ElevatorManager em, String groupName, int index) throws IOException {
            if (em.removeFloor(groupName, index)) {
                respond(t, "{\"success\":true}");
            } else {
                respond(t, "{\"error\":\"Invalid floor index\"}", 404);
            }
        }

        private void handleRenameFloor(HttpExchange t, com.shadowfang.core.elevator.ElevatorManager em, String groupName, int index) throws IOException {
            String body = new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var map = parseJsonMap(body);
            String newName = map.get("name");
            if (newName == null) {
                respond(t, "{\"error\":\"Missing name\"}", 400);
                return;
            }
            if (em.renameFloor(groupName, index, newName)) {
                respond(t, "{\"success\":true}");
            } else {
                respond(t, "{\"error\":\"Failed to rename floor\"}", 500);
            }
        }

        private String groupToJson(ElevatorGroup g, com.shadowfang.core.elevator.ElevatorManager em) {
            StringBuilder floors = new StringBuilder("[");
            boolean first = true;
            for (int i = 0; i < g.getFloorCount(); i++) {
                var floor = g.getFloor(i);
                if (!first) floors.append(",");
                floors.append("{")
                    .append("\"index\":").append(i + 1).append(",")
                    .append("\"world\":\"").append(escapeJson(floor.getWorld())).append("\",")
                    .append("\"x\":").append(floor.getX()).append(",")
                    .append("\"y\":").append(floor.getY()).append(",")
                    .append("\"z\":").append(floor.getZ()).append(",")
                    .append("\"displayName\":\"").append(escapeJson(floor.getDisplayName() != null ? floor.getDisplayName() : "")).append("\"")
                    .append("}");
                first = false;
            }
            floors.append("]");
            return "{"
                + "\"name\":\"" + escapeJson(g.getName()) + "\","
                + "\"floorCount\":" + g.getFloorCount() + ","
                + "\"floors\":" + floors.toString()
                + "}";
        }

        private int parseInt(String s, int def) {
            try { return s != null ? Integer.parseInt(s) : def; } catch (Exception e) { return def; }
        }

        private java.util.Map<String, String> parseJsonMap(String json) {
            java.util.Map<String, String> map = new java.util.HashMap<>();
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                for (String pair : json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
                    int colon = pair.indexOf(':');
                    if (colon < 0) continue;
                    String key = pair.substring(0, colon).trim().replaceAll("^\"|\"$", "");
                    String val = pair.substring(colon + 1).trim().replaceAll("^\"|\"$", "");
                    map.put(key, val);
                }
            }
            return map;
        }

        private String escapeJson(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }

        private void respond(HttpExchange t, String json) throws IOException { respond(t, json, 200); }
        private void respond(HttpExchange t, String json, int code) throws IOException {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
        }
    }

}
