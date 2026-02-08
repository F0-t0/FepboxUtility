package com.fepbox.utility.gui;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Warp;
import com.fepbox.utility.service.WarpService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarpGui {
    private final JavaPlugin plugin;
    private final Player viewer;
    private final WarpService service;
    private final MessageProvider msg;
    private final ConfigManager cfg;
    private final Inventory inv;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, Warp> slotToWarp = new HashMap<>();
    private static final Map<UUID, WarpGui> open = new ConcurrentHashMap<>();

    public WarpGui(JavaPlugin plugin, List<Warp> warps, Player viewer, WarpService service, MessageProvider msg, ConfigManager cfg){
        this.plugin = plugin;
        this.viewer = viewer;
        this.service = service;
        this.msg = msg;
        this.cfg = cfg;
        int size = cfg.raw().getInt("warps.gui-size", 54);
        this.inv = Bukkit.createInventory(viewer, size, "Warps");
        build(warps, size);
    }

    private void build(List<Warp> warps, int size){
        boolean[] occupied = new boolean[size];
        // place warps with fixed slots
        for (Warp w : warps){
            if (!viewer.hasPermission("fepboxutility.warp." + w.name()) && !viewer.hasPermission("fepboxutility.*")) continue;
            int slot = w.slot();
            ItemStack item = iconFor(w);
            if (slot >=0 && slot < size){
                inv.setItem(slot, item);
                slotToWarp.put(slot, w);
                occupied[slot]=true;
            }
        }
        // place remaining warps sequentially
        for (Warp w : warps){
            if (!viewer.hasPermission("fepboxutility.warp." + w.name()) && !viewer.hasPermission("fepboxutility.*")) continue;
            if (slotToWarp.containsValue(w)) continue;
            int slot = firstFree(occupied);
            if (slot==-1) break;
            occupied[slot]=true;
            inv.setItem(slot, iconFor(w));
            slotToWarp.put(slot, w);
        }
    }

    private int firstFree(boolean[] occ){
        for (int i=0;i<occ.length;i++) if (!occ[i]) return i;
        return -1;
    }

    private ItemStack iconFor(Warp w){
        Material mat;
        try { mat = Material.valueOf(w.icon()); } catch (Exception e){ mat = Material.ENDER_PEARL; }
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<yellow>"+w.name()+"</yellow>"));
        meta.lore(List.of(
                mm.deserialize("<gray>"+w.world()+"</gray>"),
                mm.deserialize(String.format(Locale.ROOT,"<gray>%.1f %.1f %.1f</gray>", w.x(), w.y(), w.z()))
        ));
        item.setItemMeta(meta);
        return item;
    }

    public void open(){
        viewer.openInventory(inv);
        open.put(viewer.getUniqueId(), this);
    }

    public static WarpGui current(Player p){ return open.get(p.getUniqueId()); }

    public Inventory inventory(){ return inv; }

    public void handleClick(InventoryClickEvent e){
        if (!e.getInventory().equals(inv)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        Warp warp = slotToWarp.get(slot);
        if (warp == null) return;
        switch (e.getClick()){
            case LEFT -> service.teleport(viewer, warp);
            case SHIFT_RIGHT -> msg.send(viewer,"warp-info","<name>", warp.name());
            default -> {}
        }
    }
}
