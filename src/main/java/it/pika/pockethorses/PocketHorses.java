package it.pika.pockethorses;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tchristofferson.configupdater.ConfigUpdater;
import fr.minuskube.inv.InventoryManager;
import it.pika.libs.config.Config;
import it.pika.pockethorses.api.events.HorsesInitializeEvent;
import it.pika.pockethorses.commands.HorsesCmd;
import it.pika.pockethorses.commands.MainCmd;
import it.pika.pockethorses.listeners.HorseListener;
import it.pika.pockethorses.listeners.JoinListener;
import it.pika.pockethorses.listeners.VoucherListener;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import it.pika.pockethorses.storage.Storage;
import it.pika.pockethorses.storage.StorageType;
import it.pika.pockethorses.storage.impl.JSON;
import it.pika.pockethorses.storage.impl.MySQL;
import it.pika.pockethorses.storage.impl.SQLite;
import it.pika.pockethorses.utils.Cooldowns;
import it.pika.pockethorses.utils.Metrics;
import it.pika.pockethorses.utils.Placeholders;
import it.pika.pockethorses.utils.UpdateChecker;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class PocketHorses extends JavaPlugin {

    @Getter
    private static PocketHorses instance = null;
    @Getter
    private static final Logger console = Logger.getLogger("PocketHorses");
    @Getter
    private static Storage storage = null;
    @Getter
    private static InventoryManager inventoryManager = null;
    @Getter
    private static Economy economy = null;
    @Getter
    private static final Cooldowns cooldowns = new Cooldowns();


    @Getter
    private static Config configFile = null;
    @Getter
    private static Config messagesFile = null;
    @Getter
    private static Config vouchersFile = null;


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
    private static boolean shopEnabled;
    @Getter
    private static boolean placeholdersEnabled = false;

    public static final String VERSION = "1.6.0";

    @Override
    public void onEnable() {
        instance = this;
        var stopwatch = Stopwatch.createStarted();

        setupFiles();
        if (!setupStorage()) {
            stopwatch.stop();
            console.warning("Couldn't setup storage, check your config.yml! Disabling the plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            shopEnabled = false;
            console.warning("Vault not found, you will not be able to use the shop!");
        }
        if (!setupPlaceholders())
            console.warning("PlaceholderAPI not found, you will not be able to use placeholders!");

        registerListeners();
        registerCommands();
        loadHorses();
        setupInventories();
        checkForUpdates();

        stopwatch.stop();
        Bukkit.getPluginManager().callEvent(new HorsesInitializeEvent(this));
        new Metrics(this, 19134);

        console.info("Plugin enabled in %s ms.".formatted(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
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
        messagesFile = new Config(this, "messages.yml");
        vouchersFile = new Config(this, "vouchers.yml");

        ConfigUpdater.update(this, "config.yml", configFile.getFile());
        ConfigUpdater.update(this, "messages.yml", messagesFile.getFile());

        configFile.reload();
        messagesFile.reload();

        shopEnabled = configFile.getBoolean("Options.Shop-Enabled");
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
        pm.registerEvents(new JoinListener(), this);
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
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false;

        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        economy = rsp.getProvider();
        return true;
    }

    private boolean setupPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return false;

        new Placeholders().register();
        placeholdersEnabled = true;
        return true;
    }

    private void checkForUpdates() {
        new UpdateChecker(this, 111158).getVersion(version -> {
            if (!version.equals(VERSION))
                console.warning("A new update is available! Download it from the official SpigotMC page");
        });
    }

    public static String parseColors(@Nullable String s) {
        if (s == null)
            return "null";

        var parsed = new StringBuilder();
        var colorHex = "";
        if (s.contains("&")) {
            for (String color : s.split("&")) {
                if (color.length() < 1) continue;
                if (color.substring(0, 1).matches("[A-Fa-f0-9]|k|l|m|n|o|r")) {
                    String colorCode = color.substring(0, 1);
                    parsed.append(ChatColor.getByChar(colorCode.charAt(0)));
                    parsed.append(color.substring(1));
                    continue;
                }
                if (color.length() < 7) continue;
                if (color.substring(0, 7).matches("#[A-Fa-f0-9]{6}")) {
                    if (color.substring(0, 7).matches("#[A-Fa-f0-9]{6}")) {
                        colorHex = color.substring(0, 7);
                        parsed.append(net.md_5.bungee.api.ChatColor.of(colorHex));
                        parsed.append(color.substring(7));
                        continue;
                    }
                }
                parsed.append(color);
            }
        } else {
            parsed.append(s);
        }

        return parsed.toString();
    }

    public static List<String> parseColors(List<String> list) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(parseColors(s));

        return newList;
    }

    public static String parseMessage(String s, @Nullable Horse horse, @Nullable Player player) {
        var configHorse = horse == null ? null : ConfigHorse.of(horse.getName());

        if (placeholdersEnabled) {
            return parseColors(PlaceholderAPI.setPlaceholders(player,
                    s.replaceAll("%displayName%", configHorse == null ? "null" :
                                    horse.getCustomName() == null ? configHorse.getDisplayName() : horse.getCustomName())
                            .replaceAll("%speed%", String.valueOf(horse instanceof SpawnedHorse ? ((SpawnedHorse) horse).getSpeed()
                                    : Objects.requireNonNull(configHorse).getSpeed()))
                            .replaceAll("%owner%", horse.getOwner())
                            .replaceAll("%jumpStrength%", configHorse == null ? "null"
                                    : String.valueOf(configHorse.getJumpStrength()))
                            .replaceAll("%player%", player == null ? "null" : player.getName())));
        } else {
            return parseColors(s.replaceAll("%displayName%", configHorse == null ? "null" :
                            horse.getCustomName() == null ? configHorse.getDisplayName() : horse.getCustomName())
                    .replaceAll("%speed%", String.valueOf(horse instanceof SpawnedHorse ? ((SpawnedHorse) horse).getSpeed()
                            : Objects.requireNonNull(configHorse).getSpeed()))
                    .replaceAll("%owner%", horse.getOwner())
                    .replaceAll("%jumpStrength%", configHorse == null ? "null"
                            : String.valueOf(configHorse.getJumpStrength()))
                    .replaceAll("%player%", player == null ? "null" : player.getName()));
        }
    }

    public static List<String> parseMessage(List<String> list, Horse horse, Player player) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(parseMessage(s, horse, player));

        return newList;
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

}
