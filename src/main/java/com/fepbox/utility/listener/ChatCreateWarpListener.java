package com.fepbox.utility.listener;

import com.fepbox.utility.service.WarpService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatCreateWarpListener implements Listener {
    private final WarpService service;

    public ChatCreateWarpListener(WarpService service){ this.service = service; }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (service.completeCreation(e.getPlayer(), e.getMessage())){
            e.setCancelled(true);
        }
    }
}
