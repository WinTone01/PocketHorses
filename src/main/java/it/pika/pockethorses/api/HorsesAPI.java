package it.pika.pockethorses.api;

import it.pika.pockethorses.Main;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HorsesAPI {

    /**
     * Gets an instance of the API.
     *
     * @return the API instance
     */
    public static HorsesAPI getInstance() {
        var plugin = Bukkit.getPluginManager().getPlugin("PocketHorses");
        return (plugin instanceof Main && plugin.isEnabled()) ? new HorsesAPI() : null;
    }

    /**
     * Get all the horses loaded at the moment
     *
     * @return a list with loaded horses
     */
    public List<ConfigHorse> getLoadedHorses() {
        return Main.getLoadedHorses();
    }

    /**
     * Get all the horses owned by a player
     *
     * @param player The player referred to
     * @return a list of horses owned by the player
     */
    public List<Horse> getHorsesOf(Player player) {
        return Main.getHorsesOf(player);
    }

    /**
     * Gets an instance of SpawnedHorse from an entity of Bukkit
     *
     * @param entity The entity referred to
     * @return An instance of SpawnedHorse
     */
    public SpawnedHorse getSpawnedHorse(Entity entity) {
        return Main.getSpawnedHorse(entity);
    }

    /**
     * Gets an instance of Horse from a UUID
     *
     * @param uuid The UUID referred to
     * @return An instance of Horse
     */
    public Horse getHorse(UUID uuid) {
        return Main.getHorse(uuid);
    }

    /**
     * Gets an instance of ConfigHorse from a name
     *
     * @param name The name referred to
     * @return An instance of ConfigHorse
     */
    public ConfigHorse getLoadedHorse(String name) {
        return Main.getLoadedHorse(name);
    }

}
