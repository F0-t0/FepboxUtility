package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class TpbCommand extends BaseCommand {
    public TpbCommand(JavaPlugin plugin, ConfigManager cfg, MessageProvider msg){
        super(plugin, "tpb", cfg, msg);
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("tpb")) return true;
        if (!has(sender, "fepboxutility.tpb")) { deny(sender); return true; }
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }

        Location base = p.getLocation();
        Location dest = new Location(
                base.getWorld(),
                Math.floor(base.getX()) + 0.5,
                base.getY(),
                Math.floor(base.getZ()) + 0.5,
                base.getYaw(),
                base.getPitch()
        );
        p.teleport(dest);
        msg.send(sender, "tpb-done-self");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
