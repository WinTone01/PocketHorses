package it.pika.pockethorses.hooks;

import it.pika.libs.chat.Chat;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholdersHook extends PlaceholderExpansion {

    public PlaceholdersHook() {
        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pockethorses";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Pika";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.9.5";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("horses"))
            return String.valueOf(Main.getHorsesOf(player).size());

        var parts = params.split("_");
        var horse = ConfigHorse.of(parts[0]);

        if (horse == null)
            return "Unrecognized horse";

        if (parts[1].equalsIgnoreCase("id"))
            return horse.getId();
        if (parts[1].equalsIgnoreCase("displayName"))
            return Chat.parseColors(horse.getDisplayName());
        if (parts[1].equalsIgnoreCase("color"))
            return horse.getColor().name();
        if (parts[1].equalsIgnoreCase("style"))
            return horse.getStyle().name();
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
        if (parts[1].equalsIgnoreCase("recyclable"))
            return String.valueOf(horse.isRecyclable());
        if (parts[1].equalsIgnoreCase("recyclePrice"))
            return String.valueOf(horse.getRecyclePrice());
        if (parts[1].equalsIgnoreCase("model"))
            return horse.getModel();

        return "Unrecognized option";
    }


}
