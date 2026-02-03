package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FeedCommand extends BaseCommand {
    private final UtilityService util;
    public FeedCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){ super(plugin,"feed",cfg,msg); this.util=util; }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("feed")) return true;
        if (!has(sender,"fepboxutility.feed")) { deny(sender); return true; }
        Player target = args.length>0 ? Bukkit.getPlayer(args[0]) : sender instanceof Player p ? p : null;
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        util.feed(target);
        msg.send(target,"feed-done");
        return true;
    }
}
