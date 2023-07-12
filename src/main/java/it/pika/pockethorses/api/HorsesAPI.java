package it.pika.pockethorses.api;

import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.objects.ConfigHorse;
import it.pika.pockethorses.objects.Horse;
import it.pika.pockethorses.objects.SpawnedHorse;
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
        return (plugin instanceof PocketHorses && plugin.isEnabled()) ? new HorsesAPI() : null;
    }

    /**
     * Get all the horses loaded at the moment
     *
     * @return a list with loaded horses
     */
    public List<ConfigHorse> getLoadedHorses() {
        return PocketHorses.getLoadedHorses();
    }

    /**
     * Get all the horses owned by a player
     *
     * @param player The player referred to
     * @return a list of horses owned by the player
     */
    public List<Horse> getHorsesOf(Player player) {
        return PocketHorses.getHorsesOf(player);
    }

    /**
     * Gets an instance of SpawnedHorse from an entity of Bukkit
     *
     * @param entity The entity referred to
     * @return An instance of SpawnedHorse
     */
    public SpawnedHorse getSpawnedHorse(Entity entity) {
        return PocketHorses.getSpawnedHorse(entity);
    }

    /**
     * Gets an instance of Horse from a UUID
     *
     * @param uuid The UUID referred to
     * @return An instance of Horse
     */
    public Horse getHorse(UUID uuid) {
        return PocketHorses.getHorse(uuid);
    }

    /**
     * Gets an instance of ConfigHorse from a name
     *
     * @param name The name referred to
     * @return An instance of ConfigHorse
     */
    public ConfigHorse getLoadedHorse(String name) {
        return PocketHorses.getLoadedHorse(name);
    }

}
