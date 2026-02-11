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
        ensureResource("config.yml");
        ensureResource("messages.yml");
        ensureResource("warps.yml");
        ensureResource("homes.yml");
        ensureResource("tpa.yml");

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

    private void ensureResource(String name){
        if (getResource(name) != null){
            saveResource(name, false);
            return;
        }
        var outFile = new java.io.File(getDataFolder(), name);
        if (outFile.exists()) return;
        try {
            outFile.getParentFile().mkdirs();
            String content = defaultResource(name);
            if (content == null){
                getLogger().severe("Embedded resource missing and no fallback for " + name);
                return;
            }
            java.nio.file.Files.writeString(outFile.toPath(), content);
            getLogger().warning("Wrote fallback " + name + " because embedded resource was missing.");
        } catch (Exception e){
            getLogger().severe("Failed to write fallback " + name + ": " + e.getMessage());
        }
    }

    private String defaultResource(String name){
        return switch (name) {
            case "config.yml" -> """
storage:
  type: YAML
  mysql:
    host: localhost
    port: 3306
    database: fepbox
    user: root
    password: password
modules:
  gm: true
  repair: true
  heal: true
  feed: true
  msg: true
  hat: true
  ec: true
  fly: true
  freeze: true
  kickall: true
  warps: true
  homes: true
  tpa: true
  alias: true
  tpb: true
cooldowns:
  repair: 60
  fly: 0
  warp: 5
  home: 5
  tpa: 5
warmups:
  warp: 3
  home: 3
teleport:
  cancel-on-move: true
  safe: true
  blocked-worlds: []
warps:
  gui-size: 54
  blacklist: []
homes:
  gui-slots: 5
  default-limit: 1
  ranks:
    vip: 3
    mvp: 5
fly:
  disable-on-combat: true
  combat-tag-seconds: 15
freeze:
  timeout: 120
  bypass-permission: fepboxutility.freeze.bypass
enderchest:
  admin-logging: true
  readonly: true
invsee:
  readonly: false
aliases: {}
""";
            case "messages.yml" -> """
prefix: "<gray>[<aqua>Fepbox</aqua>] "
messages:
  no-permission: "<red>Brak uprawnien.</red>"
  player-only: "<red>Komenda tylko dla graczy.</red>"
  invalid-player: "<red>Nie znaleziono gracza.</red>"
  usage: "<red>Uzycie: <usage></red>"
  cooldown: "<red>Poczekaj <seconds>s.</red>"
  warmup: "<yellow>Teleport za <seconds>s...</yellow>"
  teleport-cancelled: "<red>Teleport anulowany.</red>"
  gm-changed: "<green>Tryb gry: <mode></green>"
  gm-changed-other: "<green>Ustawiono tryb <mode> dla <player></green>"
  repair-done: "<green>Naprawiono przedmiot.</green>"
  repair-done-all: "<green>Naprawiono wszystkie przedmioty.</green>"
  heal-done: "<green>Uleczono.</green>"
  feed-done: "<green>Najedzony.</green>"
  msg-format: "<gray>[<green>MSG</green>] <sender> -> <receiver>: <message>"
  reply-missing: "<red>Brak rozmowcy.</red>"
  hat-set: "<green>Zalozono przedmiot na glowe.</green>"
  hat-cleared: "<green>Usunieto kapelusz.</green>"
  ec-opened: "<green>Otworzono enderchest.</green>"
  fly-on: "<green>Tryb latania ON.</green>"
  fly-off: "<red>Tryb latania OFF.</red>"
  freeze-on: "<red>Jestes zamrozony!</red>"
  freeze-off: "<green>Odblokowano.</green>"
  kickall: "<red>Wyrzucono wszystkich: <reason></red>"
  warp-set: "<green>Utworzono warp <name>.</green>"
  warp-teleport: "<green>Teleportacja do <name>.</green>"
  warp-deleted: "<green>Usunieto warp <name>.</green>"
  warp-rename: "<green>Zmieniono nazwe na <name>.</green>"
  warp-info: "<yellow><name></yellow> (<world> <x> <y> <z>)"
  home-set: "<green>Ustawiono home <name>.</green>"
  home-teleport: "<green>Teleport do home <name>.</green>"
  home-deleted: "<green>Usunieto home <name>.</green>"
  home-rename: "<green>Zmieniono nazwe na <name>.</green>"
  tpa-sent: "<yellow>Wyslano prosbe do <player>.</yellow>"
  tpa-received: "<yellow><player> chce sie teleportowac. <click:run_command:/tpaccept><green>[AKCEPTUJ]</green></click> <click:run_command:/tpdeny><red>[ODRZUC]</red></click></yellow>"
  tpa-accepted: "<green>Teleport zaakceptowany.</green>"
  tpa-denied: "<red>Odrzucono teleport.</red>"
  tpa-timeout: "<red>Prosba wygasla.</red>"
  teleport-safe-fail: "<red>Nie znaleziono bezpiecznego miejsca.</red>"
  ignore-added: "<yellow>Ignorujesz <player>.</yellow>"
  ignore-removed: "<yellow>Przestales ignorowac <player>.</yellow>"
  reload-done: "<green>Przeladowano konfiguracje.</green>"
  invsee-opened: "<green>Podgladasz ekwipunek <player>.</green>"
  alias-created: "<green>Dodano alias <alias> -> /<target></green>"
  alias-deleted: "<green>Usunieto alias <alias>.</green>"
  alias-exists: "<red>Alias <alias> juz istnieje.</red>"
  alias-not-found: "<red>Alias <alias> nie istnieje.</red>"
  tpb-done-self: "<green>Przeteleportowano na srodek bloku.</green>"
  gui.home.title: "<gray>Twoje home</gray>"
  gui.home.locked-name: "<red>Home <n> (zablokowany)</red>"
  gui.home.locked-lore: "<red>Brak permisji: home.<n>"
  gui.home.empty-name: "<yellow>Home <n> (pusty)</yellow>"
  gui.home.empty-lore-teleport: "<gray>LPM: teleport (brak celu)"
  gui.home.empty-lore-set: "<yellow>PPM: ustaw tutaj"
  gui.home.saved-name: "<green>Home <n></green>"
  gui.home.saved-lore-status: "<green>Zapisany</green>"
  gui.home.saved-lore-teleport: "<gray>LPM: teleport</gray>"
  gui.home.saved-lore-set: "<yellow>PPM: ustaw tutaj</yellow>"
  title.teleport.header: "<yellow>TELEPORTACJA</yellow>"
  title.teleport.sub: "<gray>za <seconds> sekundy</gray>"
""";
            case "warps.yml" -> "warps: {}\n";
            case "homes.yml" -> "homes: {}\n";
            case "tpa.yml" -> "requests: {}\n";
            default -> null;
        };
    }

    private void register(BaseCommand cmd){
        PluginCommand pc = getCommand(cmd.getName());
        if (pc != null){
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }
    }
}
