package it.pika.pockethorses.menu;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.chat.Chat;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import lombok.AllArgsConstructor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Objects;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

@AllArgsConstructor
public class HorseMenu implements InventoryProvider {

    private SpawnedHorse horse;

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(Chat.parseColors(Main.getConfigFile().getString("Horse-GUI.Title")))
                .size(Main.getConfigFile().getInt("Horse-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        if (horse.isAutoRecall()) {
            contents.set(SlotPos.of(1, 1), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Auto-Recall.Material")))
                    .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Auto-Recall.Name"), horse, player))
                    .lore(Main.parseMessage(Main.getConfigFile().getStringList("Horse-GUI.Auto-Recall.Enabled-Lore"), horse, player))
                    .build(), e -> {
                player.closeInventory();
                horse.setAutoRecall(false);

                success(player, Messages.AUTO_RECALL_DISABLED.get());
            }));
        } else {
            contents.set(SlotPos.of(1, 1), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Auto-Recall.Material")))
                    .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Auto-Recall.Name"), horse, player))
                    .lore(Main.parseMessage(Main.getConfigFile().getStringList("Horse-GUI.Auto-Recall.Disabled-Lore"), horse, player))
                    .build(), e -> {
                player.closeInventory();
                horse.setAutoRecall(true);

                success(player, Messages.AUTO_RECALL_ENABLED.get());
            }));
        }

        var configHorse = ConfigHorse.of(horse.getName());
        if (configHorse == null)
            return;

        if (configHorse.isStorage()) {
            contents.set(SlotPos.of(1, 4), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Horse-Storage.Material")))
                    .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Horse-Storage.Name"), horse, player))
                    .build(), e -> horse.openStorage(player)));
        } else {
            contents.set(SlotPos.of(1, 4), ClickableItem.empty(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Horse-Info.Material")))
                    .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Horse-Info.Name"), horse, player))
                    .build()));
        }

        contents.set(SlotPos.of(3, 1), ClickableItem.of(changeName(), e ->
                new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title(Messages.CHANGE_NAME.get())
                        .text(Messages.CHANGE_NAME.get())
                        .itemLeft(changeName())
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT)
                                return Collections.emptyList();

                            var config = Main.getConfigFile();
                            horse.getEntity().setCustomName(Chat.parseColors(stateSnapshot.getText()) +
                                    (config.getBoolean("Options.Display-HP-In-Name") ?
                                            " " + Chat.parseColors(
                                                    Objects.requireNonNull(config.getString("Options.Display-HP"))
                                                            .replaceAll("%health%", String.valueOf((int)
                                                                    ((AbstractHorse) horse.getEntity()).getHealth()))) : ""));
                            Main.getStorage().setCustomName(horse, stateSnapshot.getText());

                            success(player, Messages.CUSTOM_NAME_SET.get());
                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        }).open(player)));

        if (horse.isSit()) {
            contents.set(SlotPos.of(3, 3), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Get-Up.Material")))
                    .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Get-Up.Name"), horse, player))
                    .build(), e -> {
                player.closeInventory();
                horse.setSit(false);
                ((AbstractHorse) horse.getEntity()).setAI(true);
                ((AbstractHorse) horse.getEntity()).setTarget(player);

                if (Main.isModelEngineEnabled() && Main.getModelEngineHook() != null
                        && horse.getModeledEntity() != null)
                    Main.getModelEngineHook().makeIdle(horse);

                success(player, Messages.GET_UP.get());
            }));
        } else {
            contents.set(SlotPos.of(3, 3), ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Make-Sit.Material")))
                    .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Make-Sit.Name"), horse, player))
                    .build(), e -> {
                player.closeInventory();
                horse.setSit(true);
                ((AbstractHorse) horse.getEntity()).setAI(false);

                if (Main.isModelEngineEnabled() && Main.getModelEngineHook() != null
                        && horse.getModeledEntity() != null)
                    Main.getModelEngineHook().makeIdle(horse);

                success(player, Messages.MAKE_SIT.get());
            }));
        }

        contents.set(SlotPos.of(3, 5), ClickableItem.of(setSpeed(), e ->
                new AnvilGUI.Builder()
                        .plugin(Main.getInstance())
                        .title(Messages.SET_SPEED.get())
                        .text(Messages.SET_SPEED.get())
                        .itemLeft(setSpeed())
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT)
                                return Collections.emptyList();

                            if (!isDouble(stateSnapshot.getText())) {
                                error(player, Messages.INVALID_NUMBER.get());
                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                            }

                            var speed = Double.parseDouble(stateSnapshot.getText());
                            if (speed <= 0 || speed > configHorse.getSpeed()) {
                                error(player, Messages.INVALID_SPEED.get());
                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                            }

                            var speedModifier = speed / 3.6;
                            Objects.requireNonNull(((AbstractHorse) horse.getEntity())
                                    .getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speedModifier / 20);
                            horse.setSpeed(speed);
                            success(player, Messages.SPEED_SET.get());
                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        }).open(player)));

        contents.set(SlotPos.of(3, 7), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Remove.Material")))
                .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Remove.Name"), horse, player))
                .build(), e -> {
            player.closeInventory();
            horse.remove(player);
            success(player, Messages.HORSE_REMOVED.get());
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private ItemStack changeName() {
        return new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Change-Name.Material")))
                .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Change-Name.Name"), horse, null))
                .build();
    }

    private ItemStack setSpeed() {
        return new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Horse-GUI.Set-Speed.Material")))
                .name(Main.parseMessage(Main.getConfigFile().getString("Horse-GUI.Set-Speed.Name"), horse, null))
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
