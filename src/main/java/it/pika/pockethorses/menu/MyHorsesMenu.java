package it.pika.pockethorses.menu;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
            var configHorse = ConfigHorse.of(horse.getName());
            if (configHorse == null)
                continue;

            if (configHorse.isPermission() && !player.hasPermission(Perms.getHorse(horse.getName())))
                continue;

            contents.add(ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horses-GUI.Horse-Item.Material")))
                    .name(PocketHorses.parseMessage(PocketHorses.getConfigFile()
                            .getString("Horses-GUI.Horse-Item.Name"), horse, player))
                    .lore(PocketHorses.parseMessage(PocketHorses.getConfigFile().getStringList("Horses-GUI.Horse-Item.Lore"), horse, player))
                    .build(), e -> {
                player.closeInventory();

                if (e.isLeftClick()) {
                    if (alreadySpawned(player, horse)) {
                        error(player, Messages.ALREADY_SPAWNED.get());
                        return;
                    }

                    if (!PocketHorses.getConfigFile().getBoolean("Options.More-Horses-At-Time")
                            && PocketHorses.getSpawnedHorses().containsKey(player.getName())) {
                        error(player, Messages.CANNOT_SPAWN.get());
                        return;
                    }

                    if (PocketHorses.isWorldGuardEnabled() && !player.hasPermission(Perms.BYPASS_REGION)) {
                        for (ProtectedRegion region : getRegions(player)) {
                            for (Map.Entry<Flag<?>, Object> entry : region.getFlags().entrySet()) {
                                if (!entry.getKey().getName().equalsIgnoreCase("allow-horses"))
                                    continue;

                                if (!entry.getValue().toString().equalsIgnoreCase("DENY"))
                                    continue;

                                error(player, Messages.CANNOT_SPAWN.get());
                                return;
                            }
                        }
                    }

                    var cooldown = PocketHorses.getCooldownManager().getRemainingCooldown(player.getUniqueId());
                    if (!cooldown.isZero() && !cooldown.isNegative()) {
                        error(player, Messages.IN_COOLDOWN.get().formatted(cooldown.toSeconds()));
                        return;
                    }

                    if (horse.spawn(player))
                        success(player, Messages.HORSE_SPAWNED.get());

                    return;
                }

                if (e.isRightClick()) {
                    if (!configHorse.isRecyclable() || !PocketHorses.isShopEnabled())
                        return;

                    new ConfirmMenu(() -> {
                        player.closeInventory();

                        PocketHorses.getStorage().takeHorse(player, horse);
                        PocketHorses.getEconomy().deposit(player, configHorse.getRecyclePrice());

                        for (List<SpawnedHorse> value : PocketHorses.getSpawnedHorses().values()) {
                            for (SpawnedHorse spawnedHorse : value) {
                                if (spawnedHorse.getUuid() != horse.getUuid())
                                    continue;

                                spawnedHorse.getEntity().remove();
                                if (PocketHorses.getModelEngineHook() != null)
                                    PocketHorses.getModelEngineHook().remove(spawnedHorse);
                            }
                        }

                        success(player, Messages.HORSE_RECYCLED.get());
                    }, player::closeInventory).get().open(player);
                }
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

    public static Set<ProtectedRegion> getRegions(Player player) {
        var container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.createQuery().getApplicableRegions(WorldGuardPlugin.inst().wrapPlayer(player)
                .getLocation()).getRegions();
    }

}
