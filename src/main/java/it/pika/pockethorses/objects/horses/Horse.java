package it.pika.pockethorses.objects.horses;

import com.google.common.collect.Lists;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.api.events.HorseSpawnEvent;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.utils.Serializer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.UUID;

import static it.pika.libs.chat.Chat.error;

@Getter
@Setter
public class Horse {

    private UUID uuid;
    private String name;
    private String owner;
    private String customName;
    private String storedItems;

    public Horse(UUID uuid, String name, String owner, String customName, String storedItems) {
        this.uuid = uuid;
        this.name = name;
        this.owner = owner;
        this.customName = customName;
        this.storedItems = storedItems;
    }

    public void spawn(Player player) {
        var config = PocketHorses.getConfigFile();

        var configHorse = ConfigHorse.of(name);
        if (configHorse == null)
            return;

        var event = new HorseSpawnEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Horse.class, horse -> {
            horse.setAdult();
            horse.setTamed(true);
            horse.setOwner(player);
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

            horse.setTarget(player);
            horse.setColor(configHorse.getColor());

            double speed = configHorse.getSpeed() / 3.6;
            horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed / 20);

            horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(configHorse.getMaxHealth());
            horse.setHealth(configHorse.getMaxHealth());

            if (customName != null && !customName.equalsIgnoreCase("null")) {
                horse.setCustomName(PocketHorses.parseColors(customName) +
                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                " " + PocketHorses.parseColors(config.getString("Options.Display-HP")
                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                horse.setCustomNameVisible(true);
            } else {
                horse.setCustomName(PocketHorses.parseColors(configHorse.getDisplayName()) +
                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                " " + PocketHorses.parseColors(config.getString("Options.Display-HP")
                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
            }

            horse.setJumpStrength(configHorse.getJumpStrength());
            horse.setStyle(configHorse.getStyle());

            if (PocketHorses.getSpawnedHorses().containsKey(player.getName())) {
                var list = PocketHorses.getSpawnedHorses().remove(player.getName());
                list.add(new SpawnedHorse(uuid, name, owner, customName, storedItems, horse, configHorse.getSpeed(),
                        false, false));

                PocketHorses.getSpawnedHorses().put(player.getName(), list);
                return;
            }

            PocketHorses.getSpawnedHorses().put(player.getName(), Lists.newArrayList(new SpawnedHorse(uuid, name, owner,
                    customName, storedItems, horse, configHorse.getSpeed(), false, false)));

            var seconds = PocketHorses.getConfigFile().getInt("Options.Horse-Cooldown");
            if (seconds > 0)
                PocketHorses.getCooldowns().setCooldown(player.getUniqueId(), Duration.ofSeconds(seconds));
        });
    }

    public void openStorage(Player player) {
        player.closeInventory();

        if (PocketHorses.getConfigFile().getBoolean("Storage-GUI.Use-Permission") &&
                !player.hasPermission(Perms.STORAGE_GUI)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        var inventory = Bukkit.createInventory(null,
                PocketHorses.getConfigFile().getInt("Storage-GUI.Size.Rows") * 9,
                PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Storage-GUI.Title")));
        inventory.setContents(storedItems == null ? new ItemStack[]{} : Serializer.deserialize(storedItems));

        player.openInventory(inventory);
        PocketHorses.getInHorseStorage().put(player.getName(), this);
    }

}