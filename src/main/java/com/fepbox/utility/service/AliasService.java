package com.fepbox.utility.service;

import com.fepbox.utility.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AliasService {
    private final JavaPlugin plugin;
    private final ConfigManager cfg;
    private final Map<String, String> aliases = new HashMap<>();

    public AliasService(JavaPlugin plugin, ConfigManager cfg){
        this.plugin = plugin;
        this.cfg = cfg;
        load();
    }

    public void load(){
        aliases.clear();
        ConfigurationSection sec = cfg.raw().getConfigurationSection("aliases");
        if (sec == null) return;
        for (String key : sec.getKeys(false)){
            String target = sec.getString(key, "").trim();
            if (!target.isEmpty()){
                aliases.put(key.toLowerCase(Locale.ROOT), target);
            }
        }
    }

    public Map<String, String> all(){ return Map.copyOf(aliases); }

    public boolean exists(String alias){ return aliases.containsKey(alias.toLowerCase(Locale.ROOT)); }

    public void add(String alias, String target){
        String cleanAlias = alias.replaceFirst("^/+", "").toLowerCase(Locale.ROOT);
        String cleanTarget = target.replaceFirst("^/+", "").trim();
        aliases.put(cleanAlias, cleanTarget);
        persist();
    }

    public boolean remove(String alias){
        String key = alias.toLowerCase(Locale.ROOT);
        if (!aliases.containsKey(key)) return false;
        aliases.remove(key);
        persist();
        return true;
    }

    public String rewrite(String rawMessage){
        if (rawMessage == null || rawMessage.isEmpty()) return null;
        String msg = rawMessage.startsWith("/") ? rawMessage.substring(1) : rawMessage;
        String[] parts = msg.split(" ", 2);
        String alias = parts[0].toLowerCase(Locale.ROOT);
        String target = aliases.get(alias);
        if (target == null) return null;
        // prevent simple self-loop like alias -> same token
        if (target.equalsIgnoreCase(parts[0])) return null;
        String rest = parts.length > 1 ? " " + parts[1] : "";
        return "/" + target + rest;
    }

    private void persist(){
        // replace entire aliases section to keep config tidy
        cfg.raw().set("aliases", null);
        aliases.forEach((k,v) -> cfg.raw().set("aliases." + k, v));
        plugin.saveConfig();
    }
}
