package com.fepbox.utility.service;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportService {
    private final JavaPlugin plugin;
    private final ConfigManager cfg;
    private final MessageProvider msg;

    public TeleportService(JavaPlugin plugin, ConfigManager cfg, MessageProvider msg){
        this.plugin = plugin; this.cfg = cfg; this.msg = msg;
    }

    public boolean safeTeleport(Player p, Location target){
        if (!cfg.raw().getBoolean("teleport.safe", true)) {
            p.teleportAsync(target);
            return true;
        }
        Location safe = findSafe(target.clone());
        if (safe == null){
            msg.send(p, "teleport-safe-fail");
            return false;
        }
        p.teleportAsync(safe);
        return true;
    }

    /** Returns a safe landing location or null if none found (used for pre-checks). */
    public Location safeLocation(Location target){
        if (!cfg.raw().getBoolean("teleport.safe", true)) return target;
        return findSafe(target.clone());
    }

    private Location findSafe(Location loc){
        for (int dy = 0; dy < 3; dy++) {
            Location check = loc.clone().add(0, dy, 0);
            if (isSafe(check)) return check;
        }
        for (int dy = -2; dy > -6; dy--) {
            Location check = loc.clone().add(0, dy, 0);
            if (isSafe(check)) return check;
        }
        return null;
    }

    private boolean isSafe(Location l){
        Block feet = l.getBlock();
        Block head = l.clone().add(0,1,0).getBlock();
        Block below = l.clone().add(0,-1,0).getBlock();
        return !feet.getType().isSolid() && !head.getType().isSolid() && below.getType().isSolid() && below.getType()!= Material.LAVA;
    }
}
