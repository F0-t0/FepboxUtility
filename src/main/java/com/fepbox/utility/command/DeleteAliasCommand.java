package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.AliasService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DeleteAliasCommand extends BaseCommand {
    private final AliasService aliases;
    public DeleteAliasCommand(JavaPlugin plugin, AliasService aliases, ConfigManager cfg, MessageProvider msg){
        super(plugin, "deletealias", cfg, msg);
        this.aliases = aliases;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("alias")) return true;
        if (!has(sender,"fepboxutility.alias")) { deny(sender); return true; }
        if (args.length < 1){
            usage(sender, "/deletealias <alias>");
            return true;
        }
        String alias = args[0].replaceFirst("^/","");
        if (!aliases.remove(alias)){
            msg.send(sender,"alias-not-found","<alias>", alias);
            return true;
        }
        msg.send(sender,"alias-deleted","<alias>", alias);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1){
            String pref = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            aliases.all().keySet().stream()
                    .filter(a -> a.startsWith(pref))
                    .sorted()
                    .forEach(matches::add);
            return matches;
        }
        return java.util.Collections.emptyList();
    }
}
