package it.pika.pockethorses.objects;

import it.pika.pockethorses.PocketHorses;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Horse;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class ConfigHorse {

    private String id;
    private String displayName;
    private Horse.Color color;
    private double speed;
    private boolean buyable;
    private double price;
    private boolean permission;
    private boolean storage;

    public static ConfigHorse of(String name) {
        var config = PocketHorses.getHorsesFile();

        try {
            Horse.Color color;
            try {
                color = Horse.Color.valueOf(config.getString("%s.color".formatted(name)));
            } catch (IllegalArgumentException e) {
                color = Horse.Color.BLACK;
                PocketHorses.getConsole().warning("Color %s not recognized for horse %s, using 'BLACK' instead."
                        .formatted(config.getString("%s.color".formatted(name)), name));
            }

            return new ConfigHorse(name, config.getString("%s.displayName".formatted(name)), color,
                    config.getDouble("%s.speed".formatted(name)),
                    config.getBoolean("%s.buyable".formatted(name)),
                    config.getDouble("%s.price".formatted(name)),
                    config.getBoolean("%s.permission".formatted(name)),
                    config.getBoolean("%s.storage".formatted(name)));
        } catch (NullPointerException e) {
            return null;
        }
    }

}
