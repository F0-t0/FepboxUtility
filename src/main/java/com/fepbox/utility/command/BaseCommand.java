package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    protected final JavaPlugin plugin;
    protected final ConfigManager cfg;
    protected final MessageProvider msg;
    private final String name;

    protected BaseCommand(JavaPlugin plugin, String name, ConfigManager cfg, MessageProvider msg) {
        this.plugin = plugin;
        this.name = name;
        this.cfg = cfg;
        this.msg = msg;
    }

    public String getName() { return name; }

    protected boolean has(CommandSender sender, String perm) {
        return sender.hasPermission("fepboxutility.*") || sender.hasPermission(perm);
    }

    protected void deny(CommandSender sender) { msg.send(sender, "no-permission"); }

    protected void usage(CommandSender sender, String usage) {
        msg.send(sender, "usage", "<usage>", usage);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return handle(sender, args);
    }

    protected abstract boolean handle(CommandSender sender, String[] args);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }

    protected boolean ensurePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            msg.send(sender, "player-only");
            return false;
        }
        return true;
    }

    protected List<String> onlinePlayers(String prefix) {
        String low = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(low))
                .sorted()
                .collect(Collectors.toList());
    }
}
