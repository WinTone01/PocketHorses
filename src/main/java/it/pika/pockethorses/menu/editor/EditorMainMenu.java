package it.pika.pockethorses.menu.editor;

import com.google.common.collect.Lists;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.ConfirmMenu;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.EditingHorse;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static it.pika.libs.chat.Chat.error;

public class EditorMainMenu implements InventoryProvider {

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(Main.getConfigFile().getString("Editor-GUI.Main.Title"))
                .size(Main.getConfigFile().getInt("Editor-GUI.Main.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        var pagination = contents.pagination();
        var horses = Lists.newArrayList(Main.getLoadedHorses());
        horses.removeIf(Objects::isNull);

        ClickableItem[] items = new ClickableItem[horses.size()];

        for (int i = 0; i < items.length; i++) {
            var horse = horses.get(i);

            items[i] = ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Editor-GUI.Main.Horse-Item.Material")))
                    .name(parse(Objects.requireNonNull(Main.getConfigFile()
                            .getString("Editor-GUI.Main.Horse-Item.Name")), horse))
                    .lore(parse(Main.getConfigFile().getStringList("Editor-GUI.Main.Horse-Item.Lore"), horse))
                    .build(), e -> {
                if (e.isLeftClick()) {
                    player.closeInventory();

                    new EditingHorseMenu(new EditingHorse(horse.getId(), horse.getDisplayName(), horse.getColor(),
                            horse.getStyle(), horse.getSpeed(), horse.getJumpStrength(), horse.getMaxHealth(),
                            horse.isBuyable(), horse.getPrice(), horse.isPermission(), horse.isStorage(),
                            horse.isRecyclable(), horse.getRecyclePrice(), horse.getModel() != null ? horse.getModel() : null), false)
                            .get().open(player);
                }

                if (e.isRightClick()) {
                    new ConfirmMenu(() -> {
                        player.closeInventory();
                        try {
                            Files.delete(horse.getConfig().getFile().toPath());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        reloadHorses();
                        get().open(player);
                    }, () -> get().open(player)).get().open(player);
                }
            });
        }

        pagination.setItems(items);
        pagination.setItemsPerPage((Main.getConfigFile().getInt("Editor-GUI.Main.Size.Rows") * 9) - 9);

        var iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0);
        iterator.blacklist(5, 0).blacklist(5, 1).blacklist(5, 2)
                .blacklist(5, 3).blacklist(5, 4).blacklist(5, 5)
                .blacklist(5, 6).blacklist(5, 7).blacklist(5, 8);
        pagination.addToIterator(iterator);

        contents.set(SlotPos.of(5, 4), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Editor-GUI.Main.New-Horse.Material")))
                .name(Main.parseColors(Main.getConfigFile().getString("Editor-GUI.Main.New-Horse.Name")))
                .lore(Main.parseColors(Main.getConfigFile().getStringList("Editor-GUI.Main.New-Horse.Lore")))
                .build(), e -> {
            player.closeInventory();

            new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Set name")
                    .text("Set name")
                    .itemLeft(new ItemStack(Material.PAPER))
                    .onClick((slot, stateSnapshot) -> {
                        if (Main.getLoadedHorse(stateSnapshot.getText()) != null) {
                            error(player, Messages.HORSE_ALREADY_EXISTS.get());
                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        }

                        var editingHorse = new EditingHorse();
                        editingHorse.setId(stateSnapshot.getText());

                        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() ->
                                new EditingHorseMenu(editingHorse, true).get().open(player)));
                    }).open(player);
        }));

        contents.set(SlotPos.of(5, 3), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Editor-GUI.Main.Previous-Page.Material")))
                .name(Main.parseColors(Main.getConfigFile().getString("Editor-GUI.Main.Previous-Page.Name")))
                .lore(Main.parseColors(Main.getConfigFile().getStringList("Editor-GUI.Main.Previous-Page.Lore")))
                .build(), e -> contents.inventory().open(player, pagination.previous().getPage())));

        contents.set(SlotPos.of(5, 5), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Editor-GUI.Main.Next-Page.Material")))
                .name(Main.parseColors(Main.getConfigFile().getString("Editor-GUI.Main.Next-Page.Name")))
                .lore(Main.parseColors(Main.getConfigFile().getStringList("Editor-GUI.Main.Next-Page.Lore")))
                .build(), e -> contents.inventory().open(player, pagination.next().getPage())));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private String parse(String message, ConfigHorse horse) {
        return Main.parseColors(message
                .replaceAll("%id%", horse.getId())
                .replaceAll("%displayName%", horse.getDisplayName())
                .replaceAll("%color%", horse.getColor().name())
                .replaceAll("%style%", horse.getStyle().name())
                .replaceAll("%speed%", String.valueOf(horse.getSpeed()))
                .replaceAll("%jumpStrength%", String.valueOf(horse.getJumpStrength()))
                .replaceAll("%maxHealth%", String.valueOf(horse.getMaxHealth()))
                .replaceAll("%buyable%", String.valueOf(horse.isBuyable()))
                .replaceAll("%price%", String.valueOf(horse.getPrice()))
                .replaceAll("%permission%", String.valueOf(horse.isPermission()))
                .replaceAll("%storage%", String.valueOf(horse.isStorage())));
    }

    private List<String> parse(List<String> list, ConfigHorse horse) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(parse(s, horse));

        return newList;
    }

    private void reloadHorses() {
        Main.getConsole().info("Reloading horses...");
        Main.getLoadedHorses().clear();
        Main.getInstance().loadHorses();
    }

}
