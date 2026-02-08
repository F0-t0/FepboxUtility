package com.fepbox.utility.listener;

import com.fepbox.utility.service.AliasService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class AliasListener implements Listener {
    private final AliasService service;
    public AliasListener(AliasService service){ this.service = service; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e){
        String rewritten = service.rewrite(e.getMessage());
        if (rewritten != null){
            e.setMessage(rewritten);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent e){
        String rewritten = service.rewrite("/" + e.getCommand());
        if (rewritten != null){
            e.setCommand(rewritten.substring(1)); // ServerCommandEvent command has no leading slash
        }
    }
}
