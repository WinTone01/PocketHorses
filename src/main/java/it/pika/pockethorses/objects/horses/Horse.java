package it.pika.pockethorses.objects.horses;

import com.google.common.collect.Lists;
import it.pika.libs.chat.Chat;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.api.events.HorseSpawnEvent;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.utils.Serializer;
import it.pika.libs.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Objects;
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

    public boolean spawn(Player player) {
        var config = Main.getConfigFile();

        var configHorse = ConfigHorse.of(name);
        if (configHorse == null)
            return false;

        var event = new HorseSpawnEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        if (configHorse.getModel() == null) {
            switch (configHorse.getColor()) {
                case WHITE, CREAMY, CHESTNUT, BROWN, BLACK, GRAY, DARK_BROWN ->
                        player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Horse.class, horse -> {
                            horse.setAdult();
                            horse.setTamed(true);
                            horse.setOwner(player);
                            horse.getInventory().setSaddle(new ItemStack(Objects.requireNonNull(XMaterial.SADDLE.parseMaterial())));

                            horse.setTarget(player);
                            horse.setColor(configHorse.getColor().getBukkitColor());

                            if (Main.getActiveSupplements().containsKey(uuid)) {
                                var supplement = Main.getActiveSupplements().get(uuid);

                                var speed = (configHorse.getSpeed() + supplement.getExtraSpeed()) / 3.6;
                                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                                var jumpStrength = configHorse.getJumpStrength() + supplement.getExtraJump();
                                horse.setJumpStrength(Math.min(jumpStrength, 2.0));
                            } else {
                                double speed = configHorse.getSpeed() / 3.6;
                                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                                horse.setJumpStrength(configHorse.getJumpStrength());
                            }

                            Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(configHorse.getMaxHealth());
                            horse.setHealth(configHorse.getMaxHealth());

                            if (customName != null && !customName.equalsIgnoreCase("null")) {
                                horse.setCustomName(Chat.parseColors(customName) +
                                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                                " " + Chat.parseColors(config.getString("Options.Display-HP")
                                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                                horse.setCustomNameVisible(true);
                            } else {
                                horse.setCustomName(Chat.parseColors(configHorse.getDisplayName()) +
                                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                                " " + Chat.parseColors(config.getString("Options.Display-HP")
                                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                            }

                            horse.setStyle(configHorse.getStyle());

                            if (Main.getSpawnedHorses().containsKey(player.getName())) {
                                var list = Main.getSpawnedHorses().remove(player.getName());
                                list.add(new SpawnedHorse(uuid, name, owner, customName, storedItems,
                                        horse, configHorse.getSpeed(), false, false, null));

                                Main.getSpawnedHorses().put(player.getName(), list);
                                return;
                            }

                            Main.getSpawnedHorses().put(player.getName(), Lists.newArrayList(
                                    new SpawnedHorse(uuid, name, owner, customName, storedItems,
                                            horse, configHorse.getSpeed(), false, false, null)));

                            var seconds = configHorse.getCooldown();
                            if (seconds > 0)
                                Main.getCooldownManager().setCooldown(player.getUniqueId(), configHorse.getId(), Duration.ofSeconds(seconds));
                        });
                case ZOMBIE ->
                        player.getWorld().spawn(player.getLocation(), org.bukkit.entity.ZombieHorse.class, horse -> {
                            horse.setAdult();
                            horse.setTamed(true);
                            horse.setOwner(player);
                            horse.getInventory().setSaddle(new ItemStack(Objects.requireNonNull(XMaterial.SADDLE.parseMaterial())));

                            horse.setTarget(player);

                            if (Main.getActiveSupplements().containsKey(uuid)) {
                                var supplement = Main.getActiveSupplements().get(uuid);

                                var speed = (configHorse.getSpeed() + supplement.getExtraSpeed()) / 3.6;
                                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                                var jumpStrength = configHorse.getJumpStrength() + supplement.getExtraJump();
                                horse.setJumpStrength(Math.min(jumpStrength, 2.0));
                            } else {
                                double speed = configHorse.getSpeed() / 3.6;
                                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                                horse.setJumpStrength(configHorse.getJumpStrength());
                            }

                            Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(configHorse.getMaxHealth());
                            horse.setHealth(configHorse.getMaxHealth());

                            if (customName != null && !customName.equalsIgnoreCase("null")) {
                                horse.setCustomName(Chat.parseColors(customName) +
                                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                                " " + Chat.parseColors(config.getString("Options.Display-HP")
                                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                                horse.setCustomNameVisible(true);
                            } else {
                                horse.setCustomName(Chat.parseColors(configHorse.getDisplayName()) +
                                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                                " " + Chat.parseColors(config.getString("Options.Display-HP")
                                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                            }

                            if (Main.getSpawnedHorses().containsKey(player.getName())) {
                                var list = Main.getSpawnedHorses().remove(player.getName());
                                list.add(new SpawnedHorse(uuid, name, owner, customName, storedItems,
                                        horse, configHorse.getSpeed(), false, false, null));

                                Main.getSpawnedHorses().put(player.getName(), list);
                                return;
                            }

                            Main.getSpawnedHorses().put(player.getName(), Lists.newArrayList(
                                    new SpawnedHorse(uuid, name, owner, customName, storedItems,
                                            horse, configHorse.getSpeed(), false, false, null)));

                            var seconds = configHorse.getCooldown();
                            if (seconds > 0)
                                Main.getCooldownManager().setCooldown(player.getUniqueId(), configHorse.getId(), Duration.ofSeconds(seconds));
                        });
                case SKELETON ->
                        player.getWorld().spawn(player.getLocation(), org.bukkit.entity.SkeletonHorse.class, horse -> {
                            horse.setAdult();
                            horse.setTamed(true);
                            horse.setOwner(player);
                            horse.getInventory().setSaddle(new ItemStack(Objects.requireNonNull(XMaterial.SADDLE.parseMaterial())));

                            horse.setTarget(player);

                            if (Main.getActiveSupplements().containsKey(uuid)) {
                                var supplement = Main.getActiveSupplements().get(uuid);

                                var speed = (configHorse.getSpeed() + supplement.getExtraSpeed()) / 3.6;
                                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                                var jumpStrength = configHorse.getJumpStrength() + supplement.getExtraJump();
                                horse.setJumpStrength(Math.min(jumpStrength, 2.0));
                            } else {
                                double speed = configHorse.getSpeed() / 3.6;
                                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                                horse.setJumpStrength(configHorse.getJumpStrength());
                            }

                            Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(configHorse.getMaxHealth());
                            horse.setHealth(configHorse.getMaxHealth());

                            if (customName != null && !customName.equalsIgnoreCase("null")) {
                                horse.setCustomName(Chat.parseColors(customName) +
                                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                                " " + Chat.parseColors(config.getString("Options.Display-HP")
                                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                                horse.setCustomNameVisible(true);
                            } else {
                                horse.setCustomName(Chat.parseColors(configHorse.getDisplayName()) +
                                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                                " " + Chat.parseColors(config.getString("Options.Display-HP")
                                                        .replaceAll("%health%", String.valueOf((int) horse.getHealth()))) : ""));
                            }

                            if (Main.getSpawnedHorses().containsKey(player.getName())) {
                                var list = Main.getSpawnedHorses().remove(player.getName());
                                list.add(new SpawnedHorse(uuid, name, owner, customName, storedItems,
                                        horse, configHorse.getSpeed(), false, false, null));

                                Main.getSpawnedHorses().put(player.getName(), list);
                                return;
                            }

                            Main.getSpawnedHorses().put(player.getName(), Lists.newArrayList(
                                    new SpawnedHorse(uuid, name, owner, customName, storedItems,
                                            horse, configHorse.getSpeed(), false, false, null)));

                            var seconds = configHorse.getCooldown();
                            if (seconds > 0)
                                Main.getCooldownManager().setCooldown(player.getUniqueId(), configHorse.getId(), Duration.ofSeconds(seconds));
                        });
            }
        } else {
            if (Main.getModelEngineHook() != null)
                Main.getModelEngineHook().spawn(player, this);
        }
        return true;
    }

    public void openStorage(Player player) {
        player.closeInventory();

        if (Main.getConfigFile().getBoolean("Storage-GUI.Use-Permission") &&
                !player.hasPermission(Perms.STORAGE_GUI)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        var inventory = Bukkit.createInventory(null,
                Main.getConfigFile().getInt("Storage-GUI.Size.Rows") * 9,
                Chat.parseColors(Main.getConfigFile().getString("Storage-GUI.Title")));
        inventory.setContents(storedItems == null ? new ItemStack[]{} : Serializer.deserialize(storedItems));

        player.openInventory(inventory);
        Main.getInHorseStorage().put(player.getName(), this);
    }


}