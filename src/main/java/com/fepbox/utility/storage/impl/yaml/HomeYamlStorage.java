package com.fepbox.utility.storage.impl.yaml;

import com.fepbox.utility.model.Home;
import com.fepbox.utility.storage.dao.HomeStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HomeYamlStorage implements HomeStorage {
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration cfg;

    public HomeYamlStorage(JavaPlugin plugin) { this.plugin = plugin; }

    @Override
    public void init() {
        file = new File(plugin.getDataFolder(), "homes.yml");
        if (!file.exists()) plugin.saveResource("homes.yml", false);
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void save(UUID owner, Home home) {
        String p = owner + "." + home.name();
        cfg.set(p + ".world", home.world());
        cfg.set(p + ".x", home.x());
        cfg.set(p + ".y", home.y());
        cfg.set(p + ".z", home.z());
        cfg.set(p + ".yaw", home.yaw());
        cfg.set(p + ".pitch", home.pitch());
        saveFile();
    }

    @Override
    public Home load(UUID owner, String name) {
        String p = owner + "." + name;
        if (!cfg.contains(p)) return null;
        return new Home(name,
                cfg.getString(p + ".world"),
                cfg.getDouble(p + ".x"),
                cfg.getDouble(p + ".y"),
                cfg.getDouble(p + ".z"),
                (float) cfg.getDouble(p + ".yaw"),
                (float) cfg.getDouble(p + ".pitch"));
    }

    @Override
    public List<Home> list(UUID owner) {
        if (!cfg.contains(owner.toString())) return List.of();
        return cfg.getConfigurationSection(owner.toString()).getKeys(false).stream()
                .map(n -> load(owner, n)).filter(Objects::nonNull).toList();
    }

    @Override
    public void delete(UUID owner, String name) {
        cfg.set(owner + "." + name, null);
        saveFile();
    }

    @Override
    public void rename(UUID owner, String oldName, String newName) {
        Home h = load(owner, oldName);
        delete(owner, oldName);
        if (h != null) save(owner, new Home(newName, h.world(), h.x(), h.y(), h.z(), h.yaw(), h.pitch()));
    }

    private void saveFile() {
        try { cfg.save(file);} catch (Exception e) { e.printStackTrace(); }
    }
}
