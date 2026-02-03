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
        if (e.getFrom().getX()!=e.getTo().getX() || e.getFrom().getZ()!=e.getTo().getZ()){
            if (warp.isInWarmup(p.getUniqueId())){ warp.cancelWarmup(p.getUniqueId()); msg.send(p,"teleport-cancelled"); }
            if (home.isInWarmup(p.getUniqueId())){ home.cancelWarmup(p.getUniqueId()); msg.send(p,"teleport-cancelled"); }
        }
    }
}
