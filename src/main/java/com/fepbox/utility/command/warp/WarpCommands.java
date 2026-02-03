package com.fepbox.utility.command.warp;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Warp;
import com.fepbox.utility.service.WarpService;
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

public class WarpCommands {
    private final JavaPlugin plugin;
    private final WarpService service;
    private final ConfigManager cfg;
    private final MessageProvider msg;

    public WarpCommands(JavaPlugin plugin, WarpService service, ConfigManager cfg, MessageProvider msg){
        this.plugin=plugin; this.service=service; this.cfg=cfg; this.msg=msg;
    }

    public void registerAll(){
        register("setwarp", this::setwarp, null);
        register("createwarp", this::createwarp, null);
        register("warp", this::warp, this::tabWarp);
        register("warps", this::warps, null);
        register("delwarp", this::delwarp, this::tabWarpNames);
        register("renamework", this::renamework, this::tabRename);
        register("warpinfo", this::warpinfo, this::tabWarpNames);
    }

    private void register(String name, CommandExecutor exec, TabCompleter tab){
        org.bukkit.command.PluginCommand cmd = plugin.getCommand(name);
        if (cmd!=null){
            cmd.setExecutor(exec);
            if (tab!=null) cmd.setTabCompleter(tab);
        }
    }

    private List<String> warpNames(String prefix){
        String low = prefix==null?"" : prefix.toLowerCase(Locale.ROOT);
        return service.list().stream()
                .map(Warp::name)
                .filter(n->n.startsWith(low))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> online(String prefix){
        String low = prefix==null?"" : prefix.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n->n.toLowerCase(Locale.ROOT).startsWith(low))
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean setwarp(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (!sender.hasPermission("fepboxutility.setwarp") && !sender.hasPermission("fepboxutility.*")) { msg.send(sender,"no-permission"); return true; }
        if (args.length<1){ msg.send(sender,"usage","<usage>","/setwarp <nazwa>"); return true; }
        service.setWarp(p, args[0]);
        return true;
    }

    private boolean createwarp(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (!sender.hasPermission("fepboxutility.setwarp") && !sender.hasPermission("fepboxutility.*")) { msg.send(sender,"no-permission"); return true; }
        new com.fepbox.utility.gui.CreateWarpGui(plugin, p, service, msg, cfg.raw().getInt("warps.gui-size", 54)).open();
        return true;
    }

    private boolean warp(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (args.length<1){ msg.send(sender,"usage","<usage>","/warp <nazwa> [gracz]"); return true; }
        Warp warp = service.find(args[0]);
        if (warp == null){ msg.send(sender,"warp-info","<name>", args[0]); return true; }
        Player target = args.length>1 ? Bukkit.getPlayer(args[1]) : sender instanceof Player p ? p : null;
        if (target==null){ msg.send(sender,"invalid-player"); return true; }
        if (!target.hasPermission("fepboxutility.warp."+warp.name()) && !target.hasPermission("fepboxutility.*")) { msg.send(sender,"no-permission"); return true; }
        service.teleport(target, warp);
        return true;
    }

    private boolean warps(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        service.openGui(p);
        return true;
    }

    private boolean delwarp(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (!sender.hasPermission("fepboxutility.delwarp") && !sender.hasPermission("fepboxutility.*")) { msg.send(sender,"no-permission"); return true; }
        if (args.length<1){ msg.send(sender,"usage","<usage>","/delwarp <nazwa>"); return true; }
        service.delete((Player)sender, args[0]);
        return true;
    }

    private boolean renamework(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (!sender.hasPermission("fepboxutility.renamework") && !sender.hasPermission("fepboxutility.*")) { msg.send(sender,"no-permission"); return true; }
        if (args.length<2){ msg.send(sender,"usage","<usage>","/renamework <stara> <nowa>"); return true; }
        service.rename((Player)sender, args[0], args[1]);
        return true;
    }

    private boolean warpinfo(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (args.length<1){ msg.send(sender,"usage","<usage>","/warpinfo <nazwa>"); return true; }
        Warp warp = service.find(args[0]);
        if (warp==null){ msg.send(sender,"warp-info","<name>", args[0]); return true; }
        msg.send(sender,"warp-info","<name>", warp.name(), "<world>", warp.world(), "<x>", String.valueOf(warp.x()), "<y>", String.valueOf(warp.y()), "<z>", String.valueOf(warp.z()));
        return true;
    }

    private List<String> tabWarp(CommandSender sender, Command cmd, String alias, String[] args){
        if (args.length==1) return warpNames(args[0]);
        if (args.length==2) return online(args[1]);
        return Collections.emptyList();
    }

    private List<String> tabWarpNames(CommandSender sender, Command cmd, String alias, String[] args){
        if (args.length==1) return warpNames(args[0]);
        return Collections.emptyList();
    }

    private List<String> tabRename(CommandSender sender, Command cmd, String alias, String[] args){
        if (args.length==1) return warpNames(args[0]);
        if (args.length==2) return warpNames(args[1]);
        return Collections.emptyList();
    }
}
