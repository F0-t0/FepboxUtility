package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        Player target;
        if (args.length>1) target = Bukkit.getPlayer(args[1]);
        else target = sender instanceof Player p ? p : null;
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        boolean ok = util.repair(target, all);
        if (!ok){ msg.send(sender,"cooldown","<seconds>", String.valueOf(cfg.cooldown("repair"))); return true; }
        msg.send(target, all?"repair-done-all":"repair-done");
        return true;
    }
}
