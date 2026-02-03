package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FlyCommand extends BaseCommand {
    private final UtilityService util;

    public FlyCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin,"fly", cfg, msg);
        this.util = util;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("fly")) return true;
        if (!has(sender,"fepboxutility.fly")) { deny(sender); return true; }
        Player target = args.length>0 && Bukkit.getPlayer(args[0])!=null ? Bukkit.getPlayer(args[0]) : sender instanceof Player p ? p : null;
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        boolean state;
        if (args.length>1){ state = args[1].equalsIgnoreCase("on"); }
        else state = !target.getAllowFlight();
        if (util.isCombatTagged(target) && !sender.hasPermission("fepboxutility.fly.bypass")){
            msg.send(sender,"cooldown","<seconds>", String.valueOf(cfg.raw().getInt("fly.combat-tag-seconds"))); return true;
        }
        util.toggleFly(target, state);
        msg.send(target, state?"fly-on":"fly-off");
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) return onlinePlayers(args[0]);
        if (args.length == 2) return java.util.List.of("on","off").stream().filter(s->s.startsWith(args[1].toLowerCase())).toList();
        return java.util.Collections.emptyList();
    }
}
