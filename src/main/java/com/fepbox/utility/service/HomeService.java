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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Sound;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class HomeService {
    private final JavaPlugin plugin;
    private final HomeStorage storage;
    private final CooldownService cd;
    private final ConfigManager cfg;
    private final MessageProvider msg;
    private final TeleportService tp;
    private final ConcurrentHashMap<UUID, BukkitTask> warmups = new ConcurrentHashMap<>();

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

    public void setHomeFixed(Player p, String name){
        // overwrites specific slot without limit, permission handled by caller
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

    public String nameForIndex(int idx){ return "home" + idx; }

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
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                // małe opóźnienie ruchu po teleportacji
                p.setVelocity(new org.bukkit.util.Vector(0,0,0));
                p.setWalkSpeed(p.getWalkSpeed());
            }
        };
        // pre-check bezpieczeństwa zanim zaczniemy countdown
        var locCheck = home.toLocation();
        if (locCheck == null || tp.safeLocation(locCheck) == null){
            msg.send(p,"teleport-safe-fail");
            return;
        }
        if (warm > 0){
            int[] remaining = {warm};
            BukkitTask[] holder = new BukkitTask[1];
            BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                int sec = remaining[0];
                String head = msg.raw("title.teleport.header", "<yellow>TELEPORTACJA</yellow>").replace("<seconds>", String.valueOf(sec));
                String sub = msg.raw("title.teleport.sub", "<gray>za <seconds> sekundy</gray>").replace("<seconds>", String.valueOf(sec));
                var titleComp = msg.mini().deserialize(head);
                var subComp = msg.mini().deserialize(sub);
                String titleStr = LegacyComponentSerializer.legacySection().serialize(titleComp);
                String subStr = LegacyComponentSerializer.legacySection().serialize(subComp);
                p.sendTitle(titleStr, subStr, 0, 20, 0);
                remaining[0]--;
                if (remaining[0] < 0){
                    doTp.run();
                    BukkitTask t = holder[0];
                    if (t != null) t.cancel();
                    warmups.remove(p.getUniqueId());
                }
            }, 0L, 20L);
            holder[0] = task;
            warmups.put(p.getUniqueId(), task);
        } else doTp.run();
    }

    public boolean isInWarmup(UUID id){ return warmups.containsKey(id); }
    public void cancelWarmup(UUID id){
        BukkitTask task = warmups.remove(id);
        if (task != null) task.cancel();
    }
}
