package com.fepbox.utility.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageProvider {
    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private FileConfiguration cfg;
    private String prefix;

    public MessageProvider(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        this.cfg = YamlConfiguration.loadConfiguration(file);
        this.prefix = cfg.getString("prefix", "");
    }

    public void send(CommandSender target, String key) {
        send(target, key, new String[0]);
    }

    public void send(CommandSender target, String key, String... replacements) {
        String raw = cfg.getString("messages." + key, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        Component comp = mm.deserialize(prefix + raw);
        target.sendMessage(comp);
    }

    public Component parse(String template) {
        return mm.deserialize(prefix + template);
    }

    public MiniMessage mini() { return mm; }

    /**
     * Returns raw message string (without prefix) for custom usage like GUI titles.
     */
    public String raw(String key, String def){
        return cfg.getString("messages."+key, def);
    }
}
