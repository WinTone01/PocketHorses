package it.pika.pockethorses.menu;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.ConfigHorse;
import it.pika.pockethorses.objects.Horse;
import it.pika.pockethorses.objects.SpawnedHorse;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class MyHorsesMenu implements InventoryProvider {

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Horses-GUI.Title")))
                .size(PocketHorses.getConfigFile().getInt("Horses-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(PocketHorses.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        for (Horse horse : PocketHorses.getHorsesOf(player)) {
            if (ConfigHorse.of(horse.getName()).isPermission()
                    && !player.hasPermission(Perms.getHorse(horse.getName())))
                continue;

            contents.add(ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horses-GUI.Horse-Item.Material")))
                    .name(PocketHorses.parseMessage(Objects.requireNonNull(PocketHorses.getConfigFile()
                            .getString("Horses-GUI.Horse-Item.Name")), horse, player))
                    .lore(PocketHorses.parseMessage(PocketHorses.getConfigFile().getStringList("Horses-GUI.Horse-Item.Lore"), horse, player))
                    .build(), e -> {
                player.closeInventory();

                if (alreadySpawned(player, horse)) {
                    error(player, Messages.ALREADY_SPAWNED.get());
                    return;
                }

                if (!PocketHorses.getConfigFile().getBoolean("Options.More-Horses-At-Time")
                        && PocketHorses.getSpawnedHorses().containsKey(player.getName())) {
                    error(player, Messages.CANNOT_SPAWN.get());
                    return;
                }

                horse.spawn(player);
                success(player, Messages.HORSE_SPAWNED.get());
            }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private boolean alreadySpawned(Player player, Horse horse) {
        if (!PocketHorses.getSpawnedHorses().containsKey(player.getName()))
            return false;

        for (SpawnedHorse spawnedHorse : PocketHorses.getSpawnedHorses().get(player.getName())) {
            if (spawnedHorse.getUuid() != horse.getUuid())
                continue;

            return true;
        }

        return false;
    }

}
