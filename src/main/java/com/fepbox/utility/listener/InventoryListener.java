package com.fepbox.utility.listener;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.gui.WarpGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {
    private final ConfigManager cfg;
    private final MessageProvider msg;
    public InventoryListener(ConfigManager cfg, MessageProvider msg){ this.cfg=cfg; this.msg=msg; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player p){
            WarpGui gui = WarpGui.current(p);
            if (gui != null && e.getInventory().equals(gui.inventory())){
                gui.handleClick(e);
                return;
            }
        }
        String title = e.getView().getTitle().toLowerCase();
        if (cfg.raw().getBoolean("enderchest.readonly", true) && title.contains("ender chest")){
            e.setCancelled(true);
        }
        if (cfg.raw().getBoolean("invsee.readonly", false) && title.contains("inventory")){
            e.setCancelled(true);
        }
    }
}
