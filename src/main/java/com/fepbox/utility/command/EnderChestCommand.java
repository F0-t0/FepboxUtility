package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EnderChestCommand extends BaseCommand {
    public EnderChestCommand(JavaPlugin plugin, com.fepbox.utility.service.UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin,"ec", cfg, msg);
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("ec")) return true;
        if (!has(sender,"fepboxutility.ec")) { deny(sender); return true; }
        Player target = args.length>0 ? Bukkit.getPlayer(args[0]) : sender instanceof Player p ? p : null;
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        if (sender instanceof Player sp){
            if (!sp.equals(target) && !has(sp, "fepboxutility.ec.other")) { deny(sp); return true; }
            if (cfg.raw().getBoolean("enderchest.readonly", true) && !sp.equals(target)){
                sp.openInventory(target.getEnderChest());
                return true;
            }
            sp.openInventory(target.getEnderChest());
            if (cfg.raw().getBoolean("enderchest.admin-logging", true) && !sp.equals(target)){
                plugin.getLogger().info(sp.getName() + " opened enderchest of " + target.getName());
            }
        }
        msg.send(sender,"ec-opened");
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) return onlinePlayers(args[0]);
        return java.util.Collections.emptyList();
    }
}
