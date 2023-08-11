package it.pika.pockethorses.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.HorseMenu;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import it.pika.pockethorses.objects.items.Care;
import it.pika.pockethorses.objects.items.Supplement;
import it.pika.pockethorses.utils.Serializer;
import it.pika.pockethorses.utils.xseries.ActionBar;
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
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.Objects;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class HorseListener implements Listener {

    private static final int AUTO_RECALL_RANGE = Main.getConfigFile().getInt("Options.Auto-Recall-Range");

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        var player = event.getPlayer();

        if (!(event.getRightClicked() instanceof AbstractHorse horse))
            return;

        var spawnedHorse = Main.getSpawnedHorse(horse);
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
                    Main.getActiveSupplements().put(spawnedHorse.getUuid(), supplement);

                    success(player, Messages.ITEM_USED.get());

                    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
                        spawnedHorse.setSpeed(configHorse.getSpeed());

                        double newSpeed = configHorse.getSpeed() / 3.6;
                        Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED))
                                .setBaseValue(newSpeed / 20);

                        horse.setJumpStrength(configHorse.getJumpStrength());
                        Main.getActiveSupplements().remove(spawnedHorse.getUuid());

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
                        horse.setCustomName(Main.parseColors(horse.getCustomName()) +
                                (Main.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                                        " " + Main.parseColors(Objects.requireNonNull(Main.getConfigFile()
                                                        .getString("Options.Display-HP"))
                                                .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                        horse.setCustomNameVisible(true);
                    } else {
                        horse.setCustomName(Main.parseColors(configHorse.getDisplayName()) +
                                (Main.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                                        " " + Main.parseColors(Objects.requireNonNull(Main.getConfigFile()
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
            if (Main.getConfigFile().getBoolean("Horse-GUI.Use-Permission") &&
                    !player.hasPermission(Perms.HORSE_GUI)) {
                error(player, Messages.NO_PERMISSION.get());
                return;
            }

            new HorseMenu(spawnedHorse).get().open(player);
            return;
        }


        var configHorse = ConfigHorse.of(spawnedHorse.getName());
        if (configHorse != null && configHorse.getModel() != null) {
            if (!Main.isModelEngineEnabled() || Main.getModelEngineHook() == null)
                return;

            Main.getModelEngineHook().getOn(player, spawnedHorse);
        } else {
            spawnedHorse.getEntity().addPassenger(player);
        }

        ActionBar.sendActionBarWhile(Main.getInstance(), player, Main.parseMessage(Main.getConfigFile()
                        .getString("Options.Action-Bar-Message"), spawnedHorse, player),
                () -> player.getVehicle() != null && Main.getSpawnedHorse(player.getVehicle()) != null);
        player.sendMessage(Main.parseMessage(Messages.RIDING_HORSE.get(), spawnedHorse, player));
    }

    @EventHandler
    public void onCloseStorage(InventoryCloseEvent event) {
        var player = event.getPlayer();
        var contents = event.getInventory().getContents();

        var horse = Main.getInHorseStorage().remove(player.getName());
        if (horse == null)
            return;

        var title = event.getView().getTitle();
        if (!title.equalsIgnoreCase(Main.parseColors(Main.getConfigFile().getString("Storage-GUI.Title"))))
            return;

        horse.setStoredItems(Serializer.serialize(contents));
        Main.getStorage().setStoredItems(horse, contents);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        if (!(event.getEntity() instanceof AbstractHorse entity))
            return;

        var spawnedHorse = Main.getSpawnedHorse(entity);
        if (spawnedHorse == null)
            return;

        event.setCancelled(!Main.getConfigFile().getBoolean("Options.Allow-Player-Damage"));
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse entity))
            return;

        var spawnedHorse = Main.getSpawnedHorse(entity);
        if (spawnedHorse == null)
            return;

        event.getDrops().clear();
        Main.getSpawnedHorses().get(spawnedHorse.getOwner()).remove(spawnedHorse);
    }

    @EventHandler
    public void autoRecall(PlayerMoveEvent event) {
        var player = event.getPlayer();

        if (!Main.getSpawnedHorses().containsKey(player.getName()))
            return;

        for (SpawnedHorse spawnedHorse : Main.getSpawnedHorses().get(player.getName())) {
            if (!spawnedHorse.isAutoRecall())
                continue;

            if (player.getNearbyEntities(AUTO_RECALL_RANGE, AUTO_RECALL_RANGE, AUTO_RECALL_RANGE)
                    .contains(spawnedHorse.getEntity()))
                continue;

            Main.teleport(spawnedHorse.getEntity(), player.getLocation());
            success(player, Messages.AUTO_RECALLED.get());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.HORSE && event.getEntityType() != EntityType.ZOMBIE_HORSE
                && event.getEntityType() != EntityType.SKELETON_HORSE)
            return;

        var entity = (AbstractHorse) event.getEntity();

        var horse = Main.getSpawnedHorse(entity);
        if (horse == null)
            return;

        var configHorse = ConfigHorse.of(horse.getName());
        if (configHorse == null)
            return;

        if (horse.getCustomName() != null && !horse.getCustomName().equalsIgnoreCase("null")) {
            entity.setCustomName(Main.parseColors(horse.getCustomName()) +
                    (Main.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                            " " + Main.parseColors(Objects.requireNonNull(Main.getConfigFile()
                                            .getString("Options.Display-HP"))
                                    .replaceAll("%health%", String.valueOf((int) entity.getHealth()))) : ""));
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomName(Main.parseColors(configHorse.getDisplayName()) +
                    (Main.getConfigFile().getBoolean("Options.Display-HP-In-Name") ?
                            " " + Main.parseColors(Objects.requireNonNull(Main.getConfigFile()
                                            .getString("Options.Display-HP"))
                                    .replaceAll("%health%", String.valueOf((int) entity.getHealth()))) : ""));
        }
    }

    @EventHandler
    public void onDismount(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player))
            return;

        if (!Main.getAutoRemove().contains(player.getName()))
            return;

        if (!(event.getVehicle() instanceof AbstractHorse horse))
            return;

        var spawnedHorse = Main.getSpawnedHorse(horse);
        if (spawnedHorse == null)
            return;

        spawnedHorse.remove(player);
        success(player, Messages.HORSE_REMOVED.get());
    }

}
