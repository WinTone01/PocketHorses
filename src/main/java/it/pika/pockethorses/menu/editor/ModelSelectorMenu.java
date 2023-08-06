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
import org.bukkit.entity.Player;

import java.util.Objects;

@AllArgsConstructor
public class ModelSelectorMenu implements InventoryProvider {

    private EditingHorse horse;
    private EditingHorseMenu parent;

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(PocketHorses.getConfigFile().getString("Editor-GUI.Model-GUI.Title"))
                .size(PocketHorses.getConfigFile().getInt("Editor-GUI.Model-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(PocketHorses.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        if (!PocketHorses.isModelEngineEnabled() || PocketHorses.getModelEngineHook() == null)
            return;

        var config = PocketHorses.getConfigFile();

        for (String model : PocketHorses.getModelEngineHook().getModels()) {
            contents.add(ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(config.getString("Editor-GUI.Model-GUI.Model-Item.Material")))
                    .name(PocketHorses.parseColors(Objects.requireNonNull(
                            config.getString("Editor-GUI.Model-GUI.Model-Item.Name")).replaceAll("%model%", model)))
                    .lore(PocketHorses.parseColors(config.getStringList("Editor-GUI.Model-GUI.Model-Item.Lore")))
                    .build(), e -> {
                horse.setModel(model);
                new EditingHorseMenu(horse, parent.isCreating()).get().open(player);
            }));
        }

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
