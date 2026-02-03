package com.fepbox.utility.command;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HatCommand extends BaseCommand {
    public HatCommand(JavaPlugin plugin, com.fepbox.utility.service.UtilityService util, ConfigManager cfg, MessageProvider msg){
        super(plugin,"hat", cfg, msg);
    }

    @Override
    protected boolean handle(CommandSender sender, String[] args) {
        if (!cfg.module("hat")) return true;
        if (!has(sender,"fepboxutility.hat")) { deny(sender); return true; }
        if (!(sender instanceof Player p)){ msg.send(sender,"player-only"); return true; }
        if (args.length>0 && args[0].equalsIgnoreCase("off")){
            p.getInventory().setHelmet(new ItemStack(Material.AIR));
            msg.send(p,"hat-cleared");
            return true;
        }
        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType()==Material.AIR){ msg.send(p,"usage","<usage>","/hat"); return true; }
        ItemStack old = p.getInventory().getHelmet();
        p.getInventory().setHelmet(inHand);
        p.getInventory().setItemInMainHand(old);
        msg.send(p,"hat-set");
        return true;
    }
}
