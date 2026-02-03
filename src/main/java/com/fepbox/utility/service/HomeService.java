package com.fepbox.utility.service;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Home;
import com.fepbox.utility.storage.dao.HomeStorage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HomeService {
    private final JavaPlugin plugin;
    private final HomeStorage storage;
    private final CooldownService cd;
    private final ConfigManager cfg;
    private final MessageProvider msg;
    private final TeleportService tp;
    private final ConcurrentHashMap<UUID, Long> warmups = new ConcurrentHashMap<>();

    public HomeService(JavaPlugin plugin, HomeStorage storage, CooldownService cd, ConfigManager cfg, MessageProvider msg, TeleportService tp){
        this.plugin=plugin; this.storage=storage; this.cd=cd; this.cfg=cfg; this.msg=msg; this.tp=tp;
        storage.init();
    }

    public void setHome(Player p, String name){
        if (!canSetMore(p)) { msg.send(p, "no-permission"); return; }
        Location l = p.getLocation();
        storage.save(p.getUniqueId(), new Home(name.toLowerCase(), l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
        msg.send(p, "home-set", "<name>", name);
    }

    private boolean canSetMore(Player p){
        int limit = cfg.raw().getInt("homes.default-limit", 1);
        var ranks = cfg.raw().getConfigurationSection("homes.ranks");
        if (ranks != null) {
            for (String perm : ranks.getKeys(false)) {
                if (p.hasPermission("fepboxutility.homes." + perm)) limit = Math.max(limit, ranks.getInt(perm));
            }
        }
        return storage.list(p.getUniqueId()).size() < limit;
    }

    public void delete(Player p, String name){ storage.delete(p.getUniqueId(), name.toLowerCase()); msg.send(p,"home-deleted","<name>",name); }

    public void rename(Player p, String old, String neu){ storage.rename(p.getUniqueId(), old.toLowerCase(), neu.toLowerCase()); msg.send(p,"home-rename","<name>",neu); }

    public void listHomes(Player p){
        StringBuilder sb = new StringBuilder();
        storage.list(p.getUniqueId()).forEach(h -> sb.append(h.name()).append(" "));
        p.sendMessage(msg.parse("<yellow>Homes: " + sb));
    }

    public java.util.List<Home> list(UUID owner){ return storage.list(owner); }

    public Home find(Player p, String name){ return storage.load(p.getUniqueId(), name.toLowerCase()); }

    public void teleport(Player p, Home home){
        if (cd.isOnCooldown(p.getUniqueId(), "home") && !p.hasPermission("fepboxutility.home.bypass")){
            msg.send(p,"cooldown","<seconds>", String.valueOf(cd.remaining(p.getUniqueId(),"home"))); return;
        }
        int warm = cfg.warmup("home");
        Runnable doTp = () -> {
            var loc = home.toLocation();
            if (loc == null){ msg.send(p,"teleport-safe-fail"); return; }
            if (tp.safeTeleport(p, loc)){
                msg.send(p,"home-teleport","<name>", home.name());
                cd.put(p.getUniqueId(), "home", cfg.cooldown("home"));
            }
            warmups.remove(p.getUniqueId());
        };
        if (warm > 0){
            msg.send(p,"warmup","<seconds>", String.valueOf(warm));
            warmups.put(p.getUniqueId(), Instant.now().getEpochSecond()+warm);
            plugin.getServer().getScheduler().runTaskLater(plugin, doTp, warm*20L);
        } else doTp.run();
    }

    public boolean isInWarmup(UUID id){ return warmups.containsKey(id); }
    public void cancelWarmup(UUID id){ warmups.remove(id); }
}
