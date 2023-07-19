package it.pika.pockethorses.menu.editor;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.objects.horses.EditingHorse;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class StyleSelectorMenu implements InventoryProvider {

    private EditingHorse horse;
    private EditingHorseMenu parent;

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(PocketHorses.getConfigFile().getString("Editor-GUI.Style-GUI.Title"))
                .size(PocketHorses.getConfigFile().getInt("Editor-GUI.Style-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(PocketHorses.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        for (Horse.Style value : Horse.Style.values())
            contents.add(ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(PocketHorses.getConfigFile().getString("Editor-GUI.Style-GUI.Style-Item.Material")))
                    .name(PocketHorses.getConfigFile().getString("Editor-GUI.Style-GUI.Style-Item.Name")
                            .replaceAll("%style%", value.name()))
                    .lore(PocketHorses.getConfigFile().getStringList("Editor-GUI.Style-GUI.Style-Item.Lore"))
                    .build(), e -> {
                horse.setStyle(value);
                new EditingHorseMenu(horse, parent.isCreating()).get().open(player);
            }));

        contents.set(SlotPos.of(3, 4), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(PocketHorses.getConfigFile().getString("Editor-GUI.Editing-GUI.Back.Material")))
                .name(PocketHorses.getConfigFile().getString("Editor-GUI.Editing-GUI.Back.Name"))
                .lore(PocketHorses.getConfigFile().getStringList("Editor-GUI.Editing-GUI.Back.Lore"))
                .build(), e -> new EditingHorseMenu(horse, parent.isCreating()).get().open(player)));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}