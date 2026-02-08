package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.AliasService;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand extends BaseCommand {
    private final AliasService aliases;
    public ReloadCommand(JavaPlugin plugin, ConfigManager cfg, MessageProvider msg, AliasService aliases){
        super(plugin, "fpreload", cfg, msg);
        this.aliases = aliases;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!has(sender, "fepboxutility.reload")) { deny(sender); return true; }
        plugin.reloadConfig();
        cfg.raw().options().copyDefaults(true);
        aliases.load();
        msg.reload();
        msg.send(sender, "reload-done");
        return true;
    }
}
