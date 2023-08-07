package it.pika.pockethorses.menu;

import com.google.common.collect.Lists;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import it.pika.libs.item.ItemBuilder;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class ShopMenu implements InventoryProvider {

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(Main.getConfigFile().getString("Shop-GUI.Title"))
                .size(Main.getConfigFile().getInt("Shop-GUI.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        for (ConfigHorse horse : Main.getLoadedHorses()) {
            if (horse == null || !horse.isBuyable())
                continue;

            contents.add(ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("Shop-GUI.Horse-Item.Material")))
                    .name(format(Main.getConfigFile().getString("Shop-GUI.Horse-Item.Name"), horse))
                    .lore(format(Main.getConfigFile().getStringList("Shop-GUI.Horse-Item.Lore"), horse))
                    .build(), e -> {
                player.closeInventory();

                if (Main.has(player, horse.getId()) &&
                        !Main.getConfigFile().getBoolean("Options.More-Than-Once-Same-Horse")) {
                    error(player, Messages.ALREADY_OWNED.get());
                    return;
                }

                if (!Main.respectsLimit(player)) {
                    error(player, Messages.LIMIT_REACHED.get());
                    return;
                }

                if (!Main.getEconomy().has(player, horse.getPrice())) {
                    error(player, Messages.NOT_ENOUGH_MONEY.get());
                    return;
                }

                Main.getEconomy().withdraw(player, horse.getPrice());
                Main.getStorage().giveHorse(player, horse);

                success(player, Messages.PURCHASE_COMPLETED.get());
                if (Main.getConfigFile().getBoolean("Options.Play-Sound-When-Buy"))
                    player.playSound(player.getLocation(), Sound.valueOf(Main.
                            getConfigFile().getString("Shop-GUI.Buy-Sound")), 1F, 1F);
            }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private String format(String s, ConfigHorse horse) {
        return Main.parseColors(s)
                .replaceAll("%displayName%", horse.getDisplayName())
                .replaceAll("%speed%", String.valueOf(horse.getSpeed()))
                .replaceAll("%price%", String.valueOf(horse.getPrice()))
                .replaceAll("%jumpStrength%", String.valueOf(horse.getJumpStrength()));
    }

    private List<String> format(List<String> list, ConfigHorse horse) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(format(s, horse));

        return newList;
    }

}
