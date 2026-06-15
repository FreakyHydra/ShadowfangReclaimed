package com.shadowfang.core.terminal;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

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
}
