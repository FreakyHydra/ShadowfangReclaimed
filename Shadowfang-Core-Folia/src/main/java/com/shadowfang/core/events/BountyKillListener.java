package com.shadowfang.core.events;

import com.shadowfang.core.ShadowfangCorePlugin;
import com.shadowfang.core.bounty.BountyManager;
import com.shadowfang.core.economy.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class BountyKillListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        BountyManager bountyManager = ShadowfangCorePlugin.getInstance().getBountyManager();
        BountyManager.BountyTask task = bountyManager.getBounty(killer.getUniqueId());

        if (task != null && task.target == event.getEntityType()) {
            task.currentKills++;
            
            if (task.isComplete()) {
                EconomyManager eco = ShadowfangCorePlugin.getInstance().getEconomyManager();
                eco.addBalance(killer.getUniqueId(), task.reward);
                
                bountyManager.clearBounty(killer.getUniqueId());
                
                killer.sendMessage("§aBounty Complete! §7You have been awarded §f" + task.reward + " Silver Coins.");
            } else {
                killer.sendMessage("§7Bounty Progress: §f" + task.currentKills + " / " + task.required + " " + task.target.name() + "s.");
            }
        }
    }
}
