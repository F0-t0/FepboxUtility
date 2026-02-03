package com.fepbox.utility.gui;

import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Warp;
import com.fepbox.utility.service.WarpService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CreateWarpGui {
    private final JavaPlugin plugin;
    private final Player viewer;
    private final WarpService service;
    private final MessageProvider msg;
    private final Inventory inv;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Set<Integer> occupied = new HashSet<>();
    private static final Map<UUID, CreateWarpGui> open = new ConcurrentHashMap<>();

    public CreateWarpGui(JavaPlugin plugin, Player viewer, WarpService service, MessageProvider msg, int size){
        this.plugin = plugin;
        this.viewer = viewer;
        this.service = service;
        this.msg = msg;
        this.inv = plugin.getServer().createInventory(viewer, size, "Create Warp");
        setup();
        open.put(viewer.getUniqueId(), this);
    }

    private void setup(){
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.displayName(mm.deserialize("<gray>-</gray>"));
        pane.setItemMeta(pm);

        for (int i=0;i<inv.getSize();i++) inv.setItem(i, pane);

        List<Warp> warps = service.list();
        for (Warp w : warps){
            int slot = w.slot();
            if (slot < 0 || slot >= inv.getSize()) continue;
            occupied.add(slot);
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta bm = barrier.getItemMeta();
            bm.displayName(mm.deserialize("<red>"+w.name()+"</red>"));
            barrier.setItemMeta(bm);
            inv.setItem(slot, barrier);
        }
    }

    public void open(){ viewer.openInventory(inv); }

    public static CreateWarpGui current(Player p){ return open.get(p.getUniqueId()); }

    public Inventory inventory(){ return inv; }

    public void handleClick(InventoryClickEvent e){
        if (!e.getInventory().equals(inv)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) return;
        if (occupied.contains(slot)){
            msg.send(viewer, "warp-info", "<name>", "slot zajety");
            return;
        }
        ItemStack inHand = viewer.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR){
            msg.send(viewer, "usage", "<usage>", "Trzymaj item jako ikonę");
            return;
        }
        String icon = inHand.getType().name();
        service.beginCreation(viewer, slot, icon);
    }
}
