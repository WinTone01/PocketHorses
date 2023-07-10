package it.pika.pockethorses.menu;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.api.events.HorseDespawnEvent;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.ConfigHorse;
import it.pika.pockethorses.objects.SpawnedHorse;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

@AllArgsConstructor
public class HorseMenu implements InventoryProvider {

    private SpawnedHorse horse;

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Horse-GUI.Title")))
                .size(PocketHorses.getConfigFile().getInt("Horse-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(PocketHorses.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        if (ConfigHorse.of(horse.getName()).isStorage()) {
            contents.set(SlotPos.of(1, 4), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horse-GUI.Horse-Storage.Material")))
                    .name(PocketHorses.parseMessage(PocketHorses.getConfigFile().getString("Horse-GUI.Horse-Storage.Name"), horse, player))
                    .build(), e -> horse.openStorage(player)));
        } else {
            contents.set(SlotPos.of(1, 4), ClickableItem.empty(new ItemBuilder()
                    .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horse-GUI.Horse-Info.Material")))
                    .name(PocketHorses.parseMessage(PocketHorses.getConfigFile().getString("Horse-GUI.Horse-Info.Name"), horse, player))
                    .build()));
        }

        contents.set(SlotPos.of(3, 1), ClickableItem.of(changeName(), e ->
                new AnvilGUI.Builder()
                        .plugin(PocketHorses.getInstance())
                        .title(Messages.CHANGE_NAME.get())
                        .text(Messages.CHANGE_NAME.get())
                        .itemLeft(changeName())
                        .onComplete((p, val) -> {
                            PocketHorses.getStorage().setCustomName(horse, val);
                            horse.getEntity().customName(Component.text(PocketHorses.parseColors(val)));
                            success(player, Messages.CUSTOM_NAME_SET.get());

                            return AnvilGUI.Response.close();
                        }).open(player)));

        if (horse.isSit()) {
            contents.set(SlotPos.of(3, 3), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horse-GUI.Get-Up.Material")))
                    .name(PocketHorses.parseMessage(PocketHorses.getConfigFile().getString("Horse-GUI.Get-Up.Name"), horse, player))
                    .build(), e -> {
                player.closeInventory();
                horse.setSit(false);
                ((Horse) horse.getEntity()).setAI(true);
                ((Horse) horse.getEntity()).setTarget(player);

                success(player, Messages.GET_UP.get());
            }));
        } else {
            contents.set(SlotPos.of(3, 3), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horse-GUI.Make-Sit.Material")))
                    .name(PocketHorses.parseMessage(PocketHorses.getConfigFile().getString("Horse-GUI.Make-Sit.Name"), horse, player))
                    .build(), e -> {
                player.closeInventory();
                horse.setSit(true);
                ((Horse) horse.getEntity()).setAI(false);

                success(player, Messages.MAKE_SIT.get());
            }));
        }

        contents.set(SlotPos.of(3, 5), ClickableItem.of(setSpeed(), e ->
                new AnvilGUI.Builder()
                        .plugin(PocketHorses.getInstance())
                        .title(Messages.SET_SPEED.get())
                        .text(Messages.SET_SPEED.get())
                        .itemLeft(setSpeed())
                        .onComplete((p, val) -> {
                            if (!isDouble(val)) {
                                error(player, Messages.INVALID_NUMBER.get());
                                return AnvilGUI.Response.close();
                            }

                            var speed = Double.parseDouble(val);
                            if (speed <= 0 || speed > ConfigHorse.of(horse.getName()).getSpeed()) {
                                error(player, Messages.INVALID_SPEED.get());
                                return AnvilGUI.Response.close();
                            }

                            var speedModifier = speed / 3.6;
                            ((Horse) horse.getEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                                    .setBaseValue(speedModifier / 20);
                            horse.setSpeed(speed);
                            success(player, Messages.SPEED_SET.get());

                            return AnvilGUI.Response.close();
                        }).open(player)));

        contents.set(SlotPos.of(3, 7), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horse-GUI.Remove.Material")))
                .name(PocketHorses.parseMessage(PocketHorses.getConfigFile().getString("Horse-GUI.Remove.Name"), horse, player))
                .build(), e -> {
            player.closeInventory();

            var event = new HorseDespawnEvent(player, horse);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled())
                return;

            PocketHorses.getSpawnedHorses().get(player.getName()).remove(horse);
            horse.getEntity().remove();

            success(player, Messages.HORSE_REMOVED.get());
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private ItemStack changeName() {
        return new ItemBuilder()
                .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horse-GUI.Change-Name.Material")))
                .name(PocketHorses.parseMessage(PocketHorses.getConfigFile().getString("Horse-GUI.Change-Name.Name"), horse, null))
                .build();
    }

    private ItemStack setSpeed() {
        return new ItemBuilder()
                .material(Material.valueOf(PocketHorses.getConfigFile().getString("Horse-GUI.Set-Speed.Material")))
                .name(PocketHorses.parseMessage(PocketHorses.getConfigFile().getString("Horse-GUI.Set-Speed.Name"), horse, null))
                .build();
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
