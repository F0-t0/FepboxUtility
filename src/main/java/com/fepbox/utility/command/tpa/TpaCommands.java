package com.fepbox.utility.command.tpa;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.service.TpaService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TpaCommands {
    private final JavaPlugin plugin;
    private final TpaService service;
    private final ConfigManager cfg;
    private final MessageProvider msg;

    public TpaCommands(JavaPlugin plugin, TpaService service, ConfigManager cfg, MessageProvider msg){
        this.plugin=plugin; this.service=service; this.cfg=cfg; this.msg=msg;
    }

    public void registerAll(){
        reg("tpa", this::tpa);
        reg("tpahere", this::tpahere);
        reg("tpaccept", this::accept);
        reg("tpdeny", this::deny);
        reg("tpacancel", this::cancel);
        reg("tpacancelall", this::cancel);
        reg("tpatoggle", this::toggle);
        reg("tpignore", this::ignore);
    }

    private void reg(String name, CommandExecutor exec){
        org.bukkit.command.PluginCommand cmd = plugin.getCommand(name);
        if (cmd!=null) cmd.setExecutor(exec);
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
}
