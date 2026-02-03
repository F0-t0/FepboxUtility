package com.fepbox.utility.gui;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Warp;
import com.fepbox.utility.service.WarpService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarpGui extends PaginatedGui {
    private final JavaPlugin plugin;
    private final List<Warp> warps;
    private final WarpService service;
    private final MessageProvider msg;
    private final ConfigManager cfg;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private static final Map<UUID, WarpGui> open = new ConcurrentHashMap<>();

    public WarpGui(JavaPlugin plugin, List<Warp> warps, Player viewer, WarpService service, MessageProvider msg, ConfigManager cfg){
        super(viewer, cfg.raw().getInt("warps.gui-size", 54));
        this.plugin=plugin; this.warps = warps; this.service = service; this.msg = msg; this.cfg=cfg;
    }

    @Override
    public void open(){
        super.open();
        open.put(viewer.getUniqueId(), this);
    }

    public static WarpGui current(Player p){ return open.get(p.getUniqueId()); }

    @Override
    protected String title(){ return "Warps"; }

    @Override
    protected List<ItemStack> items(){
        List<ItemStack> list = new ArrayList<>();
        for (Warp w : warps){
            if (!viewer.hasPermission("fepboxutility.warp." + w.name()) && !viewer.hasPermission("fepboxutility.*")) continue;
            ItemStack item;
            try { item = new ItemStack(Material.valueOf(w.icon())); }
            catch (IllegalArgumentException ex){ item = new ItemStack(Material.ENDER_PEARL); }
            ItemMeta meta = item.getItemMeta();
            meta.displayName(mm.deserialize("<yellow>"+w.name()+"</yellow>"));
            meta.lore(List.of(
                    mm.deserialize("<gray>"+w.world()+"</gray>"),
                    mm.deserialize(String.format("<gray>%.1f %.1f %.1f</gray>", w.x(), w.y(), w.z()))
            ));
            item.setItemMeta(meta);
            list.add(item);
        }
        return list;
    }

    @Override
    protected ItemStack navItem(String type){
        ItemStack it = new ItemStack(type.equals("Prev")?Material.ARROW:Material.SPECTRAL_ARROW);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(mm.deserialize("<yellow>"+type+"</yellow>"));
        it.setItemMeta(meta);
        return it;
    }

    @Override
    protected void onItemClick(InventoryClickEvent e, int slot, int absoluteIndex){
        int per = size-9;
        int idx = page*per + slot;
        List<Warp> filtered = warps.stream().filter(w -> viewer.hasPermission("fepboxutility.warp."+w.name()) || viewer.hasPermission("fepboxutility.*")).toList();
        if (idx >= filtered.size()) return;
        Warp warp = filtered.get(idx);
        switch (e.getClick()){
            case LEFT -> service.teleport(viewer, warp);
            case SHIFT_RIGHT -> msg.send(viewer,"warp-info","<name>", warp.name());
            default -> {}
        }
    }
}
