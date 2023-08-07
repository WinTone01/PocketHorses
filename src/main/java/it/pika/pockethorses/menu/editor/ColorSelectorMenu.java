package it.pika.pockethorses.menu.editor;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.HorseColor;
import it.pika.pockethorses.objects.horses.EditingHorse;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;

@AllArgsConstructor
public class ColorSelectorMenu implements InventoryProvider {

    private EditingHorse horse;
    private EditingHorseMenu parent;

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(Main.getConfigFile().getString("Editor-GUI.Color-GUI.Title"))
                .size(Main.getConfigFile().getInt("Editor-GUI.Color-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        for (HorseColor value : HorseColor.values())
            contents.add(ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Editor-GUI.Color-GUI.Color-Item.Material")))
                    .name(Objects.requireNonNull(Main.getConfigFile()
                                    .getString("Editor-GUI.Color-GUI.Color-Item.Name"))
                            .replaceAll("%color%", value.name()))
                    .lore(Main.getConfigFile().getStringList("Editor-GUI.Color-GUI.Color-Item.Lore"))
                    .build(), e -> {
                horse.setColor(value);
                new EditingHorseMenu(horse, parent.isCreating()).get().open(player);
            }));

        contents.set(SlotPos.of(3, 4), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Editor-GUI.Editing-GUI.Back.Material")))
                .name(Main.getConfigFile().getString("Editor-GUI.Editing-GUI.Back.Name"))
                .lore(Main.getConfigFile().getStringList("Editor-GUI.Editing-GUI.Back.Lore"))
                .build(), e -> new EditingHorseMenu(horse, parent.isCreating()).get().open(player)));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}
