package com.fepbox.utility.gui;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Home;
import com.fepbox.utility.service.HomeService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI for managing homes with per-slot permissions (home.1, home.2, ...).
 * Left click teleports to saved home, right click zapisuje dom w slocie.
 */
public class HomeGui {
    private static final Map<UUID, HomeGui> open = new ConcurrentHashMap<>();

    private final Player viewer;
    private final HomeService service;
    private final MessageProvider msg;
    private final ConfigManager cfg;
    private final Inventory inv;
    private final Map<Integer, Integer> slotToIndex = new HashMap<>();
    private final int slots;
    private static final ItemStack FILLER = createFiller();
    private final net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();

    public HomeGui(Player viewer, HomeService service, MessageProvider msg, ConfigManager cfg){
        this.viewer = viewer;
        this.service = service;
        this.msg = msg;
        this.cfg = cfg;
        this.slots = Math.max(1, cfg.raw().getInt("homes.gui-slots", 5));
        String title = msg.raw("gui.home.title", "<gray>Twoje home</gray>");
        this.inv = Bukkit.createInventory(viewer, 27, mm.deserialize(title));
        build();
    }

    private void build(){
        slotToIndex.clear();
        List<Home> homes = service.list(viewer.getUniqueId());
        // fill first and third row with glass
        for (int i=0;i<9;i++){ inv.setItem(i, FILLER); inv.setItem(18+i, FILLER); }

        int rowStart = 9;
        int offset = Math.max(0, (9 - slots) / 2);
        for (int i=0;i<9;i++) inv.setItem(rowStart+i, FILLER);
        for (int i = 0; i < slots && i < 9; i++){
            int rawSlot = rowStart + offset + i;
            int homeIndex = i + 1;
            boolean unlocked = hasSlotPermission(homeIndex);
            Home existing = homes.stream()
                    .filter(h -> h.name().equalsIgnoreCase(service.nameForIndex(homeIndex)))
                    .findFirst().orElse(null);
            ItemStack item;
            if (!unlocked){
                item = icon(Material.BEDROCK,
                        msg.raw("gui.home.locked-name", "Home <n> (zablokowany)").replace("<n>", String.valueOf(homeIndex)),
                        List.of(msg.raw("gui.home.locked-lore", "<red>Brak permisji: home.<n>").replace("<n>", String.valueOf(homeIndex))));
            } else if (existing == null){
                item = icon(Material.LIGHT_GRAY_BED,
                        msg.raw("gui.home.empty-name", "Home <n> (pusty)").replace("<n>", String.valueOf(homeIndex)),
                        List.of(
                                msg.raw("gui.home.empty-lore-teleport", "<gray>LPM: teleport (brak celu)>"),
                                msg.raw("gui.home.empty-lore-set", "<yellow>PPM: ustaw tutaj>")
                        ));
            } else {
                item = icon(Material.RED_BED,
                        msg.raw("gui.home.saved-name", "Home <n>").replace("<n>", String.valueOf(homeIndex)),
                        List.of(
                                msg.raw("gui.home.saved-lore-status", "<green>Zapisany>"),
                                msg.raw("gui.home.saved-lore-teleport", "<gray>LPM: teleport>"),
                                msg.raw("gui.home.saved-lore-set", "<yellow>PPM: ustaw tutaj>")
                        ));
            }
            inv.setItem(rawSlot, item);
            slotToIndex.put(rawSlot, homeIndex);
        }
    }

    private boolean hasSlotPermission(int index){
        String p1 = "home." + index;
        String p2 = "fepboxutility.home." + index;
        return viewer.hasPermission("fepboxutility.*") || viewer.hasPermission(p1) || viewer.hasPermission(p2);
    }

    private ItemStack icon(Material mat, String name, List<String> lore){
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        java.util.List<net.kyori.adventure.text.Component> l = new java.util.ArrayList<>();
        for (String line : lore) l.add(mm.deserialize(line));
        meta.lore(l);
        item.setItemMeta(meta);
        return item;
    }

    public void open(){
        viewer.openInventory(inv);
        open.put(viewer.getUniqueId(), this);
    }

    public static HomeGui current(Player p){ return open.get(p.getUniqueId()); }

    public void close(){ open.remove(viewer.getUniqueId()); }

    public Inventory inventory(){ return inv; }

    public void handleClick(ClickType click, int rawSlot){
        Integer idx = slotToIndex.get(rawSlot);
        if (idx == null) return;
        if (!hasSlotPermission(idx)){
            msg.send(viewer, "no-permission");
            return;
        }
        String homeName = service.nameForIndex(idx);
        if (click.isLeftClick()){
            Home h = service.find(viewer, homeName);
            if (h == null){
                msg.send(viewer, "home-deleted", "<name>", homeName);
                return;
            }
            service.teleport(viewer, h);
        } else if (click.isRightClick()){
            service.setHomeFixed(viewer, homeName);
            refresh();
        }
        viewer.closeInventory();
    }

    private void refresh(){
        inv.clear();
        build();
        viewer.updateInventory();
    }

    private static ItemStack createFiller(){
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(net.kyori.adventure.text.Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }
}
