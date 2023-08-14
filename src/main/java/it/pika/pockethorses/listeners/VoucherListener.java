package it.pika.pockethorses.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.Voucher;
import it.pika.pockethorses.utils.xseries.Titles;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;
import java.util.Random;

import static it.pika.libs.chat.Chat.error;

public class VoucherListener implements Listener {

    @EventHandler
    public void onClickVoucher(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        var item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir())
            return;

        var nbt = new NBTItem(item);
        if (!nbt.hasTag("voucher"))
            return;

        var voucher = Voucher.of(nbt.getString("voucher"));
        if (voucher == null)
            return;

        event.setCancelled(true);

        if (voucher.getRewards().isEmpty())
            return;

        if (voucher.isPermission() && !player.hasPermission(Perms.getVoucher(voucher.getName())))
            return;

        if (Main.notRespectsLimit(player)) {
            error(player, Messages.LIMIT_REACHED.get());
            return;
        }

        item.setAmount(item.getAmount() - 1);
        Titles.sendTitle(player, 0, 20 * Main.getConfigFile().getInt("Vouchers.Opening.Delay"), 0,
                Main.parseColors(Main.getConfigFile().getString("Vouchers.Opening.Title")),
                Main.parseColors(Main.getConfigFile().getString("Vouchers.Opening.Sub-Title")));
        player.playSound(player.getLocation(),
                Sound.valueOf(Main.getConfigFile().getString("Vouchers.Opening.Sound")), 1F, 1F);

        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            var reward = voucher.getRewards().get(new Random().nextInt(voucher.getRewards().size()));

            Main.getStorage().giveHorse(player, reward);
            Titles.sendTitle(player, 0, 40, 0, Main.parseColors(Main.getConfigFile().getString("Vouchers.Reward.Title")),
                    Main.parseColors(Objects.requireNonNull(Main.getConfigFile()
                                    .getString("Vouchers.Reward.Sub-Title"))
                            .replaceAll("%reward%", reward.getDisplayName())));
            player.playSound(player.getLocation(),
                    Sound.valueOf(Main.getConfigFile().getString("Vouchers.Reward.Sound")), 1F, 1F);
        }, 20L * Main.getConfigFile().getInt("Vouchers.Opening.Delay"));
    }

}
