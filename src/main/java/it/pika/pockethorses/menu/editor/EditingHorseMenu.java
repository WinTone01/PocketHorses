package it.pika.pockethorses.menu.editor;

import com.google.common.collect.Lists;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.config.Config;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.EditingHorse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

@AllArgsConstructor
@Getter
public class EditingHorseMenu implements InventoryProvider {

    private EditingHorse horse;
    private boolean creating;

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(parse(PocketHorses.getConfigFile().getString("Editor-GUI.Editing-GUI.Title")))
                .size(PocketHorses.getConfigFile().getInt("Editor-GUI.Editing-GUI.Size.Rows"), 9)
                .manager(PocketHorses.getInventoryManager())
                .provider(this)
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        var config = PocketHorses.getConfigFile();

        contents.set(SlotPos.of(1, 1), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Display-Name.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Display-Name.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Display-Name.Lore")))
                .build(), e -> new AnvilGUI.Builder()
                .plugin(PocketHorses.getInstance())
                .title("Set value")
                .text("Set value")
                .itemLeft(new ItemStack(Material.PAPER))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT)
                        return Collections.emptyList();

                    horse.setDisplayName(PocketHorses.parseColors(stateSnapshot.getText()));
                    return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                })
                .open(player)));

        contents.set(SlotPos.of(1, 3), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Color.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Color.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Color.Lore")))
                .build(), e -> {
            new ColorSelectorMenu(horse, this).get().open(player);
        }));

        contents.set(SlotPos.of(1, 5), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Style.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Style.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Style.Lore")))
                .build(), e -> {
            new StyleSelectorMenu(horse, this).get().open(player);
        }));

        contents.set(SlotPos.of(1, 7), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Speed.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Speed.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Speed.Lore")))
                .build(), e -> new AnvilGUI.Builder()
                .plugin(PocketHorses.getInstance())
                .title("Set value")
                .text("Set value")
                .itemLeft(new ItemStack(Material.PAPER))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT)
                        return Collections.emptyList();

                    if (!isDouble(stateSnapshot.getText())) {
                        error(player, Messages.INVALID_NUMBER.get());
                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                    }

                    horse.setSpeed(Double.parseDouble(stateSnapshot.getText()));
                    return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                })
                .open(player)));

        contents.set(SlotPos.of(2, 2), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Permission.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Permission.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Permission.Lore")))
                .build(), e -> {
            horse.setPermission(!horse.isPermission());
            get().open(player);
        }));

        contents.set(SlotPos.of(2, 4), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Recyclable.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Recyclable.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Recyclable.Lore")))
                .build(), e -> {
            horse.setRecyclable(!horse.isRecyclable());
            get().open(player);
        }));

        contents.set(SlotPos.of(2, 6), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Storage.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Storage.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Storage.Lore")))
                .build(), e -> {
            horse.setStorage(!horse.isStorage());
            get().open(player);
        }));

        contents.set(SlotPos.of(3, 1), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Jump-Strength.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Jump-Strength.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Jump-Strength.Lore")))
                .build(), e -> new AnvilGUI.Builder()
                .plugin(PocketHorses.getInstance())
                .title("Set value")
                .text("Set value")
                .itemLeft(new ItemStack(Material.PAPER))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT)
                        return Collections.emptyList();

                    if (!isDouble(stateSnapshot.getText()) || Double.parseDouble(stateSnapshot.getText()) > 2.0) {
                        error(player, Messages.INVALID_NUMBER.get());
                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                    }

                    horse.setJumpStrength(Double.parseDouble(stateSnapshot.getText()));
                    return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                })
                .open(player)));

        contents.set(SlotPos.of(3, 3), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Max-Health.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Max-Health.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Max-Health.Lore")))
                .build(), e -> new AnvilGUI.Builder()
                .plugin(PocketHorses.getInstance())
                .title("Set value")
                .text("Set value")
                .itemLeft(new ItemStack(Material.PAPER))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT)
                        return Collections.emptyList();

                    if (!isInt(stateSnapshot.getText())) {
                        error(player, Messages.INVALID_NUMBER.get());
                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                    }

                    horse.setMaxHealth(Integer.parseInt(stateSnapshot.getText()));
                    return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                })
                .open(player)));

        contents.set(SlotPos.of(3, 5), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Buyable.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Buyable.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Buyable.Lore")))
                .build(), e -> {
            horse.setBuyable(!horse.isBuyable());
            get().open(player);
        }));

        contents.set(SlotPos.of(3, 7), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Price.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Price.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Price.Lore")))
                .build(), e -> new AnvilGUI.Builder()
                .plugin(PocketHorses.getInstance())
                .title("Set value")
                .text("Set value")
                .itemLeft(new ItemStack(Material.PAPER))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT)
                        return Collections.emptyList();

                    if (!isDouble(stateSnapshot.getText())) {
                        error(player, Messages.INVALID_NUMBER.get());
                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                    }

                    horse.setPrice(Double.parseDouble(stateSnapshot.getText()));
                    return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                })
                .open(player)));

        contents.set(SlotPos.of(4, 4), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Recycle-Price.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Recycle-Price.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Recycle-Price.Lore")))
                .build(), e -> new AnvilGUI.Builder()
                .plugin(PocketHorses.getInstance())
                .title("Set value")
                .text("Set value")
                .itemLeft(new ItemStack(Material.PAPER))
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT)
                        return Collections.emptyList();

                    if (!isDouble(stateSnapshot.getText())) {
                        error(player, Messages.INVALID_NUMBER.get());
                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                    }

                    horse.setRecyclePrice(Double.parseDouble(stateSnapshot.getText()));
                    return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() -> get().open(player)));
                })
                .open(player)));

        contents.set(SlotPos.of(5, 0), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Back.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Back.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Back.Lore")))
                .build(), e -> new EditorMainMenu().get().open(player)));

        contents.set(SlotPos.of(5, 8), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Editor-GUI.Editing-GUI.Confirm.Material")))
                .name(parse(config.getString("Editor-GUI.Editing-GUI.Confirm.Name")))
                .lore(parse(config.getStringList("Editor-GUI.Editing-GUI.Confirm.Lore")))
                .build(), e -> {
            player.closeInventory();

            if (creating) {
                var file = new File(PocketHorses.getInstance().getDataFolder()
                        + File.separator + "Horses" + File.separator + "%s.yml".formatted(horse.getId()));
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                if (horse.getDisplayName() == null || horse.getColor() == null || horse.getStyle() == null
                        || horse.getJumpStrength() == 0 || horse.getMaxHealth() == 0 || horse.getPrice() == 0) {
                    error(player, Messages.SET_ALL_SETTINGS.get());
                    return;
                }

                var horseConfig = new Config(PocketHorses.getInstance(), file);
                horseConfig.set("Display-Name", horse.getDisplayName());
                horseConfig.set("Color", horse.getColor().name());
                horseConfig.set("Style", horse.getStyle().name());
                horseConfig.set("Speed", horse.getSpeed());
                horseConfig.set("Jump-Strength", horse.getJumpStrength());
                horseConfig.set("Max-Health", horse.getMaxHealth());
                horseConfig.set("Buyable", horse.isBuyable());
                horseConfig.set("Price", horse.getPrice());
                horseConfig.set("Permission", horse.isPermission());
                horseConfig.set("Storage", horse.isStorage());
                horseConfig.set("Recyclable", horse.isRecyclable());
                horseConfig.set("Recycle-Price", horse.getRecyclePrice());
                horseConfig.save();

                reloadHorses();
                success(player, Messages.HORSE_CREATED.get());
                return;
            }

            var configHorse = ConfigHorse.of(horse.getId());
            if (configHorse == null) {
                error(player, "Internal PocketHorses error");
                return;
            }

            var horseConfig = configHorse.getConfig();
            horseConfig.set("Display-Name", horse.getDisplayName());
            horseConfig.set("Color", horse.getColor().name());
            horseConfig.set("Style", horse.getStyle().name());
            horseConfig.set("Speed", horse.getSpeed());
            horseConfig.set("Jump-Strength", horse.getJumpStrength());
            horseConfig.set("Max-Health", horse.getMaxHealth());
            horseConfig.set("Buyable", horse.isBuyable());
            horseConfig.set("Price", horse.getPrice());
            horseConfig.set("Permission", horse.isPermission());
            horseConfig.set("Storage", horse.isStorage());
            horseConfig.set("Recyclable", horse.isRecyclable());
            horseConfig.set("Recycle-Price", horse.getRecyclePrice());
            horseConfig.save();

            reloadHorses();
            success(player, Messages.HORSE_EDITED.get());
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

    private String parse(String s) {
        return PocketHorses.parseColors(s)
                .replaceAll("%id%", horse.getId() != null ? horse.getId() : Messages.UNDEFINED.get())
                .replaceAll("%displayName%", horse.getDisplayName() != null ? horse.getDisplayName() : Messages.UNDEFINED.get())
                .replaceAll("%color%", horse.getColor() != null ? horse.getColor().name() : Messages.UNDEFINED.get())
                .replaceAll("%style%", horse.getStyle() != null ? horse.getStyle().name() : Messages.UNDEFINED.get())
                .replaceAll("%speed%", horse.getSpeed() != 0 ? String.valueOf(horse.getSpeed()) : Messages.UNDEFINED.get())
                .replaceAll("%jumpStrength%", horse.getJumpStrength() != 0 ? String.valueOf(horse.getJumpStrength()) : Messages.UNDEFINED.get())
                .replaceAll("%maxHealth%", horse.getMaxHealth() != 0 ? String.valueOf(horse.getMaxHealth()) : Messages.UNDEFINED.get())
                .replaceAll("%buyable%", horse.isBuyable() ? Messages.ENABLED.get() : Messages.DISABLED.get())
                .replaceAll("%price%", horse.getPrice() != 0 ? String.valueOf(horse.getPrice()) : Messages.UNDEFINED.get())
                .replaceAll("%permission%", horse.isPermission() ? Messages.ENABLED.get() : Messages.DISABLED.get())
                .replaceAll("%storage%", horse.isStorage() ? Messages.ENABLED.get() : Messages.DISABLED.get())
                .replaceAll("%recyclable%", horse.isRecyclable() ? Messages.ENABLED.get() : Messages.DISABLED.get())
                .replaceAll("%recyclePrice%", horse.getRecyclePrice() != 0 ? String.valueOf(horse.getRecyclePrice()) : Messages.UNDEFINED.get());
    }

    private List<String> parse(List<String> list) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(parse(s));

        return newList;
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void reloadHorses() {
        PocketHorses.getConsole().info("Reloading horses...");
        PocketHorses.getLoadedHorses().clear();
        PocketHorses.getInstance().loadHorses();
    }

}
