package it.pika.pockethorses.menu;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Main;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ConfirmMenu implements InventoryProvider {

    private Runnable onConfirm;
    private Runnable onCancel;

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(Main.getConfigFile().getString("Confirm-GUI.Title"))
                .size(Main.getConfigFile().getInt("Confirm-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        var config = Main.getConfigFile();

        contents.set(SlotPos.of(1, 4), ClickableItem.empty(new ItemBuilder()
                .material(Material.valueOf(config.getString("Confirm-GUI.Are-You-Sure.Material")))
                .name(Main.parseColors(config.getString("Confirm-GUI.Are-You-Sure.Name")))
                .lore(Main.parseColors(config.getStringList("Confirm-GUI.Are-You-Sure.Lore")))
                .build()));

        contents.set(SlotPos.of(3, 2), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Confirm-GUI.Confirm.Material")))
                .name(Main.parseColors(config.getString("Confirm-GUI.Confirm.Name")))
                .lore(Main.parseColors(config.getStringList("Confirm-GUI.Confirm.Lore")))
                .build(), e -> onConfirm.run()));

        contents.set(SlotPos.of(3, 6), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(config.getString("Confirm-GUI.Cancel.Material")))
                .name(Main.parseColors(config.getString("Confirm-GUI.Cancel.Name")))
                .lore(Main.parseColors(config.getStringList("Confirm-GUI.Cancel.Lore")))
                .build(), e -> onCancel.run()));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}
