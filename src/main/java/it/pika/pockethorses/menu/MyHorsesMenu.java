package it.pika.pockethorses.menu;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.Perms;
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
                .title(Main.parseColors(Main.getConfigFile().getString("Horses-GUI.Title")))
                .size(Main.getConfigFile().getInt("Horses-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        var horses = Lists.newArrayList(Main.getHorsesOf(player));
        horses.sort((o1, o2) -> o1.getCustomName() != null && o2.getCustomName() != null
                ? o1.getCustomName().compareTo(o2.getCustomName()) : o1.getName().compareTo(o2.getName()));

        for (Horse horse : horses) {
            var configHorse = ConfigHorse.of(horse.getName());
            if (configHorse == null)
                continue;

            if (configHorse.isPermission() && !player.hasPermission(Perms.getHorse(horse.getName())))
                continue;

            contents.add(ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Horses-GUI.Horse-Item.Material")))
                    .name(Main.parseMessage(Main.getConfigFile()
                            .getString("Horses-GUI.Horse-Item.Name"), horse, player))
                    .lore(Main.parseMessage(Main.getConfigFile().getStringList("Horses-GUI.Horse-Item.Lore"), horse, player))
                    .build(), e -> {
                player.closeInventory();

                if (e.isLeftClick()) {
                    if (alreadySpawned(player, horse)) {
                        error(player, Messages.ALREADY_SPAWNED.get());
                        return;
                    }

                    if (!Main.getConfigFile().getBoolean("Options.More-Horses-At-Time")
                            && Main.getSpawnedHorses().containsKey(player.getName())) {
                        error(player, Messages.CANNOT_SPAWN.get());
                        return;
                    }

                    if (Main.getWorldGuardHook() != null
                            && Main.isWorldGuardEnabled() && !player.hasPermission(Perms.BYPASS_REGION)) {
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

                    var cooldown = Main.getCooldownManager().getRemainingCooldown(player.getUniqueId());
                    if (!cooldown.isZero() && !cooldown.isNegative()) {
                        error(player, Messages.IN_COOLDOWN.get().formatted(cooldown.toSeconds()));
                        return;
                    }

                    if (horse.spawn(player))
                        success(player, Messages.HORSE_SPAWNED.get());

                    return;
                }

                if (e.isRightClick()) {
                    if (!configHorse.isRecyclable() || !Main.isShopEnabled())
                        return;

                    new ConfirmMenu(() -> {
                        player.closeInventory();

                        Main.getStorage().takeHorse(player, horse);
                        Main.getEconomy().deposit(player, configHorse.getRecyclePrice());

                        for (List<SpawnedHorse> value : Main.getSpawnedHorses().values()) {
                            for (SpawnedHorse spawnedHorse : value) {
                                if (spawnedHorse.getUuid() != horse.getUuid())
                                    continue;

                                spawnedHorse.remove(player);
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
        if (!Main.getSpawnedHorses().containsKey(player.getName()))
            return false;

        for (SpawnedHorse spawnedHorse : Main.getSpawnedHorses().get(player.getName())) {
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
