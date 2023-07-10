package it.pika.pockethorses.listeners;

import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.HorseMenu;
import it.pika.pockethorses.utils.Serializer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import static it.pika.libs.chat.Chat.error;

public class HorseListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        var player = event.getPlayer();

        if (!(event.getRightClicked() instanceof Horse horse))
            return;

        var spawnedHorse = PocketHorses.getSpawnedHorse(horse);
        if (spawnedHorse == null)
            return;

        event.setCancelled(true);

        if (!spawnedHorse.getOwner().equalsIgnoreCase(player.getName())) {
            error(player, Messages.NOT_THE_OWNER.get());
            return;
        }

        if (player.isSneaking() || spawnedHorse.isSit() || horse.getPassengers().contains(player)) {
            new HorseMenu(spawnedHorse).get().open(player);
            return;
        }

        spawnedHorse.getEntity().addPassenger(player);
        player.sendMessage(Component.text(PocketHorses.parseMessage(Messages.RIDING_HORSE.get(), spawnedHorse, player)));
    }

    @EventHandler
    public void onCloseStorage(InventoryCloseEvent event) {
        var player = event.getPlayer();
        var contents = event.getInventory().getContents();

        var horse = PocketHorses.getInHorseStorage().remove(player.getName());
        if (horse == null)
            return;

        var title = event.getView().getTitle();
        if (!title.equalsIgnoreCase(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Storage-GUI.Title"))))
            return;

        horse.setStoredItems(Serializer.serialize(contents));
        PocketHorses.getStorage().setStoredItems(horse, contents);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        if (!(event.getEntity() instanceof Horse entity))
            return;

        var spawnedHorse = PocketHorses.getSpawnedHorse(entity);
        if (spawnedHorse == null)
            return;

        event.setCancelled(!PocketHorses.getConfigFile().getBoolean("Options.Allow-Player-Damage"));
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Horse entity))
            return;

        var spawnedHorse = PocketHorses.getSpawnedHorse(entity);
        if (spawnedHorse == null)
            return;

        event.getDrops().clear();
        PocketHorses.getSpawnedHorses().get(spawnedHorse.getOwner()).remove(spawnedHorse);
    }

}
