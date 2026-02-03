package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GamemodeCommand extends BaseCommand {
    private final UtilityService util;

    public GamemodeCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin, "gm", cfg, msg);
        this.util = util;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("gm")) return true;
        if (args.length < 1){ usage(sender, "/gm <0|1|2|3|s|c|a|sp> [gracz]"); return true; }
        String modeArg = args[0].toLowerCase();
        GameMode gm = switch (modeArg) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
        if (gm == null){ usage(sender, "/gm <0|1|2|3|s|c|a|sp> [gracz]"); return true; }
        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : sender instanceof Player p ? p : null;
        if (target == null){ msg.send(sender, "invalid-player"); return true; }
        if (!has(sender, "fepboxutility.gm")) { deny(sender); return true; }
        util.changeGamemode(sender, target, gm);
        msg.send(target, "gm-changed", "<mode>", gm.name());
        if (!target.equals(sender)) msg.send(sender, "gm-changed-other", "<mode>", gm.name(), "<player>", target.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.List.of("0","1","2","3","s","c","a","sp").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2) return onlinePlayers(args[1]);
        return java.util.Collections.emptyList();
    }
}
