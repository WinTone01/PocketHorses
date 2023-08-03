package it.pika.pockethorses.objects.items;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.PocketHorses;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter @Setter
public class Supplement {

    private String id;
    private double extraSpeed;
    private double extraJump;
    private long duration;
    private ItemStack item;

    public static Supplement of(String name) {
        var config = PocketHorses.getItemsFile();

        try {
            if (!Objects.requireNonNull(config.getString("%s.Type".formatted(name)))
                    .equalsIgnoreCase("SUPPLEMENT")) {
                PocketHorses.getConsole().warning("%s is not a valid supplement!".formatted(name));
                return null;
            }

            var extraSpeed = config.getDouble("%s.Extra-Speed".formatted(name));
            var extraJump = config.getDouble("%s.Extra-Jump".formatted(name));
            var duration = config.getLong("%s.Duration".formatted(name));

            var item = new ItemBuilder()
                    .material(Material.valueOf(config.getString("%s.Item.Material".formatted(name))))
                    .name(PocketHorses.parseColors(config.getString("%s.Item.Name".formatted(name))))
                    .lore(PocketHorses.parseColors(config.getStringList("%s.Item.Lore".formatted(name))))
                    .modelData(config.getInt("%s.Item.Model-Data".formatted(name)))
                    .build();

            var nbt = new NBTItem(item);
            nbt.setString("supplement", name);
            item = nbt.getItem();

            return new Supplement(name, extraSpeed, extraJump, duration, item);
        } catch (NullPointerException e) {
            PocketHorses.getConsole().warning("%s is not a valid supplement!".formatted(name));
            return null;
        }
    }

    public static Supplement fromItem(ItemStack item) {
        var nbt = new NBTItem(item);
        if (!nbt.hasTag("supplement"))
            return null;

        return of(nbt.getString("supplement"));
    }

}
