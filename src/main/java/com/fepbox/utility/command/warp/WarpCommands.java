package com.fepbox.utility.command.warp;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.Warp;
import com.fepbox.utility.service.WarpService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WarpCommands {
    private final JavaPlugin plugin;
    private final WarpService service;
    private final ConfigManager cfg;
    private final MessageProvider msg;

    public WarpCommands(JavaPlugin plugin, WarpService service, ConfigManager cfg, MessageProvider msg){
        this.plugin=plugin; this.service=service; this.cfg=cfg; this.msg=msg;
    }

    public void registerAll(){
        register("setwarp", this::setwarp);
        register("createwarp", this::setwarp);
        register("warp", this::warp);
        register("warps", this::warps);
        register("delwarp", this::delwarp);
        register("renamework", this::renamework);
        register("warpinfo", this::warpinfo);
    }

    private void register(String name, CommandExecutor exec){
        org.bukkit.command.PluginCommand cmd = plugin.getCommand(name);
        if (cmd!=null) cmd.setExecutor(exec);
    }

    private boolean setwarp(CommandSender sender, Command command, String label, String[] args){
        if (!cfg.module("warps")) return true;
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (!sender.hasPermission("fepboxutility.setwarp") && !sender.hasPermission("fepboxutility.*")) { msg.send(sender,"no-permission"); return true; }
        if (args.length<1){ msg.send(sender,"usage","<usage>","/setwarp <nazwa>"); return true; }
        service.setWarp(p, args[0]);
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
}
