package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FreezeCommand extends BaseCommand {
    private final UtilityService util;

    public FreezeCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin,"freeze", cfg, msg);
        this.util = util;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("freeze")) return true;
        if (args.length < 1){ usage(sender, "/freeze <gracz> [on|off]"); return true; }
        if (!has(sender,"fepboxutility.freeze")) { deny(sender); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        boolean state = args.length>1 ? args[1].equalsIgnoreCase("on") : !util.isFrozen(target.getUniqueId());
        util.freeze(target, state);
        msg.send(target, state?"freeze-on":"freeze-off");
        if (!target.equals(sender)) msg.send(sender, state?"freeze-on":"freeze-off");
        if (state){
            int timeout = cfg.raw().getInt("freeze.timeout", 120);
            plugin.getServer().getScheduler().runTaskLater(plugin, ()-> {
                util.freeze(target, false);
            }, timeout*20L);
        }
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) return onlinePlayers(args[0]);
        if (args.length == 2) return java.util.List.of("on","off").stream().filter(s->s.startsWith(args[1].toLowerCase())).toList();
        return java.util.Collections.emptyList();
    }
}
