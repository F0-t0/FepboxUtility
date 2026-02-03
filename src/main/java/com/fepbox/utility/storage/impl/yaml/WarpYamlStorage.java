package com.fepbox.utility.storage.impl.yaml;

import com.fepbox.utility.model.Warp;
import com.fepbox.utility.storage.dao.WarpStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class WarpYamlStorage implements WarpStorage {
    private final JavaPlugin plugin;
    private FileConfiguration cfg;
    private File file;

    public WarpYamlStorage(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        file = new File(plugin.getDataFolder(), "warps.yml");
        if (!file.exists()) plugin.saveResource("warps.yml", false);
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void save(Warp warp) {
        String p = warp.name();
        cfg.set(p + ".world", warp.world());
        cfg.set(p + ".x", warp.x());
        cfg.set(p + ".y", warp.y());
        cfg.set(p + ".z", warp.z());
        cfg.set(p + ".yaw", warp.yaw());
        cfg.set(p + ".pitch", warp.pitch());
        saveFile();
    }

    @Override
    public Warp load(String name) {
        if (!cfg.contains(name)) return null;
        return new Warp(name,
                cfg.getString(name + ".world"),
                cfg.getDouble(name + ".x"),
                cfg.getDouble(name + ".y"),
                cfg.getDouble(name + ".z"),
                (float) cfg.getDouble(name + ".yaw"),
                (float) cfg.getDouble(name + ".pitch"));
    }

    @Override
    public List<Warp> list() {
        return cfg.getKeys(false).stream().map(this::load).filter(Objects::nonNull).toList();
    }

    @Override
    public void delete(String name) {
        cfg.set(name, null);
        saveFile();
    }

    @Override
    public void rename(String oldName, String newName) {
        Warp w = load(oldName);
        delete(oldName);
        if (w != null) save(new Warp(newName, w.world(), w.x(), w.y(), w.z(), w.yaw(), w.pitch()));
    }

    private void saveFile() {
        try { cfg.save(file);} catch (Exception e) { e.printStackTrace(); }
    }
}
