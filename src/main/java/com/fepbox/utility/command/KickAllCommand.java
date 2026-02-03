package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class KickAllCommand extends BaseCommand {
    private final UtilityService util;
    public KickAllCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){ super(plugin,"kickall", cfg, msg); this.util=util; }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("kickall")) return true;
        if (!has(sender,"fepboxutility.kickall")) { deny(sender); return true; }
        String reason = args.length>0 ? String.join(" ", args) : "Kicked";
        util.kickAll(sender, reason, "fepboxutility.kickall.bypass");
        msg.send(sender,"kickall","<reason>", reason);
        return true;
    }
}
