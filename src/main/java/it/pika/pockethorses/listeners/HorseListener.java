package it.pika.pockethorses.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.HorseMenu;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import it.pika.pockethorses.objects.items.Care;
import it.pika.pockethorses.objects.items.Supplement;
import it.pika.pockethorses.utils.Serializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class HorseListener implements Listener {

    private static final int AUTO_RECALL_RANGE = PocketHorses.getConfigFile().getInt("Options.Auto-Recall-Range");

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        var player = event.getPlayer();

        if (!(event.getRightClicked() instanceof AbstractHorse horse))
            return;

        var spawnedHorse = PocketHorses.getSpawnedHorse(horse);
        if (spawnedHorse == null)
            return;

        event.setCancelled(true);

        if (!spawnedHorse.getOwner().equalsIgnoreCase(player.getName())) {
            error(player, Messages.NOT_THE_OWNER.get());
            return;
        }

        var item = player.getInventory().getItemInMainHand();
        if (!item.getType().isAir()) {
            var nbt = new NBTItem(item);

            if (nbt.hasTag("supplement")) {
                var supplement = Supplement.fromItem(item);

                if (supplement != null) {
                    var configHorse = ConfigHorse.of(spawnedHorse.getName());
                    if (configHorse == null)
                        return;

                    spawnedHorse.setSpeed(spawnedHorse.getSpeed() + supplement.getExtraSpeed());

                    double speed = spawnedHorse.getSpeed() / 3.6;
                    Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                    var newJumpStrength = horse.getJumpStrength() + supplement.getExtraJump();
                    horse.setJumpStrength(Math.min(newJumpStrength, 2.0));

                    item.setAmount(item.getAmount() - 1);
                    PocketHorses.getActiveSupplements().put(spawnedHorse.getUuid(), supplement);

                    success(player, Messages.ITEM_USED.get());

                    Bukkit.getScheduler().runTaskLaterAsynchronously(PocketHorses.getInstance(), () -> {
                        spawnedHorse.setSpeed(configHorse.getSpeed());

                        double newSpeed = configHorse.getSpeed() / 3.6;
                        Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED))
                                .setBaseValue(newSpeed / 20);

                        horse.setJumpStrength(configHorse.getJumpStrength());
                        PocketHorses.getActiveSupplements().remove(spawnedHorse.getUuid());

                        success(player, Messages.SUPPLEMENT_EXPIRED.get());
                    }, 20 * supplement.getDuration());
                }
            } else if (nbt.hasTag("care")) {
                var care = Care.fromItem(item);

                if (care != null) {
                    var configHorse = ConfigHorse.of(spawnedHorse.getName());
                    if (configHorse == null)
                        return;

                    var newHealth = horse.getHealth() + care.getRestoreHealth();
                    horse.setHealth(Math.min(newHealth, configHorse.getMaxHealth()));

                    if (spawnedHorse.getCustomName() != null
                            && !spawnedHorse.getCustomName().equalsIgnoreCase("null")) {
                        horse.setCustomName(PocketHorses.parseColors(horse.getCustomName()) +
                                (PocketHorses.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                                        " " + PocketHorses.parseColors(Objects.requireNonNull(PocketHorses.getConfigFile()
                                                        .getString("Options.Display-HP"))
                                                .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                        horse.setCustomNameVisible(true);
                    } else {
                        horse.setCustomName(PocketHorses.parseColors(configHorse.getDisplayName()) +
                                (PocketHorses.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                                        " " + PocketHorses.parseColors(Objects.requireNonNull(PocketHorses.getConfigFile()
                                                        .getString("Options.Display-HP"))
                                                .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                    }

                    item.setAmount(item.getAmount() - 1);
                    success(player, Messages.ITEM_USED.get());
                }
            }
            return;
        }

        if (player.isSneaking() || spawnedHorse.isSit() || horse.getPassengers().contains(player)) {
            if (PocketHorses.getConfigFile().getBoolean("Horse-GUI.Use-Permission") &&
                    !player.hasPermission(Perms.HORSE_GUI)) {
                error(player, Messages.NO_PERMISSION.get());
                return;
            }

            new HorseMenu(spawnedHorse).get().open(player);
            return;
        }


        var configHorse = ConfigHorse.of(spawnedHorse.getName());
        if (configHorse != null && configHorse.getModel() != null) {
            if (!PocketHorses.isModelEngineEnabled() || PocketHorses.getModelEngineHook() == null)
                return;

            PocketHorses.getModelEngineHook().getOn(player, spawnedHorse);
        } else {
            spawnedHorse.getEntity().addPassenger(player);
        }

        player.sendMessage(PocketHorses.parseMessage(Messages.RIDING_HORSE.get(), spawnedHorse, player));
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

        if (!(event.getEntity() instanceof AbstractHorse entity))
            return;

        var spawnedHorse = PocketHorses.getSpawnedHorse(entity);
        if (spawnedHorse == null)
            return;

        event.setCancelled(!PocketHorses.getConfigFile().getBoolean("Options.Allow-Player-Damage"));
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse entity))
            return;

        var spawnedHorse = PocketHorses.getSpawnedHorse(entity);
        if (spawnedHorse == null)
            return;

        event.getDrops().clear();
        PocketHorses.getSpawnedHorses().get(spawnedHorse.getOwner()).remove(spawnedHorse);
    }

    @EventHandler
    public void autoRecall(PlayerMoveEvent event) {
        var player = event.getPlayer();

        if (!PocketHorses.getSpawnedHorses().containsKey(player.getName()))
            return;

        for (SpawnedHorse spawnedHorse : PocketHorses.getSpawnedHorses().get(player.getName())) {
            if (!spawnedHorse.isAutoRecall())
                continue;

            if (player.getNearbyEntities(AUTO_RECALL_RANGE, AUTO_RECALL_RANGE, AUTO_RECALL_RANGE)
                    .contains(spawnedHorse.getEntity()))
                continue;

            PocketHorses.teleport(spawnedHorse.getEntity(), player.getLocation());
            success(player, Messages.AUTO_RECALLED.get());
        }
    }

    @EventHandler
    public void actionBar(PlayerMoveEvent event) {
        var player = event.getPlayer();

        if (player.getVehicle() == null)
            return;

        var spawnedHorse = PocketHorses.getSpawnedHorse(player.getVehicle());
        if (spawnedHorse == null)
            return;

        if (!PocketHorses.getConfigFile().getBoolean("Options.Action-Bar-While-Riding"))
            return;

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PocketHorses
                .parseMessage(PocketHorses.getConfigFile().getString("Options.Action-Bar-Message"),
                        spawnedHorse, player)));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.HORSE && event.getEntityType() != EntityType.ZOMBIE_HORSE
                && event.getEntityType() != EntityType.SKELETON_HORSE)
            return;

        var entity = (AbstractHorse) event.getEntity();

        var horse = PocketHorses.getSpawnedHorse(entity);
        if (horse == null)
            return;

        var configHorse = ConfigHorse.of(horse.getName());
        if (configHorse == null)
            return;

        if (horse.getCustomName() != null && !horse.getCustomName().equalsIgnoreCase("null")) {
            entity.setCustomName(PocketHorses.parseColors(horse.getCustomName()) +
                    (PocketHorses.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                            " " + PocketHorses.parseColors(Objects.requireNonNull(PocketHorses.getConfigFile()
                                            .getString("Options.Display-HP"))
                                    .replaceAll("%health%", String.valueOf((int) entity.getHealth()))) : ""));
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomName(PocketHorses.parseColors(configHorse.getDisplayName()) +
                    (PocketHorses.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                            " " + PocketHorses.parseColors(Objects.requireNonNull(PocketHorses.getConfigFile()
                                            .getString("Options.Display-HP"))
                                    .replaceAll("%health%", String.valueOf((int) entity.getHealth()))) : ""));
        }
    }

}
