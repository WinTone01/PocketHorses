package it.pika.pockethorses;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.tchristofferson.configupdater.ConfigUpdater;
import fr.minuskube.inv.InventoryManager;
import io.papermc.lib.PaperLib;
import it.pika.libs.chat.Chat;
import it.pika.libs.config.Config;
import it.pika.libs.reflection.Reflections;
import it.pika.pockethorses.api.events.HorsesInitializeEvent;
import it.pika.pockethorses.commands.HorsesCmd;
import it.pika.pockethorses.commands.MainCmd;
import it.pika.pockethorses.enums.EconomyType;
import it.pika.pockethorses.enums.StorageType;
import it.pika.pockethorses.hooks.ModelEngineHook;
import it.pika.pockethorses.hooks.PlaceholdersHook;
import it.pika.pockethorses.hooks.WorldGuardHook;
import it.pika.pockethorses.hooks.economy.Economy;
import it.pika.pockethorses.hooks.economy.impl.PlayerPointsEconomy;
import it.pika.pockethorses.hooks.economy.impl.VaultEconomy;
import it.pika.pockethorses.listeners.HorseListener;
import it.pika.pockethorses.listeners.PlayerListener;
import it.pika.pockethorses.listeners.VoucherListener;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import it.pika.pockethorses.objects.items.Supplement;
import it.pika.pockethorses.storage.Storage;
import it.pika.pockethorses.storage.impl.JSON;
import it.pika.pockethorses.storage.impl.MySQL;
import it.pika.pockethorses.storage.impl.SQLite;
import it.pika.pockethorses.utils.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance = null;
    @Getter
    private static final Logger console = new Logger("PocketHorses");
    @Getter
    private static Storage storage = null;
    @Getter
    private static InventoryManager inventoryManager = null;
    @Getter
    private static Economy economy = null;
    @Getter
    private static final Cooldown cooldownManager = new Cooldown();
    @Getter
    private static ModelEngineHook modelEngineHook = null;
    @Getter
    private static PlaceholdersHook placeholdersHook = null;
    @Getter
    private static WorldGuardHook worldGuardHook = null;
    @Getter
    private static LanguageManager languageManager = null;


    @Getter
    private static Config configFile = null;
    @Getter
    private static Config vouchersFile = null;
    @Getter
    private static Config itemsFile = null;


    @Getter
    @Setter
    private static List<Horse> cache = Lists.newArrayList();
    @Getter
    private static final List<ConfigHorse> loadedHorses = Lists.newArrayList();
    @Getter
    private static final Map<String, List<SpawnedHorse>> spawnedHorses = Maps.newHashMap();
    @Getter
    private static final Map<String, Horse> inHorseStorage = Maps.newHashMap();
    @Getter
    private static final Map<UUID, Supplement> activeSupplements = Maps.newHashMap();
    @Getter
    private static final List<String> autoRemove = Lists.newArrayList();


    @Getter
    private static boolean shopEnabled;
    @Getter
    private static boolean placeholdersEnabled = false;
    @Getter
    private static boolean worldGuardEnabled = false;
    @Getter
    private static boolean modelEngineEnabled = false;


    public static final String VERSION = "1.9.0";

    @Override
    public void onLoad() {
        setupWorldGuard();

        if (!setupPlaceholders())
            console.warning("PlaceholderAPI not found, you will not be able to use placeholders!");
    }

    @Override
    public void onEnable() {
        instance = this;
        var stopwatch = Stopwatch.createStarted();

        console.info("§6  ____  _   _ ");
        console.info("§6 |  _ \\| | | |");
        console.info("§6 | |_) | |_| |    §6Pocket§eHorses §7v%s §8| §aEnabling..".formatted(VERSION));
        console.info("§6 |  __/|  _  |    §7Made with §clove §7and §epizza §7by §bzPikaa§7.");
        console.info("§6 |_|   |_| |_|");
        console.info("§6              ");

        if (Reflections.getNumericalVersion() < 13) {
            stopwatch.stop();
            console.warning("Server version not supported, disabling the plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        setupFiles();
        if (!setupStorage()) {
            stopwatch.stop();
            console.warning("Couldn't setup storage, check your config.yml! Disabling the plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            shopEnabled = false;
            console.warning("Couldn't setup economy, you will not be able to use shop-related features!");
        }

        registerListeners();
        registerCommands();
        loadHorses();
        setupInventories();
        setupModelEngine();
        checkForUpdates();

        if (isFolia())
            console.info("It sounds like you are using Folia. " +
                    "If you experience an issue while using it please report it on my Discord server.");

        stopwatch.stop();
        Bukkit.getPluginManager().callEvent(new HorsesInitializeEvent(this));
        new Metrics(this, 19134);

        PaperLib.suggestPaper(this);
        console.info("Plugin enabled in %s ms.".formatted(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        console.info("For support join my Discord server: dsc.gg/pockethorses");
    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.close();

        for (List<SpawnedHorse> value : spawnedHorses.values())
            for (SpawnedHorse horse : value)
                horse.getEntity().remove();
    }

    @SneakyThrows
    private void setupFiles() {
        configFile = new Config(this, "config.yml");
        vouchersFile = new Config(this, "vouchers.yml");
        itemsFile = new Config(this, "items.yml");

        ConfigUpdater.update(this, "config.yml", configFile.getFile());
        configFile.reload();

        var messages = new File(getDataFolder(), "messages.yml");
        if (messages.exists())
            Files.delete(messages.toPath());

        languageManager = new LanguageManager();
        languageManager.init();

        shopEnabled = configFile.getBoolean("Options.Shop-Enabled");

        var horsesFolder = new File(getDataFolder() + File.separator + "Horses");
        if (!horsesFolder.exists()) {
            if (!horsesFolder.mkdir())
                console.warning("An error occurred");
        }
    }

    private boolean setupStorage() {
        StorageType type;
        try {
            type = StorageType.valueOf(Objects.requireNonNull(configFile.getString("Storage.Type")).toUpperCase());
        } catch (IllegalArgumentException e) {
            console.warning("Database type not recognized!");
            return false;
        }

        switch (type) {
            case MYSQL -> {
                storage = new MySQL(configFile.getString("Storage.MySQL.Host"),
                        configFile.getInt("Storage.MySQL.Port"),
                        configFile.getString("Storage.MySQL.Database"),
                        configFile.getString("Storage.MySQL.Username"),
                        configFile.getString("Storage.MySQL.Password"));
                storage.init();
            }
            case SQLITE -> {
                storage = new SQLite();
                storage.init();
            }
            case JSON -> {
                storage = new JSON();
                storage.init();
            }
        }

        console.info("Storage type: %s".formatted(type));
        return true;
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new HorseListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new VoucherListener(), this);
    }

    private void registerCommands() {
        new MainCmd(this, "pockethorses", configFile.getStringList("Commands.PocketHorses.Aliases"));
        new HorsesCmd(this, "horses", configFile.getStringList("Commands.Horses.Aliases"));
    }

    public void loadHorses() {
        var horsesFolder = new File(getDataFolder() + File.separator + "Horses");
        if (!horsesFolder.exists())
            return;

        console.info(""); // For a good appearance
        for (File file : Objects.requireNonNull(horsesFolder.listFiles())) {
            if (!file.getName().endsWith(".yml"))
                continue;

            var horse = ConfigHorse.of(file);
            if (horse == null)
                continue;

            loadedHorses.add(horse);
            console.info("Loaded horse: %s".formatted(file.getName().replaceAll(".yml", "")));
        }
    }

    private void setupInventories() {
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
    }

    private boolean setupEconomy() {
        EconomyType type;
        try {
            type = EconomyType.valueOf(Objects.requireNonNull(configFile.getString("Options.Economy-Type")).toUpperCase());
        } catch (IllegalArgumentException e) {
            console.warning("Economy type not recognized!");
            return false;
        }

        switch (type) {
            case VAULT -> {
                if (Bukkit.getPluginManager().getPlugin("Vault") == null)
                    return false;

                var rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (rsp == null)
                    return false;

                economy = new VaultEconomy(rsp.getProvider());
            }
            case PLAYERPOINTS -> {
                var plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
                if (plugin == null)
                    return false;

                economy = new PlayerPointsEconomy(new PlayerPointsAPI((PlayerPoints) plugin));
            }
        }

        console.info("Economy type: %s".formatted(economy.getType()));
        return true;
    }

    private boolean setupPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return false;

        placeholdersHook = new PlaceholdersHook();
        placeholdersEnabled = true;

        return true;
    }

    private void setupWorldGuard() {
        var plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");

        if (!(plugin instanceof WorldGuardPlugin))
            return;

        worldGuardHook = new WorldGuardHook();
        worldGuardHook.init();

        worldGuardEnabled = true;
    }

    private void setupModelEngine() {
        var plugin = Bukkit.getPluginManager().getPlugin("ModelEngine");
        if (plugin == null || !plugin.isEnabled())
            return;

        modelEngineEnabled = true;
        modelEngineHook = new ModelEngineHook();
    }

    private void checkForUpdates() {
        new UpdateChecker(this, 111158).getVersion(version -> {
            if (!version.equals(VERSION))
                console.warning("A new update is available! Download it from the official SpigotMC page");
        });
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String parseMessage(String s, Horse horse, Player player) {
        if (placeholdersEnabled) {
            return Chat.parseColors(PlaceholderAPI.setPlaceholders(player, parse(s, player, horse)));
        } else {
            return Chat.parseColors(parse(s, player, horse));
        }
    }

    public static List<String> parseMessage(List<String> list, Horse horse, Player player) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(parseMessage(s, horse, player));

        return newList;
    }

    private static String parse(String s, @Nullable Player player, @Nullable Horse horse) {
        var configHorse = horse == null ? null : ConfigHorse.of(horse.getName());

        return s.replaceAll("%displayName%", configHorse == null ? "null" :
                        horse.getCustomName() == null ? configHorse.getDisplayName() : horse.getCustomName())
                .replaceAll("%speed%", String.valueOf(horse instanceof SpawnedHorse ? ((SpawnedHorse) horse).getSpeed()
                        : Objects.requireNonNull(configHorse).getSpeed()))
                .replaceAll("%owner%", horse.getOwner())
                .replaceAll("%jumpStrength%", configHorse == null ? "null"
                        : String.valueOf(configHorse.getJumpStrength()))
                .replaceAll("%player%", player == null ? "null" : player.getName());
    }

    public static ConfigHorse getLoadedHorse(String name) {
        for (ConfigHorse loadedHorse : loadedHorses) {
            if (!loadedHorse.getId().equalsIgnoreCase(name))
                continue;

            return loadedHorse;
        }

        return null;
    }

    public static List<Horse> getHorsesOf(Player player) {
        List<Horse> horses = Lists.newArrayList();

        for (Horse horse : cache) {
            if (!horse.getOwner().equalsIgnoreCase(player.getName()))
                continue;

            horses.add(horse);
        }

        return horses;
    }

    public static Horse getHorse(UUID uuid) {
        for (Horse horse : cache) {
            if (horse.getUuid() != uuid)
                continue;

            return horse;
        }

        return null;
    }

    public static SpawnedHorse getSpawnedHorse(Entity entity) {
        for (Map.Entry<String, List<SpawnedHorse>> entry : spawnedHorses.entrySet()) {
            for (SpawnedHorse spawnedHorse : entry.getValue()) {
                if (spawnedHorse.getEntity() != entity)
                    continue;

                return spawnedHorse;
            }

            return null;
        }

        return null;
    }

    public static boolean has(Player player, String horseName) {
        for (Horse horse : getHorsesOf(player)) {
            if (!horse.getName().equalsIgnoreCase(horseName))
                continue;

            return true;
        }

        return false;
    }

    public static void teleport(Entity entity, Location location) {
        PaperLib.teleportAsync(entity, location).thenAccept(result -> {
            if (!result)
                entity.teleport(location);
        });
    }

    public static boolean notRespectsLimit(Player player) {
        if (player.isOp() || player.hasPermission("*"))
            return false;

        var horses = getHorsesOf(player).size();
        int limit = getConfigFile().getInt("Options.Horse-Limit");

        for (int i = 0; i < limit; i++)
            if (player.hasPermission(Perms.getLimit(i)))
                limit = i;

        return horses >= limit;
    }

}
