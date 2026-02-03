package com.fepbox.utility.service;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class UtilityService {
    private final JavaPlugin plugin;
    private final ConfigManager cfg;
    private final MessageProvider msg;
    private final CooldownService cd;
    private final Set<UUID> frozen = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, UUID> lastMsg = new HashMap<>();
    private final Set<UUID> combatTag = Collections.synchronizedSet(new HashSet<>());

    public UtilityService(JavaPlugin plugin, ConfigManager cfg, MessageProvider msg, CooldownService cd){
        this.plugin = plugin; this.cfg = cfg; this.msg = msg; this.cd = cd;
    }

    public boolean changeGamemode(CommandSender sender, Player target, GameMode gm){
        target.setGameMode(gm);
        return true;
    }

    public boolean repair(Player p, boolean all){
        if (cd.isOnCooldown(p.getUniqueId(), "repair") && !p.hasPermission("fepboxutility.repair.bypass")) return false;
        PlayerInventory inv = p.getInventory();
        if (all){
            inv.forEach(this::repairItem);
            ItemStack[] armor = inv.getArmorContents();
            for(int i=0;i<armor.length;i++) repairItem(armor[i]);
            inv.setArmorContents(armor);
            repairItem(inv.getItemInOffHand());
        } else {
            repairItem(inv.getItemInMainHand());
        }
        cd.put(p.getUniqueId(), "repair", cfg.cooldown("repair"));
        return true;
    }

    private void repairItem(ItemStack item){
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType().getMaxDurability() > 0 && item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable dmg){
            dmg.setDamage(0);
            item.setItemMeta((org.bukkit.inventory.meta.Damageable) dmg);
        }
    }

    public void heal(Player p){
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setFireTicks(0);
    }

    public void feed(Player p){
        p.setFoodLevel(20);
        p.setSaturation(20f);
    }

    public void toggleFly(Player p, boolean state){
        p.setAllowFlight(state);
        p.setFlying(state);
    }

    public void freeze(Player p, boolean state){
        if (state) frozen.add(p.getUniqueId()); else frozen.remove(p.getUniqueId());
        p.setInvulnerable(state);
    }

    public boolean isFrozen(UUID id){ return frozen.contains(id); }

    public void rememberMsg(Player from, Player to){
        lastMsg.put(from.getUniqueId(), to.getUniqueId());
        lastMsg.put(to.getUniqueId(), from.getUniqueId());
    }

    public UUID lastMsg(UUID player){ return lastMsg.get(player); }

    public void kickAll(CommandSender sender, String reason, String bypassPermission){
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.hasPermission(bypassPermission) || p.hasPermission("fepboxutility.*")) return;
            p.kick(msg.mini().deserialize(reason));
        });
    }

    public void tagCombat(Player p){
        if (!cfg.raw().getBoolean("fly.disable-on-combat", true)) return;
        combatTag.add(p.getUniqueId());
        p.setAllowFlight(false);
        p.setFlying(false);
        int seconds = cfg.raw().getInt("fly.combat-tag-seconds", 15);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> combatTag.remove(p.getUniqueId()), seconds * 20L);
    }

    public boolean isCombatTagged(Player p){ return combatTag.contains(p.getUniqueId()); }
}
