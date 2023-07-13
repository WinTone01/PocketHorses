package it.pika.pockethorses.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.objects.Voucher;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Random;

public class VoucherListener implements Listener {

    @EventHandler
    public void onClickVoucher(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (!event.getAction().isRightClick())
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

        item.setAmount(item.getAmount() - 1);
        player.sendTitle(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Vouchers.Opening.Title")),
                PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Vouchers.Opening.Sub-Title")),
                0, 20 * PocketHorses.getConfigFile().getInt("Vouchers.Opening.Delay"), 0);
        player.playSound(player.getLocation(),
                Sound.valueOf(PocketHorses.getConfigFile().getString("Vouchers.Opening.Sound")), 1F, 1F);

        Bukkit.getScheduler().runTaskLaterAsynchronously(PocketHorses.getInstance(), () -> {
            var reward = voucher.getRewards().get(new Random().nextInt(voucher.getRewards().size()));

            PocketHorses.getStorage().giveHorse(player, reward);
            player.sendTitle(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Vouchers.Reward.Title")),
                    PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Vouchers.Reward.Sub-Title")
                            .replaceAll("%reward%", reward.getDisplayName())), 0, 40, 0);
            player.playSound(player.getLocation(),
                    Sound.valueOf(PocketHorses.getConfigFile().getString("Vouchers.Reward.Sound")), 1F, 1F);
        }, 20L * PocketHorses.getConfigFile().getInt("Vouchers.Opening.Delay"));
    }

}
