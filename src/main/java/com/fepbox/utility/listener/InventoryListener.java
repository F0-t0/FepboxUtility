package com.fepbox.utility.listener;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.gui.InvseeGui;
import com.fepbox.utility.gui.HomeGui;
import com.fepbox.utility.gui.WarpGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryListener implements Listener {
    private final ConfigManager cfg;
    private final MessageProvider msg;
    private final JavaPlugin plugin;
    public InventoryListener(JavaPlugin plugin, ConfigManager cfg, MessageProvider msg){ this.plugin=plugin; this.cfg=cfg; this.msg=msg; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player p){
            WarpGui gui = WarpGui.current(p);
            if (gui != null && e.getInventory().equals(gui.inventory())){
                gui.handleClick(e);
                return;
            }
            HomeGui homes = HomeGui.current(p);
            if (homes != null && e.getInventory().equals(homes.inventory())){
                e.setCancelled(true);
                homes.handleClick(e.getClick(), e.getRawSlot());
                return;
            }
            com.fepbox.utility.gui.CreateWarpGui creator = com.fepbox.utility.gui.CreateWarpGui.current(p);
            if (creator != null && e.getInventory().equals(creator.inventory())){
                creator.handleClick(e);
                return;
            }
            InvseeGui invsee = InvseeGui.current(p);
            if (invsee != null && e.getInventory().equals(invsee.inventory())){
                if (InvseeGui.isDecorSlot(e.getRawSlot())){
                    e.setCancelled(true);
                    return;
                }
                if (cfg.raw().getBoolean("invsee.readonly", false)){
                    e.setCancelled(true);
                    return;
                }
                // apply immediately after click so target inventory stays in sync
                Bukkit.getScheduler().runTask(plugin, invsee::applyChanges);
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getPlayer() instanceof Player p){
            InvseeGui invsee = InvseeGui.current(p);
            if (invsee != null && e.getInventory().equals(invsee.inventory())){
                invsee.applyChanges();
                invsee.close();
                return;
            }
            HomeGui homes = HomeGui.current(p);
            if (homes != null && e.getInventory().equals(homes.inventory())){
                homes.close();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e){
        if (e.getWhoClicked() instanceof Player p){
            InvseeGui invsee = InvseeGui.current(p);
            if (invsee != null && e.getInventory().equals(invsee.inventory())){
                if (cfg.raw().getBoolean("invsee.readonly", false)){
                    e.setCancelled(true);
                    return;
                }
                // cancel if drag touches decor slots
                for (int slot : e.getRawSlots()){
                    if (InvseeGui.isDecorSlot(slot)){
                        e.setCancelled(true);
                        return;
                    }
                }
                Bukkit.getScheduler().runTask(plugin, invsee::applyChanges);
            }
        }
    }
}
