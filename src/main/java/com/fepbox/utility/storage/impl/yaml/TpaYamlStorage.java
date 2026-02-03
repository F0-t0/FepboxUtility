package com.fepbox.utility.storage.impl.yaml;

import com.fepbox.utility.model.TpaRequest;
import com.fepbox.utility.storage.dao.TpaStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class TpaYamlStorage implements TpaStorage {
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration cfg;

    public TpaYamlStorage(JavaPlugin plugin) { this.plugin = plugin; }

    @Override
    public void init() {
        file = new File(plugin.getDataFolder(), "tpa.yml");
        if (!file.exists()) plugin.saveResource("tpa.yml", false);
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void saveRequest(TpaRequest req) {
        String path = "requests." + req.sender() + "." + req.target();
        cfg.set(path + ".expires", req.expiresAt());
        cfg.set(path + ".here", req.here());
        saveFile();
    }

    @Override
    public void removeRequest(UUID sender, UUID target) {
        cfg.set("requests." + sender + "." + target, null);
        saveFile();
    }

    @Override
    public List<TpaRequest> requestsTo(UUID target) {
        List<TpaRequest> list = new ArrayList<>();
        if (!cfg.contains("requests")) return list;
        cfg.getConfigurationSection("requests").getKeys(false).forEach(sender -> {
            String path = "requests." + sender + "." + target;
            if (cfg.contains(path)) {
                long exp = cfg.getLong(path + ".expires");
                boolean here = cfg.getBoolean(path + ".here");
                list.add(new TpaRequest(UUID.fromString(sender), target, exp, here));
            }
        });
        return list;
    }

    @Override
    public List<TpaRequest> requestsFrom(UUID sender) {
        List<TpaRequest> list = new ArrayList<>();
        String base = "requests." + sender;
        if (!cfg.contains(base)) return list;
        cfg.getConfigurationSection(base).getKeys(false).forEach(t -> {
            long exp = cfg.getLong(base + "." + t + ".expires");
            boolean here = cfg.getBoolean(base + "." + t + ".here");
            list.add(new TpaRequest(sender, UUID.fromString(t), exp, here));
        });
        return list;
    }

    @Override
    public void clearAllFrom(UUID sender) {
        cfg.set("requests." + sender, null);
        saveFile();
    }

    @Override
    public boolean toggle(UUID player) {
        boolean current = cfg.getBoolean("toggle." + player, false);
        cfg.set("toggle." + player, !current);
        saveFile();
        return !current;
    }

    @Override
    public boolean isToggled(UUID player) {
        return cfg.getBoolean("toggle." + player, false);
    }

    @Override
    public void ignore(UUID player, UUID target, boolean state) {
        cfg.set("ignore." + player + "." + target, state);
        saveFile();
    }

    @Override
    public boolean isIgnoring(UUID player, UUID target) {
        return cfg.getBoolean("ignore." + player + "." + target, false);
    }

    private void saveFile(){ try{ cfg.save(file);}catch(Exception e){ e.printStackTrace(); }}
}
