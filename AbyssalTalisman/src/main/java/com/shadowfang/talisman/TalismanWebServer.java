package com.shadowfang.talisman;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class TalismanWebServer {

    private static final int PORT = 56553;
    private HttpServer server;
    private final AbyssalTalismanPlugin plugin;

    public TalismanWebServer(AbyssalTalismanPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new GuideHandler(plugin));
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("Talisman Guide web server started on port " + PORT);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to start web server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Talisman Guide web server stopped.");
        }
    }

    public int getPort() {
        return PORT;
    }

    private static class GuideHandler implements HttpHandler {
        private final AbyssalTalismanPlugin plugin;

        public GuideHandler(AbyssalTalismanPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = getHtmlContent();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getHtmlContent() {
            return "<!DOCTYPE html>\n" +
"<html lang=\"en\">\n" +
"<head>\n" +
"  <meta charset=\"UTF-8\">\n" +
"  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
"  <title>Abyssal Talisman Guide — Shadowfang Reclaimed</title>\n" +
"  <style>\n" +
"    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
"    body {\n" +
"      font-family: 'Segoe UI', Minecraft, monospace;\n" +
"      background: #0a0a0f;\n" +
"      color: #d4c4a8;\n" +
"      min-height: 100vh;\n" +
"    }\n" +
"    @font-face {\n" +
"      font-family: 'Minecraft';\n" +
"      src: url('https://fonts.cdnfonts.com/s/25936/Minecraft.woff') format('woff');\n" +
"    }\n" +
"    a { color: #c8a84b; text-decoration: none; }\n" +
"    a:hover { color: #e8c86b; }\n" +
"\n" +
"    /* NAV */\n" +
"    nav {\n" +
"      position: fixed;\n" +
"      top: 0; left: 0; right: 0;\n" +
"      background: #111118;\n" +
"      border-bottom: 2px solid #3a2a0a;\n" +
"      padding: 0 2rem;\n" +
"      z-index: 100;\n" +
"      display: flex;\n" +
"      align-items: center;\n" +
"      height: 56px;\n" +
"    }\n" +
"    nav .logo {\n" +
"      font-family: 'Minecraft', monospace;\n" +
"      color: #c8a84b;\n" +
"      font-size: 1.1rem;\n" +
"      font-weight: bold;\n" +
"      text-shadow: 2px 2px 0 #000;\n" +
"      letter-spacing: 1px;\n" +
"    }\n" +
"    nav .logo span { color: #e84040; }\n" +
"    nav ul {\n" +
"      list-style: none;\n" +
"      display: flex;\n" +
"      gap: 0.25rem;\n" +
"      margin-left: 2rem;\n" +
"    }\n" +
"    nav ul li a {\n" +
"      display: block;\n" +
"      padding: 0.5rem 1rem;\n" +
"      color: #a09080;\n" +
"      font-size: 0.85rem;\n" +
"      border-radius: 4px;\n" +
"      transition: all 0.2s;\n" +
"    }\n" +
"    nav ul li a:hover, nav ul li a.active {\n" +
"      background: #1e1a0f;\n" +
"      color: #c8a84b;\n" +
"    }\n" +
"    nav .server-tag {\n" +
"      margin-left: auto;\n" +
"      font-size: 0.75rem;\n" +
"      color: #606060;\n" +
"      background: #1a1a22;\n" +
"      padding: 0.25rem 0.75rem;\n" +
"      border-radius: 4px;\n" +
"      border: 1px solid #2a2a3a;\n" +
"    }\n" +
"\n" +
"    /* HERO */\n" +
"    .hero {\n" +
"      padding: 6rem 2rem 4rem;\n" +
"      text-align: center;\n" +
"      background: linear-gradient(180deg, #0f0f18 0%, #0a0a0f 100%);\n" +
"      border-bottom: 1px solid #2a1a0a;\n" +
"    }\n" +
"    .hero h1 {\n" +
"      font-family: 'Minecraft', monospace;\n" +
"      font-size: 2.5rem;\n" +
"      color: #c8a84b;\n" +
"      text-shadow: 3px 3px 0 #000, 0 0 20px rgba(200,168,75,0.3);\n" +
"      margin-bottom: 0.5rem;\n" +
"    }\n" +
"    .hero .subtitle {\n" +
"      color: #8a7a60;\n" +
"      font-size: 1rem;\n" +
"      font-style: italic;\n" +
"      margin-bottom: 2rem;\n" +
"    }\n" +
"    .hero .tagline {\n" +
"      color: #e84040;\n" +
"      font-size: 0.85rem;\n" +
"      letter-spacing: 2px;\n" +
"      text-transform: uppercase;\n" +
"    }\n" +
"    .hero .blaze-rod {\n" +
"      font-size: 4rem;\n" +
"      margin: 1rem 0;\n" +
"      filter: drop-shadow(0 0 20px rgba(255,100,0,0.6));\n" +
"    }\n" +
"\n" +
"    /* LAYOUT */\n" +
"    .container {\n" +
"      max-width: 900px;\n" +
"      margin: 0 auto;\n" +
"      padding: 2rem;\n" +
"    }\n" +
"\n" +
"    /* SECTIONS */\n" +
"    section {\n" +
"      margin-bottom: 4rem;\n" +
"      scroll-margin-top: 70px;\n" +
"    }\n" +
"    section h2 {\n" +
"      font-family: 'Minecraft', monospace;\n" +
"      font-size: 1.4rem;\n" +
"      color: #c8a84b;\n" +
"      border-left: 4px solid #c8a84b;\n" +
"      padding-left: 0.75rem;\n" +
"      margin-bottom: 1.5rem;\n" +
"      text-shadow: 1px 1px 0 #000;\n" +
"    }\n" +
"    section h3 {\n" +
"      color: #b8a080;\n" +
"      font-size: 1rem;\n" +
"      margin: 1.25rem 0 0.5rem;\n" +
"    }\n" +
"    section p {\n" +
"      color: #a09080;\n" +
"      line-height: 1.7;\n" +
"      margin-bottom: 1rem;\n" +
"    }\n" +
"    section ul {\n" +
"      list-style: none;\n" +
"      margin-bottom: 1rem;\n" +
"    }\n" +
"    section ul li {\n" +
"      color: #a09080;\n" +
"      padding: 0.3rem 0 0.3rem 1.5rem;\n" +
"      position: relative;\n" +
"      line-height: 1.5;\n" +
"    }\n" +
"    section ul li::before {\n" +
"      content: '▸';\n" +
"      color: #c8a84b;\n" +
"      position: absolute;\n" +
"      left: 0;\n" +
"    }\n" +
"\n" +
"    /* INFO BOX */\n" +
"    .info-box {\n" +
"      background: #12120f;\n" +
"      border: 1px solid #3a2a0a;\n" +
"      border-radius: 6px;\n" +
"      padding: 1.25rem;\n" +
"      margin-bottom: 1rem;\n" +
"    }\n" +
"    .info-box.warning {\n" +
"      border-color: #6a3a0a;\n" +
"      background: #1a1205;\n" +
"    }\n" +
"    .info-box.warning .box-title { color: #e88040; }\n" +
"    .info-box.danger {\n" +
"      border-color: #6a1a1a;\n" +
"      background: #1a0505;\n" +
"    }\n" +
"    .info-box.danger .box-title { color: #e84040; }\n" +
"    .info-box.success {\n" +
"      border-color: #1a4a1a;\n" +
"      background: #051a05;\n" +
"    }\n" +
"    .info-box.success .box-title { color: #50c850; }\n" +
"    .box-title {\n" +
"      font-weight: bold;\n" +
"      margin-bottom: 0.5rem;\n" +
"      font-size: 0.9rem;\n" +
"    }\n" +
"\n" +
"    /* CODE / COMMAND */\n" +
"    code, .cmd {\n" +
"      font-family: 'Cascadia Code', 'Consolas', monospace;\n" +
"      background: #1a1a22;\n" +
"      color: #c8a84b;\n" +
"      padding: 0.15rem 0.4rem;\n" +
"      border-radius: 3px;\n" +
"      font-size: 0.85em;\n" +
"    }\n" +
"    .command-block {\n" +
"      background: #0f0f18;\n" +
"      border: 1px solid #3a3a5a;\n" +
"      border-radius: 6px;\n" +
"      padding: 1rem;\n" +
"      margin-bottom: 1rem;\n" +
"      font-family: 'Cascadia Code', monospace;\n" +
"      font-size: 0.9rem;\n" +
"    }\n" +
"    .command-block .prompt { color: #50c850; }\n" +
"    .command-block .cmd-text { color: #c8a84b; }\n" +
"    .command-block .comment { color: #606080; font-style: italic; }\n" +
"\n" +
"    /* MODE CARDS */\n" +
"    .mode-grid {\n" +
"      display: grid;\n" +
"      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));\n" +
"      gap: 1rem;\n" +
"      margin-bottom: 1.5rem;\n" +
"    }\n" +
"    .mode-card {\n" +
"      background: #12120f;\n" +
"      border: 1px solid #2a2a1a;\n" +
"      border-radius: 6px;\n" +
"      padding: 1rem;\n" +
"      transition: border-color 0.2s;\n" +
"    }\n" +
"    .mode-card:hover { border-color: #c8a84b; }\n" +
"    .mode-card .mode-name {\n" +
"      font-family: 'Minecraft', monospace;\n" +
"      color: #c8a84b;\n" +
"      font-size: 1rem;\n" +
"      margin-bottom: 0.5rem;\n" +
"      text-shadow: 1px 1px 0 #000;\n" +
"    }\n" +
"    .mode-card .mode-power {\n" +
"      color: #d4c4a8;\n" +
"      font-size: 0.85rem;\n" +
"      margin-bottom: 0.5rem;\n" +
"      line-height: 1.4;\n" +
"    }\n" +
"    .mode-card .mode-curse {\n" +
"      color: #e84040;\n" +
"      font-size: 0.8rem;\n" +
"      font-style: italic;\n" +
"      opacity: 0.8;\n" +
"    }\n" +
"\n" +
"    /* ITEM DISPLAY */\n" +
"    .item-display {\n" +
"      display: flex;\n" +
"      align-items: center;\n" +
"      gap: 1rem;\n" +
"      background: #12120f;\n" +
"      border: 1px solid #2a2a1a;\n" +
"      border-radius: 6px;\n" +
"      padding: 1rem;\n" +
"      margin-bottom: 1rem;\n" +
"    }\n" +
"    .item-icon {\n" +
"      font-size: 3rem;\n" +
"      width: 64px;\n" +
"      text-align: center;\n" +
"      filter: drop-shadow(0 0 8px rgba(200,168,75,0.4));\n" +
"    }\n" +
"    .item-info .item-name {\n" +
"      font-family: 'Minecraft', monospace;\n" +
"      font-size: 1.1rem;\n" +
"      color: #c8a84b;\n" +
"      text-shadow: 1px 1px 0 #000;\n" +
"      margin-bottom: 0.25rem;\n" +
"    }\n" +
"    .item-info .item-sub { color: #8a7a60; font-size: 0.8rem; font-style: italic; }\n" +
"    .item-info .item-desc { color: #a09080; font-size: 0.85rem; margin-top: 0.5rem; line-height: 1.5; }\n" +
"\n" +
"    /* LORE FRAGMENTS */\n" +
"    .fragment-list {\n" +
"      display: grid;\n" +
"      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
"      gap: 0.75rem;\n" +
"      margin-bottom: 1.5rem;\n" +
"    }\n" +
"    .fragment-card {\n" +
"      background: #0f0f18;\n" +
"      border: 1px solid #2a2040;\n" +
"      border-radius: 4px;\n" +
"      padding: 0.75rem;\n" +
"    }\n" +
"    .fragment-card .frag-num { color: #8080c0; font-size: 0.7rem; text-transform: uppercase; letter-spacing: 1px; }\n" +
"    .fragment-card .frag-title { color: #b0a0d0; font-size: 0.9rem; font-weight: bold; margin: 0.25rem 0; }\n" +
"    .fragment-card .frag-content { color: #706080; font-size: 0.75rem; font-style: italic; line-height: 1.4; }\n" +
"\n" +
"    /* STEPS */\n" +
"    .steps {\n" +
"      counter-reset: steps;\n" +
"    }\n" +
"    .step {\n" +
"      display: flex;\n" +
"      gap: 1rem;\n" +
"      margin-bottom: 1rem;\n" +
"      align-items: flex-start;\n" +
"    }\n" +
"    .step-num {\n" +
"      counter-increment: steps;\n" +
"      content: counter(steps);\n" +
"      width: 28px;\n" +
"      height: 28px;\n" +
"      background: #1e1a0f;\n" +
"      border: 2px solid #c8a84b;\n" +
"      border-radius: 50%;\n" +
"      display: flex;\n" +
"      align-items: center;\n" +
"      justify-content: center;\n" +
"      font-size: 0.8rem;\n" +
"      color: #c8a84b;\n" +
"      font-weight: bold;\n" +
"      flex-shrink: 0;\n" +
"    }\n" +
"    .step-content { flex: 1; }\n" +
"    .step-content p { margin: 0; }\n" +
"\n" +
"    /* CURSED CALLOUT */\n" +
"    .cursed-box {\n" +
"      background: #1a0505;\n" +
"      border: 2px solid #6a1a1a;\n" +
"      border-radius: 6px;\n" +
"      padding: 1rem;\n" +
"      margin-top: 1.5rem;\n" +
"    }\n" +
"    .cursed-box h3 { color: #e84040; margin-top: 0; }\n" +
"    .cursed-box p { color: #c07070; }\n" +
"\n" +
"    /* FOOTER */\n" +
"    footer {\n" +
"      text-align: center;\n" +
"      padding: 2rem;\n" +
"      border-top: 1px solid #1a1a2a;\n" +
"      color: #404050;\n" +
"      font-size: 0.75rem;\n" +
"      margin-top: 4rem;\n" +
"    }\n" +
"    footer span { color: #c8a84b; }\n" +
"\n" +
"    /* RESPONSIVE */\n" +
"    @media (max-width: 600px) {\n" +
"      nav ul { display: none; }\n" +
"      .hero h1 { font-size: 1.8rem; }\n" +
"      .container { padding: 1rem; }\n" +
"    }\n" +
"  </style>\n" +
"</head>\n" +
"<body>\n" +
"\n" +
"<!-- NAV -->\n" +
"<nav>\n" +
"  <div class=\"logo\">&#9670; <span>Shadow</span>fang Reclaimed</div>\n" +
"  <ul>\n" +
"    <li><a href=\"#home\" class=\"active\">Home</a></li>\n" +
"    <li><a href=\"#lore\">Lore</a></li>\n" +
"    <li><a href=\"#talisman\">Talisman</a></li>\n" +
"    <li><a href=\"#modes\">Modes</a></li>\n" +
"    <li><a href=\"#curses\">Curses</a></li>\n" +
"    <li><a href=\"#vault\">Vault</a></li>\n" +
"    <li><a href=\"#commands\">Commands</a></li>\n" +
"  </ul>\n" +
"  <div class=\"server-tag\">Shadowfang Reclaimed</div>\n" +
"</nav>\n" +
"\n" +
"<!-- HERO -->\n" +
"<div class=\"hero\" id=\"home\">\n" +
"  <div class=\"tagline\">Ancient Power · Hidden Curses</div>\n" +
"  <h1>Abyssal Talisman</h1>\n" +
"  <div class=\"subtitle\">The deep calls back. Every gift demands a price.</div>\n" +
"  <div class=\"blaze-rod\">&#128293;</div>\n" +
"  <p style=\"color:#606080; font-size:0.8rem;\">Blaze Rod · Abyssal Artifact · Unbreakable</p>\n" +
"</div>\n" +
"\n" +
"<div class=\"container\">\n" +
"\n" +
"<!-- WHAT IS IT -->\n" +
"<section id=\"talisman\">\n" +
"  <h2>What is the Abyssal Talisman?</h2>\n" +
"  <div class=\"item-display\">\n" +
"    <div class=\"item-icon\">&#128293;</div>\n" +
"    <div class=\"item-info\">\n" +
"      <div class=\"item-name\">Abyssal Talisman</div>\n" +
"      <div class=\"item-sub\">Blaze Rod · Artifact of the Deep</div>\n" +
"      <div class=\"item-desc\">An ancient artifact forged from the depths of the abyss. It grants its wielder the power to channel the earth itself — but the abyss always collects its due. The talisman cannot be dropped, traded, or stored in ordinary chests. It exists only in your off-hand or your personal vault.</div>\n" +
"    </div>\n" +
"  </div>\n" +
"  <p>The Abyssal Talisman is a powerful mining artifact that offers three distinct modes of operation. Hold it in your off-hand to activate its power. Sneak + right-click to cycle between modes. Each mode comes with a benefit and a curse — you cannot have one without the other.</p>\n" +
"  <div class=\"info-box warning\">\n" +
"    <div class=\"box-title\">&#9888; Soulbound</div>\n" +
"    <p style=\"margin:0; color:#c07050;\">The talisman is bound to your soul. On death, it returns to you. It cannot be dropped on the ground or given to other players. The only way to store it is in your personal vault.</p>\n" +
"  </div>\n" +
"</section>\n" +
"\n" +
"<!-- LORE FRAGMENTS -->\n" +
"<section id=\"lore\">\n" +
"  <h2>Lore Fragments — The Path to Power</h2>\n" +
"  <p>The talisman cannot simply be crafted or purchased. It must be <em>earned</em> by collecting the lost knowledge of Shadowfang — five ancient fragments scattered across the realm. Each fragment is a written book containing fragments of a darker story.</p>\n" +
"  <p>When you have collected all five fragments, use the <code>/talisman bind</code> command to attune yourself to the talisman. The fragments are consumed in the process. There is a chance — small, but real — that something darker awakens instead.</p>\n" +
"\n" +
"  <div class=\"fragment-list\">\n" +
"    <div class=\"fragment-card\">\n" +
"      <div class=\"frag-num\">Fragment I</div>\n" +
"      <div class=\"frag-title\">A Bloody Moon</div>\n" +
"      <div class=\"frag-content\">\"The moon was red that night. I remember the howling...\"</div>\n" +
"    </div>\n" +
"    <div class=\"fragment-card\">\n" +
"      <div class=\"frag-num\">Fragment II</div>\n" +
"      <div class=\"frag-title\">Silver Scars</div>\n" +
"      <div class=\"frag-content\">\"They told us silver would burn them. They didn't say it would make them angrier...\"</div>\n" +
"    </div>\n" +
"    <div class=\"fragment-card\">\n" +
"      <div class=\"frag-num\">Fragment III</div>\n" +
"      <div class=\"frag-title\">The Last Stand</div>\n" +
"      <div class=\"frag-content\">\"We fortified the gates. It wasn't enough. They came from the shadows...\"</div>\n" +
"    </div>\n" +
"    <div class=\"fragment-card\">\n" +
"      <div class=\"frag-num\">Fragment IV</div>\n" +
"      <div class=\"frag-title\">Notes on the Warden</div>\n" +
"      <div class=\"frag-content\">\"He watches. Do not anger the trees. Do not speak his name...\"</div>\n" +
"    </div>\n" +
"    <div class=\"fragment-card\">\n" +
"      <div class=\"frag-num\">Fragment V</div>\n" +
"      <div class=\"frag-title\">Torn Journal Entry</div>\n" +
"      <div class=\"frag-content\">\"Day 43. The cold is unbearable. The pack outside is circling...\"</div>\n" +
"    </div>\n" +
"  </div>\n" +
"\n" +
"  <div class=\"cursed-box\">\n" +
"    <h3>&#9760; The Cursed Variant</h3>\n" +
"    <p>When binding a talisman, you have a <strong>10% chance</strong> to receive the Cursed Abyssal Talisman instead. It looks the same — but the whispers are louder. The powers are stronger, but the curses are worse. You cannot tell until you use it.</p>\n" +
"  </div>\n" +
"</section>\n" +
"\n" +
"<!-- MODES -->\n" +
"<section id=\"modes\">\n" +
"  <h2>The Three Modes</h2>\n" +
"  <p>Sneak + right-click while holding the talisman in your off-hand to cycle modes. Each mode is active at all times while in off-hand — but the curse is always present too.</p>\n" +
"\n" +
"  <div class=\"mode-grid\">\n" +
"    <div class=\"mode-card\">\n" +
"      <div class=\"mode-name\">&#9670; Vein</div>\n" +
"      <div class=\"mode-power\"><strong>Power:</strong> Break an ore or log block and chain-mine all connected blocks of the same type within 30 blocks.</div>\n" +
"      <div class=\"mode-curse\">&#9888; Curse: Hunger III for 4 seconds after each vein break. (Hunger IV for 6s if Cursed.)</div>\n" +
"    </div>\n" +
"    <div class=\"mode-card\">\n" +
"      <div class=\"mode-name\">&#9670; Excavate</div>\n" +
"      <div class=\"mode-power\"><strong>Power:</strong> Break a soft block (dirt, sand, gravel, clay) and mine the entire 3×3 face of soft blocks from the direction you're looking. (5×5 if Cursed.)</div>\n" +
"      <div class=\"mode-curse\">&#9888; Curse: Mining Fatigue II for 3 seconds after use. (Fatigue III for 5s if Cursed.)</div>\n" +
"    </div>\n" +
"    <div class=\"mode-card\">\n" +
"      <div class=\"mode-name\">&#9670; Prospect</div>\n" +
"      <div class=\"mode-power\"><strong>Power:</strong> Break any block to pulse — ores within 12 blocks glow through stone for 5 seconds. 45 second cooldown. (20 blocks if Cursed.)</div>\n" +
"      <div class=\"mode-curse\">&#9888; Curse: Night Vision is suppressed while the talisman is held in off-hand. (Blindness flicker if Cursed.)</div>\n" +
"    </div>\n" +
"  </div>\n" +
"\n" +
"  <div class=\"info-box\">\n" +
"    <div class=\"box-title\">Switching Modes</div>\n" +
"    <p style=\"margin:0; color:#a09080;\">Use <code>/ta</code> (no args) to cycle modes without breaking a block. Or sneak + right-click on any surface. Each mode shows its active curse in yellow when selected.</p>\n" +
"  </div>\n" +
"</section>\n" +
"\n" +
"<!-- CURSES -->\n" +
"<section id=\"curses\">\n" +
"  <h2>The Cost of Power</h2>\n" +
"  <p>Every mode of the talisman comes with a curse — a price paid for the power. The curse is active as long as the talisman is in your off-hand. Choose your mode wisely based on what you're doing.</p>\n" +
"\n" +
"  <div class=\"info-box danger\">\n" +
"    <div class=\"box-title\">&#9888; Curse of Hunger (Vein)</div>\n" +
"    <p style=\"margin:0; color:#c07070;\">Breaking a vein chain causes intense hunger. Bring food when mining large ore deposits. The Cursed variant lasts longer and is more severe.</p>\n" +
"  </div>\n" +
"\n" +
"  <div class=\"info-box danger\">\n" +
"    <div class=\"box-title\">&#9888; Curse of Exhaustion (Excavate)</div>\n" +
"    <p style=\"margin:0; color:#c07070;\">After clearing a large area, your hands tremble with Mining Fatigue. Wait it out or eat Golden Apples to counteract. The Cursed variant lasts even longer.</p>\n" +
"  </div>\n" +
"\n" +
"  <div class=\"info-box danger\">\n" +
"    <div class=\"box-title\">&#9888; Curse of Darkness (Prospect)</div>\n" +
"    <p style=\"margin:0; color:#c07070;\">The talisman suppresses your Night Vision while held. If you're mining in darkness and rely on Night Vision potions, you won't be able to see. The Cursed variant causes Blindness flickers.</p>\n" +
"  </div>\n" +
"\n" +
"  <div class=\"info-box warning\">\n" +
"    <div class=\"box-title\">&#9752; Strategic Tips</div>\n" +
"    <ul style=\"margin:0; color:#a09080;\">\n" +
"      <li>Vein mode: Eat before mining, bring stack of bread or golden apples</li>\n" +
"      <li>Excavate mode: Plan your mining session — don't start a large dig when you need to escape quickly</li>\n" +
"      <li>Prospect mode: Switch to Vein or Excavate after finding ore veins — don't prospect continuously</li>\n" +
"      <li>When cursed: The powers are stronger but the curses are significantly worse — assess if it's worth it</li>\n" +
"    </ul>\n" +
"  </div>\n" +
"</section>\n" +
"\n" +
"<!-- HOW TO GET -->\n" +
"<section id=\"obtain\">\n" +
"  <h2>How to Obtain</h2>\n" +
"  <div class=\"steps\">\n" +
"    <div class=\"step\">\n" +
"      <div class=\"step-num\">1</div>\n" +
"      <div class=\"step-content\">\n" +
"        <p><strong>Find the 5 Lost Fragments</strong> — Scattered across the world as loot, mob drops, or hidden in structures. Each is a written book with PDC tag. Check all containers, especially in dungeons and strongholds.</p>\n" +
"      </div>\n" +
"    </div>\n" +
"    <div class=\"step\">\n" +
"      <div class=\"step-num\">2</div>\n" +
"      <div class=\"step-content\">\n" +
"        <p><strong>Hold a Blaze Rod</strong> — The Blaze Rod is the vessel. Have one in your main hand when binding. (You don't need it if using <code>/talisman wand</code>.)</p>\n" +
"      </div>\n" +
"    </div>\n" +
"    <div class=\"step\">\n" +
"      <div class=\"step-num\">3</div>\n" +
"      <div class=\"step-content\">\n" +
"        <p><strong>Execute the Ritual</strong></p>\n" +
"        <div class=\"command-block\">\n" +
"          <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman bind</span>\n" +
"          <br><span class=\"comment\">// Consumes 5 fragments. 10% chance of Cursed variant.</span>\n" +
"        </div>\n" +
"      </div>\n" +
"    </div>\n" +
"    <div class=\"step\">\n" +
"      <div class=\"step-num\">4</div>\n" +
"      <div class=\"step-content\">\n" +
"        <p><strong>Equip in Off-Hand</strong> — The talisman appears in your off-hand automatically. Hold it there to activate its power.</p>\n" +
"      </div>\n" +
"    </div>\n" +
"  </div>\n" +
"\n" +
"  <div class=\"info-box success\">\n" +
"    <div class=\"box-title\">Admin Command</div>\n" +
"    <p style=\"margin:0; color:#70c070;\">Admins can skip the ritual entirely: <code>/talisman wand</code> — grants an Abyssal Talisman (normal, not cursed) instantly.</p>\n" +
"  </div>\n" +
"</section>\n" +
"\n" +
"<!-- VAULT -->\n" +
"<section id=\"vault\">\n" +
"  <h2>The Talisman Vault</h2>\n" +
"  <p>Talismans cannot be stored in regular chests or Ender chests. They can only exist in two places: your <strong>off-hand</strong> (active) or your <strong>personal vault</strong> (inactive).</p>\n" +
"  <p>Your vault is private, soulbound, and persists across server restarts. No one else can access it.</p>\n" +
"\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman vault</span>\n" +
"    <br><span class=\"comment\">// Opens your personal talisman vault (27-slot chest)</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/ta v</span>\n" +
"    <br><span class=\"comment\">// Shortcut to open vault</span>\n" +
"  </div>\n" +
"\n" +
"  <h3>Vault Mechanics</h3>\n" +
"  <ul>\n" +
"    <li><strong>Click a talisman in vault</strong> → moves it to your off-hand (equips it)</li>\n" +
"    <li><strong>Click a talisman in off-hand</strong> while vault is open → stores it in vault</li>\n" +
"    <li><strong>Cannot</strong> drag talismans into regular chests, furnaces, crafting tables, or any container</li>\n" +
"    <li><strong>Cannot</strong> drop talismans on the ground</li>\n" +
"    <li><strong>Cannot</strong> trade talismans to other players</li>\n" +
"    <li><strong>On death</strong> — talisman stays in your inventory, returns on respawn</li>\n" +
"  </ul>\n" +
"</section>\n" +
"\n" +
"<!-- COMMANDS -->\n" +
"<section id=\"commands\">\n" +
"  <h2>Command Reference</h2>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman</span>\n" +
"    <br><span class=\"comment\">// Show help menu</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman bind</span>\n" +
"    <br><span class=\"comment\">// Bind a talisman using 5 lore fragments</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman wand</span>\n" +
"    <br><span class=\"comment\">// Admin: receive an Abyssal Talisman instantly</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman vault</span>\n" +
"    <br><span class=\"comment\">// Open your personal talisman vault</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman toggle</span>\n" +
"    <br><span class=\"comment\">// Cycle to next talisman mode</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/talisman info</span>\n" +
"    <br><span class=\"comment\">// View current talisman type, mode, and curse details</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/ta</span>\n" +
"    <br><span class=\"comment\">// Quick cycle mode (if holding talisman) or open vault (if not)</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/ta v</span>\n" +
"    <br><span class=\"comment\">// Shortcut: open vault</span>\n" +
"  </div>\n" +
"  <div class=\"command-block\">\n" +
"    <span class=\"prompt\">></span> <span class=\"cmd-text\">/ta wand</span>\n" +
"    <br><span class=\"comment\">// Shortcut: admin get wand</span>\n" +
"  </div>\n" +
"\n" +
"  <h3>Permissions</h3>\n" +
"  <ul>\n" +
"    <li><code>shadowfang.talisman</code> — Use talisman commands (default: op)</li>\n" +
"    <li><code>shadowfang.talisman.wand</code> — Receive a talisman via /talisman wand (default: op)</li>\n" +
"    <li><code>shadowfang.talisman.bind</code> — Use /talisman bind to create a talisman from fragments (default: op)</li>\n" +
"  </ul>\n" +
"  <p style=\"font-size:0.8rem; color:#606080; margin-top:1rem;\">Note: The talisman's power works for any player who holds it, regardless of permissions. The commands are for obtaining, storing, and mode-switching.</p>\n" +
"</section>\n" +
"\n" +
"<!-- FOOTER -->\n" +
"<footer>\n" +
"  <p><span>Abyssal Talisman</span> — Shadowfang Reclaimed &copy; 2026</p>\n" +
"  <p style=\"margin-top:0.5rem; color:#303040;\">Every gift demands a price.</p>\n" +
"</footer>\n" +
"\n" +
"</div>\n" +
"\n" +
"<script>\n" +
"  // Smooth scroll for nav links\n" +
"  document.querySelectorAll('nav a[href^=\"#\"]').forEach(anchor => {\n" +
"    anchor.addEventListener('click', function(e) {\n" +
"      e.preventDefault();\n" +
"      const target = document.querySelector(this.getAttribute('href'));\n" +
"      if (target) {\n" +
"        target.scrollIntoView({ behavior: 'smooth', block: 'start' });\n" +
"        document.querySelectorAll('nav a').forEach(a => a.classList.remove('active'));\n" +
"        this.classList.add('active');\n" +
"      }\n" +
"    });\n" +
"  });\n" +
"\n" +
"  // Highlight nav item on scroll\n" +
"  const sections = document.querySelectorAll('section[id]');\n" +
"  window.addEventListener('scroll', () => {\n" +
"    let current = '';\n" +
"    sections.forEach(section => {\n" +
"      const sectionTop = section.offsetTop - 80;\n" +
"      if (scrollY >= sectionTop) current = section.getAttribute('id');\n" +
"    });\n" +
"    document.querySelectorAll('nav a').forEach(a => {\n" +
"      a.classList.remove('active');\n" +
"      if (a.getAttribute('href') === '#' + current) a.classList.add('active');\n" +
"    });\n" +
"  });\n" +
"</script>\n" +
"\n" +
"</body>\n" +
"</html>\n";
        }
    }
}
