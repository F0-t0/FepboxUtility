package com.fepbox.utility.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Simple shared inventory view for /invsee that also exposes armor and offhand slots.
 */
public class InvseeGui {
    private static final Map<UUID, InvseeGui> open = new ConcurrentHashMap<>();
    private static final Set<Integer> DECOR_SLOTS = Set.of(36, 37, 38, 39, 40, 41, 42, 43, 44, 49);

    private final Player viewer;
    private final Player target;
    private final Inventory inv;

    public InvseeGui(Player viewer, Player target){
        this.viewer = viewer;
        this.target = target;
        String title = target.getName() + " Inventory";
        this.inv = Bukkit.createInventory(viewer, 54, title);
        populate();
    }

    private void populate(){
        PlayerInventory tInv = target.getInventory();
        ItemStack[] contents = tInv.getContents();
        // main inventory + hotbar
        for (int i = 0; i < 36 && i < contents.length; i++) {
            inv.setItem(i, contents[i]);
        }
        addDecorations();
        inv.setItem(45, tInv.getHelmet());
        inv.setItem(46, tInv.getChestplate());
        inv.setItem(47, tInv.getLeggings());
        inv.setItem(48, tInv.getBoots());
        inv.setItem(50, tInv.getItemInOffHand());
    }

    private void addDecorations(){
        inv.setItem(36, label("Helm v"));
        inv.setItem(37, label("Klatka v"));
        inv.setItem(38, label("Nogi v"));
        inv.setItem(39, label("Buty v"));
        inv.setItem(41, label("Offhand v"));
        inv.setItem(40, filler());
        inv.setItem(42, filler());
        inv.setItem(43, filler());
        inv.setItem(44, filler());
        inv.setItem(49, filler());
    }

    private ItemStack label(String name){
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var meta = pane.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + name);
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack filler(){ return label(" "); }

    public void open(){
        viewer.openInventory(inv);
        open.put(viewer.getUniqueId(), this);
    }

    public static InvseeGui current(Player viewer){
        return open.get(viewer.getUniqueId());
    }

    public void close(){
        open.remove(viewer.getUniqueId());
    }

    public Inventory inventory(){
        return inv;
    }

    public Player target(){
        return target;
    }

    public static boolean isDecorSlot(int rawSlot){
        return DECOR_SLOTS.contains(rawSlot);
    }

    public void applyChanges(){
        if (!target.isOnline()) { // avoid touching offline targets to prevent NPE on some servers
            return;
        }
        PlayerInventory tInv = target.getInventory();
        ItemStack[] updated = new ItemStack[41];
        ItemStack[] view = inv.getContents();
        for (int i = 0; i < 36 && i < view.length; i++) {
            updated[i] = view[i];
        }
        updated[36] = safeItem(inv.getItem(48)); // boots
        updated[37] = safeItem(inv.getItem(47)); // leggings
        updated[38] = safeItem(inv.getItem(46)); // chestplate
        updated[39] = safeItem(inv.getItem(45)); // helmet
        updated[40] = safeItem(inv.getItem(50)); // offhand
        tInv.setContents(updated);
        target.updateInventory();
    }

    private ItemStack safeItem(ItemStack item){
        return item == null ? new ItemStack(Material.AIR) : item;
    }
}
