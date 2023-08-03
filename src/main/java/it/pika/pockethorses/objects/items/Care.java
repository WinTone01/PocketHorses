package it.pika.pockethorses.objects.items;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.PocketHorses;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Care {

    private String id;
    private double restoreHealth;
    private ItemStack item;

    public static Care of(String name) {
        var config = PocketHorses.getItemsFile();

        try {
            if (!Objects.requireNonNull(config.getString("%s.Type".formatted(name)))
                    .equalsIgnoreCase("CARE")) {
                PocketHorses.getConsole().warning("%s is not a valid care!".formatted(name));
                return null;
            }

            var restoreHealth = config.getDouble("%s.Restore-Health".formatted(name));

            var item = new ItemBuilder()
                    .material(Material.valueOf(config.getString("%s.Item.Material".formatted(name))))
                    .name(PocketHorses.parseColors(config.getString("%s.Item.Name".formatted(name))))
                    .lore(PocketHorses.parseColors(config.getStringList("%s.Item.Lore".formatted(name))))
                    .modelData(config.getInt("%s.Item.Model-Data".formatted(name)))
                    .build();

            var nbt = new NBTItem(item);
            nbt.setString("care", name);
            item = nbt.getItem();

            return new Care(name, restoreHealth, item);
        } catch (NullPointerException e) {
            PocketHorses.getConsole().warning("%s is not a valid care!".formatted(name));
            return null;
        }
    }

    public static Care fromItem(ItemStack item) {
        var nbt = new NBTItem(item);
        if (!nbt.hasTag("care"))
            return null;

        return of(nbt.getString("care"));
    }

}
