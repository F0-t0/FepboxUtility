package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class InvseeCommand extends BaseCommand {
    public InvseeCommand(JavaPlugin plugin, ConfigManager cfg, MessageProvider msg){
        super(plugin, "invsee", cfg, msg);
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("warps")) {} // module guard not needed; keep base
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (!has(sender,"fepboxutility.invsee")) { deny(sender); return true; }
        if (args.length<1){ usage(sender,"/invsee <gracz>"); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        p.openInventory(target.getInventory());
        msg.send(p,"invsee-opened","<player>", target.getName());
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) return onlinePlayers(args[0]);
        return java.util.Collections.emptyList();
    }
}
