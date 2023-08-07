package it.pika.pockethorses.objects;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class Voucher {

    private String name;
    private String displayName;
    private Material material;
    private List<String> lore;
    private boolean permission;
    private List<ConfigHorse> rewards;

    public static Voucher of(String name) {
        var config = Main.getVouchersFile();

        try {
            var displayName = config.getString("%s.DisplayName".formatted(name));
            var material = Material.valueOf(config.getString("%s.Material".formatted(name)));

            var permission = config.getBoolean("%s.Permission".formatted(name));

            List<ConfigHorse> rewards = Lists.newArrayList();
            List<String> rewardNames = Lists.newArrayList();
            for (String s : config.getStringList("%s.Rewards".formatted(name))) {
                var configHorse = ConfigHorse.of(s);
                if (configHorse == null)
                    continue;

                rewards.add(configHorse);
                rewardNames.add(s);
            }

            List<String> lore = Lists.newArrayList();
            for (String loreLine : config.getStringList("%s.Lore".formatted(name)))
                lore.add(loreLine.replaceAll("%rewards%", rewardNames.toString()
                        .replace("[", "").replace("]", "")));

            return new Voucher(name, displayName, material, lore, permission, rewards);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public ItemStack getItem() {
        var item = new ItemBuilder()
                .material(material)
                .name(Main.parseColors(displayName))
                .lore(Main.parseColors(lore))
                .build();

        var nbt = new NBTItem(item);
        nbt.setString("voucher", name);

        return nbt.getItem();
    }

}
