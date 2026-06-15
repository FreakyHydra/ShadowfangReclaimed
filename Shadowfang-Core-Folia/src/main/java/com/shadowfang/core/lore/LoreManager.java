package com.shadowfang.core.lore;

import com.shadowfang.core.ShadowfangCorePlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class LoreManager {

    private Map<Integer, LoreFragment> fragments = new HashMap<>();

    public LoreManager() {
    }

    public void load() {
        fragments.put(1, new LoreFragment(1, "A Bloody Moon", "The moon was red that night. I remember the howling..."));
        fragments.put(2, new LoreFragment(2, "Silver Scars", "They told us silver would burn them. They didn't say it would make them angrier..."));
        fragments.put(3, new LoreFragment(3, "The Last Stand", "We fortified the gates. It wasn't enough. They came from the shadows..."));
        fragments.put(4, new LoreFragment(4, "Notes on the Warden", "He watches. Do not anger the trees. Do not speak his name..."));
        fragments.put(5, new LoreFragment(5, "Torn Journal Entry", "Day 43. The cold is unbearable. The pack outside is circling..."));
    }

    public LoreFragment getFragment(int id) {
        return fragments.get(id);
    }

    public int getTotalFragments() {
        return fragments.size();
    }

    public ItemStack createLoreFragmentItem(int fragmentId) {
        LoreFragment fragment = getFragment(fragmentId);
        if (fragment == null) return new ItemStack(Material.AIR);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        
        if (meta != null) {
            meta.setTitle("Lost Fragment: " + fragment.title);
            meta.setAuthor("Unknown");
            meta.addPage(fragment.content);
            
            NamespacedKey isLoreKey = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "isLoreFragment");
            NamespacedKey fragmentKey = new NamespacedKey(ShadowfangCorePlugin.getInstance(), "loreFragment");
            
            meta.getPersistentDataContainer().set(isLoreKey, PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(fragmentKey, PersistentDataType.INTEGER, fragmentId);
            
            book.setItemMeta(meta);
        }
        
        return book;
    }

    public static class LoreFragment {
        public final int id;
        public final String title;
        public final String content;

        public LoreFragment(int id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }
    }
}
