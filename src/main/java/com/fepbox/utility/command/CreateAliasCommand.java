package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.AliasService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class CreateAliasCommand extends BaseCommand {
    private final AliasService aliases;
    public CreateAliasCommand(JavaPlugin plugin, AliasService aliases, ConfigManager cfg, MessageProvider msg){
        super(plugin, "createalias", cfg, msg);
        this.aliases = aliases;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("alias")) return true;
        if (!has(sender,"fepboxutility.alias")) { deny(sender); return true; }
        if (args.length < 2){
            usage(sender, "/createalias <alias> <komenda>");
            return true;
        }
        String alias = args[0].replaceFirst("^/","");
        String target = String.join(" ", java.util.Arrays.copyOfRange(args,1,args.length)).replaceFirst("^/+", "");
        if (aliases.exists(alias)){
            msg.send(sender,"alias-exists","<alias>", alias);
            return true;
        }
        aliases.add(alias, target);
        msg.send(sender,"alias-created","<alias>", alias, "<target>", target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1){
            // suggest current aliases to avoid duplicates
            String pref = args[0].toLowerCase();
            return aliases.all().keySet().stream()
                    .filter(a -> a.startsWith(pref))
                    .sorted()
                    .toList();
        }
        if (args.length == 2){
            String pref = args[1].toLowerCase();
            return plugin.getDescription().getCommands().keySet().stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(pref))
                    .sorted()
                    .toList();
        }
        return Collections.emptyList();
    }
}
