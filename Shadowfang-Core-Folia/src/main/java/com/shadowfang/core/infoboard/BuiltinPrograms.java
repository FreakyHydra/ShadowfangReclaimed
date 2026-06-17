package com.shadowfang.core.infoboard;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.economy.EconomyManager;
import com.shadowfang.core.faction.Faction;
import com.shadowfang.core.faction.FactionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BuiltinPrograms {

    private final ShadowfangCorePlugin plugin;

    public BuiltinPrograms(ShadowfangCorePlugin plugin) {
        this.plugin = plugin;
    }

    public Program players() {
        return new Program() {
            @Override
            public String getName() { return "players"; }

            @Override
            public List<String> render(InfoBoard board) {
                Collection<? extends Player> online = Bukkit.getOnlinePlayers();
                int count = online.size();
                int max = Bukkit.getMaxPlayers();
                List<String> lines = new ArrayList<>();
                lines.add("§b§lPLAYERS ONLINE");
                lines.add("§7━━━━━━━━━━━━━━━━━━━");
                lines.add("§e" + count + "§7/§e" + max + " §7online");
                lines.add("");
                if (count > 0) {
                    List<String> names = online.stream()
                            .map(Player::getName)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList());
                    StringBuilder row = new StringBuilder("§f");
                    for (int i = 0; i < names.size(); i++) {
                        if (i > 0) row.append(" §7| §f");
                        row.append(names.get(i));
                        if (row.length() > 50) {
                            lines.add(row.toString());
                            row = new StringBuilder("§f");
                        }
                    }
                    if (row.length() > 2) lines.add(row.toString());
                } else {
                    lines.add("§7No players online");
                }
                return lines;
            }
        };
    }

    public Program factions() {
        return new Program() {
            @Override
            public String getName() { return "factions"; }

            @Override
            public List<String> render(InfoBoard board) {
                FactionManager fm = plugin.getFactionManager();
                EconomyManager em = plugin.getEconomyManager();
                List<String> lines = new ArrayList<>();
                lines.add("§6§lFACTION LEADERBOARD");
                lines.add("§7━━━━━━━━━━━━━━━━━━━");
                if (fm == null) {
                    lines.add("§cSystem unavailable");
                    return lines;
                }
                Collection<Faction> all = fm.getAllFactions();
                if (all.isEmpty()) {
                    lines.add("§7No factions yet");
                    return lines;
                }
                List<Faction> sorted = new ArrayList<>(all);
                sorted.sort((a, b) -> Double.compare(b.getHoardBalance(), a.getHoardBalance()));
                int rank = 1;
                for (Faction f : sorted) {
                    if (rank > 10) break;
                    String prefix = rank == 1 ? "§e#1 " : rank == 2 ? "§7#2 " : rank == 3 ? "§6#3 " : "§f#" + rank + " ";
                    lines.add(prefix + "§b" + f.getName() + " §7- §e$" + String.format("%.0f", f.getHoardBalance()));
                    rank++;
                }
                return lines;
            }
        };
    }

    public Program server() {
        return new Program() {
            @Override
            public String getName() { return "server"; }

            @Override
            public List<String> render(InfoBoard board) {
                List<String> lines = new ArrayList<>();
                lines.add("§a§lSERVER STATUS");
                lines.add("§7━━━━━━━━━━━━━━━━━━━");
                double tps = Bukkit.getTPS()[0];
                String tpsColor = tps > 18.0 ? "§a" : tps > 15.0 ? "§e" : "§c";
                lines.add("§fTPS: " + tpsColor + String.format("%.1f", tps));
                Runtime rt = Runtime.getRuntime();
                long used = (rt.totalMemory() - rt.freeMemory()) / 1048576;
                long max = rt.maxMemory() / 1048576;
                lines.add("§fRAM: §b" + used + "§7/§b" + max + " MB");
                long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
                long days = uptimeMs / 86400000;
                long hours = (uptimeMs % 86400000) / 3600000;
                lines.add("§fUptime: §b" + days + "d " + hours + "h");
                lines.add("§fVersion: §7" + Bukkit.getBukkitVersion());
                lines.add("");
                lines.add("§8" + Bukkit.getMotd().replace("§", ""));
                return lines;
            }
        };
    }

    public Program clock() {
        return new Program() {
            @Override
            public String getName() { return "clock"; }

            @Override
            public List<String> render(InfoBoard board) {
                List<String> lines = new ArrayList<>();
                lines.add("§d§lREALM CLOCK");
                lines.add("§7━━━━━━━━━━━━━━━━━━━");
                LocalDateTime now = LocalDateTime.now();
                lines.add("§f" + now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
                lines.add("");
                lines.add("§5" + now.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                return lines;
            }
        };
    }

    public Program motd() {
        return new Program() {
            @Override
            public String getName() { return "motd"; }

            @Override
            public List<String> render(InfoBoard board) {
                List<String> lines = new ArrayList<>();
                String motd = Bukkit.getMotd();
                if (motd == null || motd.isEmpty()) {
                    lines.add("§7Welcome to Shadowfang Reclaimed");
                } else {
                    String[] parts = motd.split("\\\\n");
                    for (String line : parts) {
                        lines.add(line);
                    }
                }
                return lines;
            }
        };
    }

    public List<Program> all() {
        return Arrays.asList(players(), factions(), server(), clock(), motd());
    }
}
