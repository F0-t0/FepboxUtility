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

public class RepairCommand extends BaseCommand {
    private final UtilityService util;

    public RepairCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin, "repair", cfg, msg);
        this.util = util;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("repair")) return true;
        if (!has(sender, "fepboxutility.repair")) { deny(sender); return true; }
        boolean all = args.length>0 && args[0].equalsIgnoreCase("all");
        if (all && !has(sender, "fepboxutility.repair.all")) { deny(sender); return true; }
        Player target;
        if (args.length>1) target = Bukkit.getPlayer(args[1]);
        else target = sender instanceof Player p ? p : null;
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        boolean ok = util.repair(target, all);
        if (!ok){ msg.send(sender,"cooldown","<seconds>", String.valueOf(cfg.cooldown("repair"))); return true; }
        msg.send(target, all?"repair-done-all":"repair-done");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new java.util.ArrayList<>();
            if ("all".startsWith(args[0].toLowerCase())) list.add("all");
            list.addAll(onlinePlayers(args[0]));
            return list;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("all")) return onlinePlayers(args[1]);
        return java.util.Collections.emptyList();
    }
}
