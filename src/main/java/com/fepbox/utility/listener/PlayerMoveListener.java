package com.fepbox.utility.listener;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.HomeService;
import com.fepbox.utility.service.TpaService;
import com.fepbox.utility.service.UtilityService;
import com.fepbox.utility.service.WarpService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final UtilityService util;
    private final WarpService warp;
    private final HomeService home;
    private final TpaService tpa;
    private final ConfigManager cfg;
    private final MessageProvider msg;

    public PlayerMoveListener(UtilityService util, WarpService warp, HomeService home, TpaService tpa, ConfigManager cfg, MessageProvider msg){
        this.util=util; this.warp=warp; this.home=home; this.tpa=tpa; this.cfg=cfg; this.msg=msg;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        var p = e.getPlayer();
        if (util.isFrozen(p.getUniqueId())){
            e.setTo(e.getFrom());
            return;
        }
        if (!cfg.raw().getBoolean("teleport.cancel-on-move", true)) return;
        // cancel only when player changes block coordinates, rotations alone are allowed
        if (e.getFrom().getBlockX()!=e.getTo().getBlockX() || e.getFrom().getBlockY()!=e.getTo().getBlockY() || e.getFrom().getBlockZ()!=e.getTo().getBlockZ()){
            boolean cancelled = false;
            if (warp.isInWarmup(p.getUniqueId())){ warp.cancelWarmup(p.getUniqueId()); cancelled = true; }
            if (home.isInWarmup(p.getUniqueId())){ home.cancelWarmup(p.getUniqueId()); cancelled = true; }
            if (cancelled) msg.send(p,"teleport-cancelled");
        }
    }
}
