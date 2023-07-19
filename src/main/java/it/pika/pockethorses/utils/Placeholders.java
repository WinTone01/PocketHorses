package it.pika.pockethorses.utils;

import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "pockethorses";
    }

    @Override
    public @NotNull String getAuthor() {
        return "zPikaa";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.3.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("cooldown")) {
            if (!PocketHorses.getCooldowns().hasCooldown(player.getUniqueId()))
                return "No cooldown";

            return String.valueOf(PocketHorses.getCooldowns().getRemainingCooldown(player.getUniqueId()).toSeconds());
        }

        var parts = params.split("_");
        var horse = ConfigHorse.of(parts[0]);

        if (horse == null)
            return "Unrecognized horse";

        if (parts[1].equalsIgnoreCase("id"))
            return horse.getId();
        if (parts[1].equalsIgnoreCase("displayName"))
            return PocketHorses.parseColors(horse.getDisplayName());
        if (parts[1].equalsIgnoreCase("color"))
            return horse.getColor().name();
        if (parts[1].equalsIgnoreCase("speed"))
            return String.valueOf(horse.getSpeed());
        if (parts[1].equalsIgnoreCase("jumpStrength"))
            return String.valueOf(horse.getJumpStrength());
        if (parts[1].equalsIgnoreCase("maxHealth"))
            return String.valueOf(horse.getMaxHealth());
        if (parts[1].equalsIgnoreCase("buyable"))
            return String.valueOf(horse.isBuyable());
        if (parts[1].equalsIgnoreCase("price"))
            return String.valueOf(horse.getPrice());
        if (parts[1].equalsIgnoreCase("permission"))
            return String.valueOf(horse.isPermission());
        if (parts[1].equalsIgnoreCase("storage"))
            return String.valueOf(horse.isStorage());

        return "Unrecognized option";
    }

}
