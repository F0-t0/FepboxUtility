package com.fepbox.utility.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class PaginatedGui {
    protected final Player viewer;
    protected final int size;
    protected int page = 0;
    protected Inventory inv;

    protected PaginatedGui(Player viewer, int size){ this.viewer = viewer; this.size = size; }

    public void open(){
        this.inv = Bukkit.createInventory(viewer, size, title());
        drawPage();
        viewer.openInventory(inv);
    }

    public Inventory inventory(){ return inv; }

    protected abstract String title();
    protected abstract List<ItemStack> items();

    protected void drawPage(){
        inv.clear();
        List<ItemStack> list = items();
        int per = size - 9;
        int start = page * per;
        for (int i=0;i<per && start+i<list.size();i++) inv.setItem(i, list.get(start+i));
        drawNav(list.size(), per);
    }

    private void drawNav(int total, int per){
        if (page>0) inv.setItem(size-6, navItem("Prev"));
        if ((page+1)*per < total) inv.setItem(size-4, navItem("Next"));
    }

    protected abstract ItemStack navItem(String type);

    public void handleClick(InventoryClickEvent e){
        if (!e.getInventory().equals(inv)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        int per = size - 9;
        if (slot == size-6 && page>0){ page--; drawPage(); return; }
        if (slot == size-4 && (page+1)*per < items().size()){ page++; drawPage(); return; }
        onItemClick(e, slot, slot + page*per);
    }

    protected abstract void onItemClick(InventoryClickEvent e, int slot, int absoluteIndex);
}
