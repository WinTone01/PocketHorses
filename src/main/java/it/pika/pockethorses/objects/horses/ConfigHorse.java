package it.pika.pockethorses.objects.horses;

import it.pika.libs.config.Config;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.HorseColor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Horse;

import java.io.File;

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

    public static ConfigHorse of(String name) {
        var file = new File(PocketHorses.getInstance().getDataFolder()
                + File.separator + "Horses" + File.separator + "%s.yml".formatted(name));
        if (!file.exists())
            return null;

        return load(new Config(PocketHorses.getInstance(), file));
    }

    public static ConfigHorse of(File file) {
        var config = new Config(PocketHorses.getInstance(), file);

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

            return new ConfigHorse(config, config.getFile().getName().replaceAll(".yml", ""),
                    config.getString("Display-Name"), color, style,
                    config.getDouble("Speed"), config.getDouble("Jump-Strength"),
                    config.getInt("Max-Health"), config.getBoolean("Buyable"),
                    config.getDouble("Price"), config.getBoolean("Permission"),
                    config.getBoolean("Storage"), config.getBoolean("Recyclable"),
                    config.getDouble("Recycle-Price"));
        } catch (NullPointerException e) {
            return null;
        }
    }

}
