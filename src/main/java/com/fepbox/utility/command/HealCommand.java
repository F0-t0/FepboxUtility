package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class HealCommand extends BaseCommand {
    private final UtilityService util;
    public HealCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){ super(plugin,"heal",cfg,msg); this.util=util; }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("heal")) return true;
        if (!has(sender,"fepboxutility.heal")) { deny(sender); return true; }
        Player target = args.length>0 ? Bukkit.getPlayer(args[0]) : sender instanceof Player p ? p : null;
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        util.heal(target);
        msg.send(target,"heal-done");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return onlinePlayers(args[0]);
        return java.util.Collections.emptyList();
    }
}
