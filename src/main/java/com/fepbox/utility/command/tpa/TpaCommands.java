package com.fepbox.utility.command.tpa;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.TpaService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TpaCommands {
    private final JavaPlugin plugin;
    private final TpaService service;
    private final ConfigManager cfg;
    private final MessageProvider msg;

    public TpaCommands(JavaPlugin plugin, TpaService service, ConfigManager cfg, MessageProvider msg){
        this.plugin=plugin; this.service=service; this.cfg=cfg; this.msg=msg;
    }

    public void registerAll(){
        reg("tpa", this::tpa, this::tabPlayers);
        reg("tpahere", this::tpahere, this::tabPlayers);
        reg("tpaccept", this::accept, null);
        reg("tpdeny", this::deny, null);
        reg("tpacancel", this::cancel, null);
        reg("tpacancelall", this::cancel, null);
        reg("tpatoggle", this::toggle, null);
        reg("tpignore", this::ignore, this::tabPlayers);
    }

    private void reg(String name, CommandExecutor exec, TabCompleter tab){
        org.bukkit.command.PluginCommand cmd = plugin.getCommand(name);
        if (cmd!=null){
            cmd.setExecutor(exec);
            if (tab!=null) cmd.setTabCompleter(tab);
        }
    }

    private List<String> online(String prefix){
        String low = prefix==null?"" : prefix.toLowerCase(Locale.ROOT);
        return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n->n.toLowerCase(Locale.ROOT).startsWith(low))
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean tpa(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("tpa")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (args.length<1){ msg.send(sender,"usage","<usage>","/tpa <gracz>"); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        service.sendRequest(p, target, false);
        return true;
    }

    private boolean tpahere(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (args.length<1){ msg.send(sender,"usage","<usage>","/tpahere <gracz>"); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        service.sendRequest(p, target, true);
        return true;
    }

    private boolean accept(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        service.accept(p);
        return true;
    }

    private boolean deny(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        service.deny(p);
        return true;
    }

    private boolean cancel(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        service.cancel(p);
        return true;
    }

    private boolean toggle(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        service.toggle(p);
        return true;
    }

    private boolean ignore(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (args.length<1){ msg.send(sender,"usage","<usage>","/tpignore <gracz>"); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        service.ignore(p, target);
        return true;
    }

    private List<String> tabPlayers(CommandSender sender, Command cmd, String alias, String[] args){
        if (args.length==1) return online(args[0]);
        return Collections.emptyList();
    }
}
