package it.pika.pockethorses.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import it.pika.libs.chat.Chat;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.MyHorsesMenu;
import it.pika.pockethorses.menu.ShopMenu;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class PlayerCommands {

    public CommandAPICommand get() {
        var config = Main.getConfigFile();

        return new CommandAPICommand("horses")
                .withAliases(config.getStringList("Commands.Horses.Aliases").toArray(new String[0]))
                .withSubcommands(shop(), recall(), help(), buy(), autoRemove())
                .executesPlayer((player, args) -> {
                    if (Main.getConfigFile().getBoolean("Horses-GUI.Use-Permission") &&
                            !player.hasPermission(Perms.HORSES_GUI)) {
                        error(player, Messages.NO_PERMISSION.get());
                        return;
                    }

                    new MyHorsesMenu().get().open(player);
                });
    }

    private CommandAPICommand shop() {
        return new CommandAPICommand("shop")
                .executesPlayer((player, args) -> {
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
                });
    }

    private CommandAPICommand recall() {
        return new CommandAPICommand("recall")
                .executesPlayer((player, args) -> {
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
                });
    }

    private CommandAPICommand help() {
        return new CommandAPICommand("help")
                .withPermission(Perms.HELP_HORSES)
                .executes((sender, args) -> {
                    sendHelp(sender);
                });
    }

    private void sendHelp(CommandSender sender) {
        for (String s : Main.getLanguageManager().getHorsesHelp())
            sender.sendMessage(Chat.parseColors(s));
    }

    private CommandAPICommand buy() {
        return new CommandAPICommand("buy")
                .withPermission(Perms.BUY)
                .withArguments(new StringArgument("horse")
                        .replaceSuggestions(ArgumentSuggestions.strings(Main.getHorseNames())))
                .executesPlayer((player, args) -> {
                    var horse = ConfigHorse.of((String) args.get("horse"));

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
                });
    }

    private CommandAPICommand autoRemove() {
        return new CommandAPICommand("autoRemove")
                .withPermission(Perms.AUTO_REMOVE)
                .executesPlayer((player, args) -> {
                    if (Main.getAutoRemove().contains(player.getName())) {
                        Main.getAutoRemove().remove(player.getName());
                        success(player, Messages.AUTO_REMOVE_DISABLED.get());
                    } else {
                        Main.getAutoRemove().add(player.getName());
                        success(player, Messages.AUTO_REMOVE_ENABLED.get());
                    }
                });
    }

}
