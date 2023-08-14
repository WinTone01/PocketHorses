package it.pika.pockethorses.commands;

import it.pika.libs.command.SubCommand;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.MyHorsesMenu;
import it.pika.pockethorses.menu.ShopMenu;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class HorsesCmd extends SubCommand {

    public HorsesCmd(JavaPlugin plugin, String label, List<String> aliases) {
        super(plugin, label, aliases);
    }

    @Override
    public void noArgs(CommandSender sender) {
        var player = Validator.getPlayerSender(sender);

        if (Main.getConfigFile().getBoolean("Horses-GUI.Use-Permission") &&
                !player.hasPermission(Perms.HORSES_GUI)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        new MyHorsesMenu().get().open(player);
    }

    @SubCommandName("shop")
    public void shop(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        if (!Main.isShopEnabled()) {
            error(player, Messages.SHOP_NOT_ENABLED.get());
            return;
        }

        if (Main.getConfigFile().getBoolean("Shop-GUI.Use-Permission")
                && !player.hasPermission(Perms.SHOP_GUI)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        new ShopMenu().get().open(player);
    }

    @SubCommandName("recall")
    public void recall(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        if (Main.getConfigFile().getBoolean("Options.Use-Recall-Permission")
                && !player.hasPermission(Perms.RECALL)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        if (!Main.getSpawnedHorses().containsKey(player.getName())) {
            error(player, Messages.NO_HORSES_SPAWNED.get());
            return;
        }

        for (SpawnedHorse horse : Main.getSpawnedHorses().get(player.getName()))
            Main.teleport(horse.getEntity(), player.getLocation());

        success(player, Messages.HORSES_RECALLED.get());
    }

    @SubCommandName("help")
    @SubCommandPermission(Perms.HELP_HORSES)
    public void help(CommandSender sender, String label, String[] args) {
        for (String s : Main.getLanguageManager().getHorsesHelp())
            sender.sendMessage(Main.parseColors(s));
    }

    @SubCommandName("buy")
    @SubCommandUsage("<horse>")
    @SubCommandMinArgs(1)
    @SubCommandPermission(Perms.BUY)
    public void buy(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);
        var horse = ConfigHorse.of(args[0]);

        if (horse == null || !horse.isBuyable()) {
            error(player, Messages.HORSE_NOT_EXISTING.get());
            return;
        }

        if (Main.has(player, horse.getId()) &&
                !Main.getConfigFile().getBoolean("Options.More-Than-Once-Same-Horse")) {
            error(player, Messages.ALREADY_OWNED.get());
            return;
        }

        if (Main.notRespectsLimit(player)) {
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
    }

    @SubCommandName("autoRemove")
    @SubCommandPermission(Perms.AUTO_REMOVE)
    public void autoRemove(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        if (Main.getAutoRemove().contains(player.getName())) {
            Main.getAutoRemove().remove(player.getName());
            success(player, Messages.AUTO_REMOVE_DISABLED.get());
        } else {
            Main.getAutoRemove().add(player.getName());
            success(player, Messages.AUTO_REMOVE_ENABLED.get());
        }
    }

}
