package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.UtilityService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MsgCommand extends BaseCommand {
    private final UtilityService util;

    public MsgCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin, "msg", cfg, msg);
        this.util = util;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("msg")) return true;
        if (args.length < 2){ usage(sender, "/msg <gracz> <wiadomosc>"); return true; }
        if (!(sender instanceof Player sp)){ msg.send(sender, "player-only"); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null){ msg.send(sender,"invalid-player"); return true; }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args,1,args.length));
        Component comp = msg.mini().deserialize("<gray>[<green>MSG</green>] " + sp.getName() + " -> " + target.getName() + ": " + message);
        Component click = comp.hoverEvent(HoverEvent.showText(Component.text("Kliknij aby odpisac")))
                .clickEvent(ClickEvent.suggestCommand("/r "));
        sp.sendMessage(click);
        target.sendMessage(click);
        util.rememberMsg(sp, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return onlinePlayers(args[0]);
        return java.util.Collections.emptyList();
    }
}
