package com.fepbox.utility.listener;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {
    private final UtilityService util;
    private final ConfigManager cfg;
    public CombatListener(UtilityService util, ConfigManager cfg){ this.util=util; this.cfg=cfg; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        if (!cfg.module("fly")) return;
        if (e.getDamager() instanceof Player p) util.tagCombat(p);
        if (e.getEntity() instanceof Player p) util.tagCombat(p);
    }
}
