package it.pika.pockethorses.commands;

import it.pika.libs.command.SubCommand;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.MyHorsesMenu;
import it.pika.pockethorses.menu.ShopMenu;
import it.pika.pockethorses.objects.SpawnedHorse;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class HorsesCmd extends SubCommand {

    public HorsesCmd(JavaPlugin plugin, String label, String... aliases) {
        super(plugin, label, aliases);
    }

    @Override
    public void noArgs(CommandSender sender) {
        var player = Validator.getPlayerSender(sender);

        if (PocketHorses.getConfigFile().getBoolean("Horses-GUI.Use-Permission") &&
                !player.hasPermission(Perms.HORSES_GUI)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        new MyHorsesMenu().get().open(player);
    }

    @SubCommandName("shop")
    public void shop(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        if (!PocketHorses.isShopEnabled()) {
            error(player, Messages.SHOP_NOT_ENABLED.get());
            return;
        }

        if (PocketHorses.getConfigFile().getBoolean("Shop-GUI.Use-Permission")
                && !player.hasPermission(Perms.SHOP_GUI)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        new ShopMenu().get().open(player);
    }

    @SubCommandName("recall")
    public void recall(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        if (!PocketHorses.getSpawnedHorses().containsKey(player.getName())) {
            error(player, Messages.NO_HORSES_SPAWNED.get());
            return;
        }

        for (SpawnedHorse horse : PocketHorses.getSpawnedHorses().get(player.getName()))
            horse.getEntity().teleport(player.getLocation());

        success(player, Messages.HORSES_RECALLED.get());
    }


}
