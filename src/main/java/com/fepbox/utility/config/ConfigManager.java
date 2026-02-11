package com.fepbox.utility.config;

import com.fepbox.utility.storage.DatabasePool;
import com.fepbox.utility.storage.StorageType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private DatabasePool pool;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration raw() { return plugin.getConfig(); }

    public boolean module(String key) { return plugin.getConfig().getBoolean("modules." + key, true); }

    public int cooldown(String key) { return plugin.getConfig().getInt("cooldowns." + key, 0); }

    public int warmup(String key) { return plugin.getConfig().getInt("warmups." + key, 0); }

    public StorageType storageType() {
        String t = plugin.getConfig().getString("storage.type", "YAML");
        return StorageType.valueOf(t.trim().toUpperCase());
    }

    public DatabasePool databasePool() {
        if (pool != null) return pool;
        pool = new DatabasePool(plugin);
        return pool;
    }
}
