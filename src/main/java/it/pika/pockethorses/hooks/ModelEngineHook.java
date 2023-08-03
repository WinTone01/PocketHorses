package it.pika.pockethorses.hooks;

import com.google.common.collect.Lists;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Objects;

import static it.pika.libs.chat.Chat.error;

public class ModelEngineHook {

    public void spawn(Player player, Horse horse) {
        var config = PocketHorses.getConfigFile();
        var configHorse = ConfigHorse.of(horse.getName());
        if (configHorse == null)
            return;

        if (!PocketHorses.isModelEngineEnabled()) {
            PocketHorses.getConsole().warning("ModelEngine is not enabled on this server!");
            error(player, "An error was encountered, check the console for more information!");
            return;
        }

        var entity = player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Horse.class, horseEntity -> {
            horseEntity.setAdult();
            horseEntity.setTamed(true);
            horseEntity.setOwner(player);
            horseEntity.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            horseEntity.setInvisible(true);

            horseEntity.setTarget(player);

            if (PocketHorses.getActiveSupplements().containsKey(horse.getUuid())) {
                var supplement = PocketHorses.getActiveSupplements().get(horse.getUuid());

                var speed = (configHorse.getSpeed() + supplement.getExtraSpeed()) / 3.6;
                Objects.requireNonNull(horseEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                var jumpStrength = configHorse.getJumpStrength() + supplement.getExtraJump();
                horseEntity.setJumpStrength(Math.min(jumpStrength, 2.0));
            } else {
                double speed = configHorse.getSpeed() / 3.6;
                Objects.requireNonNull(horseEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed / 20);

                horseEntity.setJumpStrength(configHorse.getJumpStrength());
            }

            Objects.requireNonNull(horseEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH))
                    .setBaseValue(configHorse.getMaxHealth());
            horseEntity.setHealth(configHorse.getMaxHealth());

            if (horse.getCustomName() != null && !horse.getCustomName().equalsIgnoreCase("null")) {
                horseEntity.setCustomName(PocketHorses.parseColors(horse.getCustomName()) +
                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                " " + PocketHorses.parseColors(config.getString("Options.Display-HP")
                                        .replaceAll("%health%", String.valueOf((int) horseEntity.getHealth()))) : ""));
                horseEntity.setCustomNameVisible(true);
            } else {
                horseEntity.setCustomName(PocketHorses.parseColors(configHorse.getDisplayName()) +
                        (config.getBoolean("Options.Display-HP-In-Name") ?
                                " " + PocketHorses.parseColors(config.getString("Options.Display-HP")
                                        .replaceAll("%health%", String.valueOf((int) horseEntity.getHealth()))) : ""));
            }

            var seconds = PocketHorses.getConfigFile().getInt("Options.Horse-Cooldown");
            if (seconds > 0)
                PocketHorses.getCooldownManager().setCooldown(player.getUniqueId(), Duration.ofSeconds(seconds));
        });

        var blueprint = com.ticxo.modelengine.api.ModelEngineAPI.getBlueprint(configHorse.getModel());
        if (blueprint == null) {
            PocketHorses.getConsole().warning("The %s model for the %s horse does not exist on ModelEngine!"
                    .formatted(configHorse.getModel(), configHorse.getId()));
            error(player, "An error was encountered, check the console for more information!");
            return;
        }

        var model = com.ticxo.modelengine.api.ModelEngineAPI.api.createActiveModelImpl(blueprint);
        if (model == null) {
            PocketHorses.getConsole().warning("The %s model for the %s horse does not exist on ModelEngine!"
                    .formatted(configHorse.getModel(), configHorse.getId()));
            error(player, "An error was encountered, check the console for more information!");
            return;
        }

        var modeledEntity = com.ticxo.modelengine.api.ModelEngineAPI.api
                .createModeledEntityImpl(new com.ticxo.modelengine.api.entity.BukkitEntity(entity));
        if (modeledEntity == null) {
            PocketHorses.getConsole().warning("The %s model for the %s horse does not exist on ModelEngine!"
                    .formatted(configHorse.getModel(), configHorse.getId()));
            error(player, "An error was encountered, check the console for more information!");
            return;
        }

        modeledEntity.addModel(model, true);

        if (PocketHorses.getSpawnedHorses().containsKey(player.getName())) {
            var list = PocketHorses.getSpawnedHorses().remove(player.getName());
            list.add(new SpawnedHorse(horse.getUuid(), horse.getName(), horse.getOwner(), horse.getCustomName(),
                    horse.getStoredItems(), entity, configHorse.getSpeed(), false, false, modeledEntity));

            PocketHorses.getSpawnedHorses().put(player.getName(), list);
            return;
        }

        PocketHorses.getSpawnedHorses().put(player.getName(), Lists.newArrayList(
                new SpawnedHorse(horse.getUuid(), horse.getName(), horse.getOwner(), horse.getCustomName(),
                        horse.getStoredItems(), entity, configHorse.getSpeed(), false, false, modeledEntity)));
    }

    public void remove(SpawnedHorse horse) {
        if (!(horse.getModeledEntity() instanceof com.ticxo.modelengine.api.model.ModeledEntity))
            return;

        ((com.ticxo.modelengine.api.model.ModeledEntity) horse.getModeledEntity()).destroy();
    }

}
