package com.fepbox.utility;

import com.fepbox.utility.command.*;
import com.fepbox.utility.command.home.HomeCommands;
import com.fepbox.utility.command.tpa.TpaCommands;
import com.fepbox.utility.command.warp.WarpCommands;
import com.fepbox.utility.config.ConfigManager;
import com.fepbox.utility.config.MessageProvider;
import com.fepbox.utility.listener.CombatListener;
import com.fepbox.utility.listener.InventoryListener;
import com.fepbox.utility.listener.AliasListener;
import com.fepbox.utility.listener.PlayerMoveListener;
import com.fepbox.utility.service.*;
import com.fepbox.utility.storage.StorageType;
import com.fepbox.utility.storage.dao.HomeStorage;
import com.fepbox.utility.storage.dao.TpaStorage;
import com.fepbox.utility.storage.dao.WarpStorage;
import com.fepbox.utility.storage.impl.sql.*;
import com.fepbox.utility.storage.impl.yaml.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FepboxUtilityPlugin extends JavaPlugin {
    private ConfigManager cfg;
    private MessageProvider msg;
    private CooldownService cooldownService;
    private TeleportService tpService;
    private WarpService warpService;
    private HomeService homeService;
    private TpaService tpaService;
    private UtilityService utilService;
    private AliasService aliasService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("warps.yml", false);
        saveResource("homes.yml", false);
        saveResource("tpa.yml", false);

        this.cfg = new ConfigManager(this);
        this.msg = new MessageProvider(this);
        this.cooldownService = new CooldownService();
        this.tpService = new TeleportService(this, cfg, msg);

        StorageType type = cfg.storageType();
        WarpStorage warpStorage;
        HomeStorage homeStorage;
        TpaStorage tpaStorage;
        if (type == StorageType.YAML) {
            warpStorage = new WarpYamlStorage(this);
            homeStorage = new HomeYamlStorage(this);
            tpaStorage = new TpaYamlStorage(this);
        } else {
            var pool = cfg.databasePool();
            warpStorage = new WarpSqlStorage(this, pool);
            homeStorage = new HomeSqlStorage(this, pool);
            tpaStorage = new TpaSqlStorage(this, pool);
        }

        this.warpService = new WarpService(this, warpStorage, cooldownService, cfg, msg, tpService);
        this.homeService = new HomeService(this, homeStorage, cooldownService, cfg, msg, tpService);
        this.tpaService = new TpaService(this, tpaStorage, cooldownService, cfg, msg, tpService);
        this.utilService = new UtilityService(this, cfg, msg, cooldownService);
        this.aliasService = new AliasService(this, cfg);

        register(new GamemodeCommand(this, utilService, cfg, msg));
        register(new RepairCommand(this, utilService, cfg, msg));
        register(new HealCommand(this, utilService, cfg, msg));
        register(new FeedCommand(this, utilService, cfg, msg));
        register(new MsgCommand(this, utilService, cfg, msg));
        register(new ReplyCommand(this, utilService, cfg, msg));
        register(new HatCommand(this, utilService, cfg, msg));
        register(new EnderChestCommand(this, utilService, cfg, msg));
        register(new FlyCommand(this, utilService, cfg, msg));
        register(new FreezeCommand(this, utilService, cfg, msg));
        register(new KickAllCommand(this, utilService, cfg, msg));
        register(new ReloadCommand(this, cfg, msg, aliasService));
        register(new InvseeCommand(this, cfg, msg));
        register(new CreateAliasCommand(this, aliasService, cfg, msg));
        register(new DeleteAliasCommand(this, aliasService, cfg, msg));
        register(new TpbCommand(this, cfg, msg));
        new WarpCommands(this, warpService, cfg, msg).registerAll();
        new HomeCommands(this, homeService, cfg, msg).registerAll();
        new TpaCommands(this, tpaService, cfg, msg).registerAll();

        getServer().getPluginManager().registerEvents(new PlayerMoveListener(utilService, warpService, homeService, tpaService, cfg, msg), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this, cfg, msg), this);
        getServer().getPluginManager().registerEvents(new CombatListener(utilService, cfg), this);
        getServer().getPluginManager().registerEvents(new com.fepbox.utility.listener.ChatCreateWarpListener(warpService), this);
        getServer().getPluginManager().registerEvents(new AliasListener(aliasService), this);
    }

    private void register(BaseCommand cmd){
        PluginCommand pc = getCommand(cmd.getName());
        if (pc != null){
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }
    }
}
