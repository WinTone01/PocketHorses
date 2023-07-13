package it.pika.pockethorses.objects;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.PocketHorses;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter @Setter
public class Voucher {

    private String name;
    private String displayName;
    private Material material;
    private List<String> lore;
    private boolean permission;
    private List<ConfigHorse> rewards;

    public static Voucher of(String name) {
        var config = PocketHorses.getVouchersFile();

        try {
            var displayName = config.getString("%s.DisplayName".formatted(name));
            var material = Material.valueOf(config.getString("%s.Material".formatted(name)));

            var permission = config.getBoolean("%s.Permission".formatted(name));

            List<ConfigHorse> rewards = Lists.newArrayList();
            List<String> rewardNames = Lists.newArrayList();
            for (String s : config.getStringList("%s.Rewards".formatted(name))) {
                rewards.add(ConfigHorse.of(s));
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
                .name(PocketHorses.parseColors(displayName))
                .lore(PocketHorses.parseColors(lore))
                .build();

        var nbt = new NBTItem(item);
        nbt.setString("voucher", name);

        return nbt.getItem();
    }

}
