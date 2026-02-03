package com.fepbox.utility.command.home;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Home;
import com.fepbox.utility.service.HomeService;
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

public class HomeCommands {
    private final JavaPlugin plugin;
    private final HomeService service;
    private final ConfigManager cfg;
    private final MessageProvider msg;

    public HomeCommands(JavaPlugin plugin, HomeService service, ConfigManager cfg, MessageProvider msg){
        this.plugin=plugin; this.service=service; this.cfg=cfg; this.msg=msg;
    }

    public void registerAll(){
        reg("sethome", this::sethome, null);
        reg("home", this::home, this::tabHome);
        reg("homes", this::homes, null);
        reg("delhome", this::delhome, this::tabHome);
        reg("renamehome", this::renamehome, this::tabRename);
    }

    private void reg(String name, CommandExecutor exec, TabCompleter tab){
        org.bukkit.command.PluginCommand cmd = plugin.getCommand(name);
        if (cmd!=null){
            cmd.setExecutor(exec);
            if (tab!=null) cmd.setTabCompleter(tab);
        }
    }

    private List<String> homeNames(Player p, String prefix){
        String low = prefix==null?"" : prefix.toLowerCase(Locale.ROOT);
        return service.list(p.getUniqueId()).stream()
                .map(Home::name)
                .filter(n->n.startsWith(low))
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean sethome(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("homes")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (!sender.hasPermission("fepboxutility.sethome") && !sender.hasPermission("fepboxutility.*")) { msg.send(sender,"no-permission"); return true; }
        String name = args.length>0 ? args[0] : "home";
        service.setHome(p, name);
        return true;
    }

    private boolean home(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("homes")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        String name = args.length>0 ? args[0] : "home";
        Home h = service.find(p, name);
        if (h==null){ msg.send(sender,"home-deleted","<name>", name); return true; }
        service.teleport(p, h);
        return true;
    }

    private boolean homes(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("homes")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        service.listHomes(p);
        return true;
    }

    private boolean delhome(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("homes")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (args.length<1){ msg.send(sender,"usage","<usage>","/delhome <nazwa>"); return true; }
        service.delete(p, args[0]);
        return true;
    }

    private boolean renamehome(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("homes")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (args.length<2){ msg.send(sender,"usage","<usage>","/renamehome <stara> <nowa>"); return true; }
        service.rename(p, args[0], args[1]);
        return true;
    }

    private List<String> tabHome(CommandSender sender, Command cmd, String alias, String[] args){
        if (!(sender instanceof Player p)) return Collections.emptyList();
        if (args.length==1) return homeNames(p, args[0]);
        return Collections.emptyList();
    }

    private List<String> tabRename(CommandSender sender, Command cmd, String alias, String[] args){
        if (!(sender instanceof Player p)) return Collections.emptyList();
        if (args.length==1) return homeNames(p, args[0]);
        if (args.length==2) return homeNames(p, args[1]);
        return Collections.emptyList();
    }
}
