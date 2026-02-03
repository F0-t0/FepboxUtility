package com.fepbox.utility.service;

import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.model.TpaRequest;
import com.fepbox.utility.storage.dao.TpaStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TpaService {
    private final JavaPlugin plugin;
    private final TpaStorage storage;
    private final CooldownService cd;
    private final ConfigManager cfg;
    private final MessageProvider msg;
    private final TeleportService tp;
    private final ConcurrentHashMap<UUID, Long> warmups = new ConcurrentHashMap<>();

    public TpaService(JavaPlugin plugin, TpaStorage storage, CooldownService cd, ConfigManager cfg, MessageProvider msg, TeleportService tp){
        this.plugin=plugin; this.storage=storage; this.cd=cd; this.cfg=cfg; this.msg=msg; this.tp=tp;
        storage.init();
    }

    public void sendRequest(Player sender, Player target, boolean here){
        if (storage.isToggled(target.getUniqueId())) { msg.send(sender, "tpa-denied"); return; }
        if (storage.isIgnoring(target.getUniqueId(), sender.getUniqueId())) { msg.send(sender, "tpa-denied"); return; }
        if (cd.isOnCooldown(sender.getUniqueId(), "tpa") && !sender.hasPermission("fepboxutility.tpa.bypass")){
            msg.send(sender,"cooldown","<seconds>", String.valueOf(cd.remaining(sender.getUniqueId(),"tpa"))); return;
        }
        long expires = Instant.now().getEpochSecond() + 60;
        storage.saveRequest(new TpaRequest(sender.getUniqueId(), target.getUniqueId(), expires, here));
        msg.send(sender, "tpa-sent", "<player>", target.getName());
        Component accept = Component.text("[AKCEPTUJ]").clickEvent(ClickEvent.runCommand("/tpaccept"));
        Component deny = Component.text("[ODRZUC]").clickEvent(ClickEvent.runCommand("/tpdeny"));
        target.sendMessage(msg.parse("<yellow>" + sender.getName() + " chce sie teleportowac. ")
                .append(Component.space()).append(accept).append(Component.space()).append(deny));
    }

    public void accept(Player target){
        long now = Instant.now().getEpochSecond();
        List<TpaRequest> reqs = storage.requestsTo(target.getUniqueId());
        reqs.stream().filter(r -> r.expiresAt() > now).findFirst().ifPresent(req -> {
            Player sender = Bukkit.getPlayer(req.sender());
            if (sender == null) { storage.removeRequest(req.sender(), target.getUniqueId()); return; }
            storage.removeRequest(req.sender(), target.getUniqueId());
            if (req.here()) tp.safeTeleport(sender, target.getLocation()); else tp.safeTeleport(target, sender.getLocation());
            msg.send(target,"tpa-accepted");
            msg.send(sender,"tpa-accepted");
            cd.put(target.getUniqueId(), "tpa", cfg.cooldown("tpa"));
        });
    }

    public void deny(Player target){
        storage.requestsTo(target.getUniqueId()).forEach(r -> storage.removeRequest(r.sender(), r.target()));
        msg.send(target,"tpa-denied");
    }

    public void cancel(Player sender){ storage.clearAllFrom(sender.getUniqueId()); msg.send(sender,"tpa-denied"); }

    public void toggle(Player p){ boolean state = storage.toggle(p.getUniqueId()); msg.send(p, state?"ignore-added":"ignore-removed", "<player>", "TPA"); }

    public void ignore(Player p, Player target){
        boolean state = !storage.isIgnoring(p.getUniqueId(), target.getUniqueId());
        storage.ignore(p.getUniqueId(), target.getUniqueId(), state);
        msg.send(p, state?"ignore-added":"ignore-removed", "<player>", target.getName());
    }
}
