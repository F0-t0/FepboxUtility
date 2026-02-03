package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand extends BaseCommand {
    public ReloadCommand(JavaPlugin plugin, ConfigManager cfg, MessageProvider msg){
        super(plugin, "fpreload", cfg, msg);
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!has(sender, "fepboxutility.reload")) { deny(sender); return true; }
        plugin.reloadConfig();
        cfg.raw().options().copyDefaults(true);
        msg.reload();
        msg.send(sender, "reload-done");
        return true;
    }
}
