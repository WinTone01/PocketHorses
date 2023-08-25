package it.pika.pockethorses.objects.horses;

import it.pika.libs.config.Config;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.HorseColor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Horse;

import java.io.File;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class ConfigHorse {

    private Config config;
    private String id;
    private String displayName;
    private HorseColor color;
    private Horse.Style style;
    private double speed;
    private double jumpStrength;
    private int maxHealth;
    private boolean buyable;
    private double price;
    private boolean permission;
    private boolean storage;
    private boolean recyclable;
    private double recyclePrice;
    private String model;
    private int cooldown;
    private List<String> consoleCommandsOnMount;
    private List<String> consoleCommandsOnDismount;

    public static ConfigHorse of(String name) {
        var file = new File(Main.getInstance().getDataFolder()
                + File.separator + "Horses" + File.separator + "%s.yml".formatted(name));
        if (!file.exists())
            return null;

        return load(new Config(Main.getInstance(), file, false));
    }

    public static ConfigHorse of(File file) {
        var config = new Config(Main.getInstance(), file, false);

        return load(config);
    }

    private static ConfigHorse load(Config config) {
        try {
            HorseColor color;
            try {
                color = HorseColor.valueOf(config.getString("Color"));
            } catch (IllegalArgumentException e) {
                color = HorseColor.BLACK;
            }

            Horse.Style style;
            try {
                style = Horse.Style.valueOf(config.getString("Style"));
            } catch (NullPointerException | IllegalArgumentException e) {
                style = Horse.Style.BLACK_DOTS;
            }

            String model;
            try {
                model = config.getString("Model");
            } catch (NullPointerException e) {
                model = null;
            }

            return new ConfigHorse(config, config.getFile().getName().replaceAll(".yml", ""),
                    config.getString("Display-Name"), color, style,
                    config.getDouble("Speed"), config.getDouble("Jump-Strength"),
                    config.getInt("Max-Health"), config.getBoolean("Buyable"),
                    config.getDouble("Price"), config.getBoolean("Permission"),
                    config.getBoolean("Storage"), config.getBoolean("Recyclable"),
                    config.getDouble("Recycle-Price"), model, config.getInt("Cooldown"),
                    config.getStringList("Console-Commands-On-Mount"),
                    config.getStringList("Console-Commands-On-Dismount"));
        } catch (NullPointerException e) {
            return null;
        }
    }

}
