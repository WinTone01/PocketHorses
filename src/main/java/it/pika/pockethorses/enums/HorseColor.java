package it.pika.pockethorses.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Horse;

@AllArgsConstructor
public enum HorseColor {

    WHITE(Horse.Color.WHITE),
    CREAMY(Horse.Color.CREAMY),
    CHESTNUT(Horse.Color.CHESTNUT),
    BROWN(Horse.Color.BROWN),
    BLACK(Horse.Color.BLACK),
    GRAY(Horse.Color.GRAY),
    DARK_BROWN(Horse.Color.DARK_BROWN),
    ZOMBIE(null),
    SKELETON(null);

    @Getter
    private final Horse.Color bukkitColor;

}
