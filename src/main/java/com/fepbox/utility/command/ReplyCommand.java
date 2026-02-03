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

public class ReplyCommand extends BaseCommand {
    private final UtilityService util;

    public ReplyCommand(JavaPlugin plugin, UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin, "r", cfg, msg);
        this.util = util;
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("msg")) return true;
        if (!(sender instanceof Player sp)){ msg.send(sender, "player-only"); return true; }
        if (args.length < 1){ usage(sender, "/r <wiadomosc>"); return true; }
        var last = util.lastMsg(sp.getUniqueId());
        if (last == null){ msg.send(sender, "reply-missing"); return true; }
        Player target = Bukkit.getPlayer(last);
        if (target == null){ msg.send(sender,"invalid-player"); return true; }
        String message = String.join(" ", args);
        Component comp = msg.mini().deserialize("<gray>[<green>MSG</green>] " + sp.getName() + " -> " + target.getName() + ": " + message)
                .hoverEvent(HoverEvent.showText(Component.text("Kliknij aby odpisac")))
                .clickEvent(ClickEvent.suggestCommand("/r "));
        sp.sendMessage(comp);
        target.sendMessage(comp);
        util.rememberMsg(sp, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return java.util.Collections.singletonList("<wiadomosc>");
        return java.util.Collections.emptyList();
    }
}
