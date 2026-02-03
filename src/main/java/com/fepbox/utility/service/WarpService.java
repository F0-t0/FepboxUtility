package com.fepbox.utility.service;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.gui.WarpGui;
import com.fepbox.utility.model.Warp;
import com.fepbox.utility.storage.dao.WarpStorage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarpService {
    private final JavaPlugin plugin;
    private final WarpStorage storage;
    private final CooldownService cd;
    private final ConfigManager cfg;
    private final MessageProvider msg;
    private final TeleportService tp;
    private final ConcurrentHashMap<UUID, Long> warmups = new ConcurrentHashMap<>();

    public WarpService(JavaPlugin plugin, WarpStorage storage, CooldownService cd, ConfigManager cfg, MessageProvider msg, TeleportService tp){
        this.plugin = plugin; this.storage = storage; this.cd = cd; this.cfg = cfg; this.msg = msg; this.tp = tp;
        storage.init();
    }

    public void setWarp(Player p, String name){
        Location l = p.getLocation();
        storage.save(new Warp(name.toLowerCase(), l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
        msg.send(p, "warp-set", "<name>", name);
    }

    public void delete(Player p, String name){ storage.delete(name.toLowerCase()); msg.send(p,"warp-deleted","<name>",name); }

    public void rename(Player p, String old, String neu){ storage.rename(old.toLowerCase(), neu.toLowerCase()); msg.send(p,"warp-rename","<name>", neu); }

    public Warp find(String name){ return storage.load(name.toLowerCase()); }

    public List<Warp> list(){ return storage.list(); }

    public void openGui(Player p){ new WarpGui(plugin, list(), p, this, msg, cfg).open(); }

    public void teleport(Player p, Warp warp){
        if (warp == null){ msg.send(p, "warp-info", "<name>", "?"); return; }
        if (cd.isOnCooldown(p.getUniqueId(), "warp") && !p.hasPermission("fepboxutility.warp.bypass")){
            msg.send(p, "cooldown", "<seconds>", String.valueOf(cd.remaining(p.getUniqueId(), "warp"))); return;
        }
        int warm = cfg.warmup("warp");
        Runnable doTp = () -> {
            Location loc = warp.toLocation();
            if (loc == null){ msg.send(p, "warp-info", "<name>", warp.name()); return; }
            if (cfg.raw().getStringList("warps.blacklist").contains(loc.getWorld().getName())){ msg.send(p,"teleport-safe-fail"); return; }
            if (tp.safeTeleport(p, loc)) {
                msg.send(p, "warp-teleport", "<name>", warp.name());
                cd.put(p.getUniqueId(), "warp", cfg.cooldown("warp"));
            }
            warmups.remove(p.getUniqueId());
        };
        if (warm > 0){
            msg.send(p, "warmup", "<seconds>", String.valueOf(warm));
            warmups.put(p.getUniqueId(), Instant.now().getEpochSecond() + warm);
            plugin.getServer().getScheduler().runTaskLater(plugin, doTp, warm * 20L);
        } else doTp.run();
    }

    public boolean isInWarmup(UUID id){ return warmups.containsKey(id); }
    public void cancelWarmup(UUID id){ warmups.remove(id); }
}
