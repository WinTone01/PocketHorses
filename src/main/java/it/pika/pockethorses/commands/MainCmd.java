package it.pika.pockethorses.commands;

import it.pika.libs.command.SubCommand;
import it.pika.libs.config.Config;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.editor.EditingHorseMenu;
import it.pika.pockethorses.menu.editor.EditorMainMenu;
import it.pika.pockethorses.objects.Voucher;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.EditingHorse;
import it.pika.pockethorses.objects.items.Care;
import it.pika.pockethorses.objects.items.Supplement;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class MainCmd extends SubCommand {

    public MainCmd(JavaPlugin plugin, String label, List<String> aliases) {
        super(plugin, label, aliases);
    }

    @Override
    public void noArgs(CommandSender sender) {
        for (String s : PocketHorses.getMessagesFile().getStringList("help-message"))
            sender.sendMessage(PocketHorses.parseColors(s));
    }

    @SubCommandName("give")
    @SubCommandUsage("<player> <horse>")
    @SubCommandMinArgs(2)
    @SubCommandPermission(Perms.GIVE)
    public void give(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);
        var target = Validator.getOnlinePlayer(args[0]);
        var horse = PocketHorses.getLoadedHorse(args[1]);

        if (horse == null) {
            error(player, Messages.HORSE_NOT_EXISTING.get());
            return;
        }

        if (PocketHorses.has(player, horse.getId()) &&
                !PocketHorses.getConfigFile().getBoolean("Options.More-Than-Once-Same-Horse")) {
            error(player, Messages.ALREADY_OWNED.get());
            return;
        }

        PocketHorses.getStorage().giveHorse(target, horse);
        success(player, Messages.HORSE_GIVEN.get().formatted(target.getName()));
    }

    @SubCommandName("editor")
    @SubCommandPermission(Perms.EDITOR)
    public void editor(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        new EditorMainMenu().get().open(player);
    }

    @SubCommandName("create")
    @SubCommandUsage("<name>")
    @SubCommandMinArgs(1)
    @SubCommandPermission(Perms.CREATE)
    public void create(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);
        var name = args[0];

        if (PocketHorses.getLoadedHorse(name) != null) {
            error(player, Messages.HORSE_ALREADY_EXISTS.get());
            return;
        }

        var editingHorse = new EditingHorse();
        editingHorse.setId(name);

        new EditingHorseMenu(editingHorse, true).get().open(player);
    }

    @SubCommandName("list")
    @SubCommandPermission(Perms.LIST)
    public void list(CommandSender sender, String label, String[] args) {
        sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Horses-List.Header")));
        for (ConfigHorse horse : PocketHorses.getLoadedHorses())
            sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Horses-List.Horse"))
                    .replaceAll("%horse%", horse.getId()));
    }

    @SubCommandName("giveVoucher")
    @SubCommandMinArgs(2)
    @SubCommandUsage("<player> <voucher>")
    @SubCommandPermission(Perms.GIVE_VOUCHER)
    public void giveVoucher(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);
        var target = Validator.getOnlinePlayer(args[0]);

        var voucher = Voucher.of(args[1]);
        if (voucher == null) {
            error(player, Messages.VOUCHER_NOT_EXISTING.get());
            return;
        }

        target.getInventory().addItem(voucher.getItem());
        success(player, Messages.VOUCHER_GIVEN.get().formatted(target.getName()));
    }

    @SubCommandName("listVouchers")
    @SubCommandPermission(Perms.LIST_VOUCHERS)
    public void listVouchers(CommandSender sender, String label, String[] args) {
        sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Vouchers.List.Header")));
        for (String key : PocketHorses.getVouchersFile().getConfigurationSection("").getKeys(false))
            sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Vouchers.List.Voucher"))
                    .replaceAll("%voucher%", key));
    }

    @SubCommandName("reload")
    @SubCommandPermission(Perms.RELOAD)
    public void reload(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            PocketHorses.getConfigFile().reload();
            PocketHorses.getMessagesFile().reload();
            PocketHorses.getVouchersFile().reload();
            PocketHorses.getItemsFile().reload();

            PocketHorses.getLoadedHorses().clear();
            PocketHorses.getInstance().loadHorses();

            success(sender, Messages.RELOAD.get());
            return;
        }

        var fileName = args[0];
        var parts = fileName.split("/");

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            path.append(parts[i]);

            if (i != parts.length - 1)
                path.append(File.separator);
        }

        var file = new File(PocketHorses.getInstance().getDataFolder(), path.toString());
        if (!file.exists() || !file.getName().endsWith(".yml")) {
            error(sender, Messages.INVALID_FILE.get());
            return;
        }

        switch (file.getName()) {
            case "config.yml" -> PocketHorses.getConfigFile().reload();
            case "messages.yml" -> PocketHorses.getMessagesFile().reload();
            case "vouchers.yml" -> PocketHorses.getVouchersFile().reload();
            case "items.yml" -> PocketHorses.getItemsFile().reload();
            default -> {
                var config = new Config(PocketHorses.getInstance(), file);
                config.reload();
            }
        }

        success(sender, Messages.FILE_RELOADED.get().formatted(file.getName()));
    }

    @SubCommandName("help")
    @SubCommandPermission(Perms.HELP_MAIN)
    public void help(CommandSender sender, String label, String[] args) {
        for (String s : PocketHorses.getMessagesFile().getStringList("help-message"))
            sender.sendMessage(PocketHorses.parseColors(s));
    }

    @SubCommandName("giveItem")
    @SubCommandUsage("<type> <player> <item>")
    @SubCommandMinArgs(3)
    @SubCommandPermission(Perms.GIVE_ITEM)
    public void giveItem(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        var type = args[0];
        var target = Validator.getOnlinePlayer(args[1]);
        var name = args[2];

        if (type.equalsIgnoreCase("SUPPLEMENT")) {
            var supplement = Supplement.of(name);
            if (supplement == null) {
                error(player, Messages.ITEM_NOT_EXISTING.get());
                return;
            }

            target.getInventory().addItem(supplement.getItem());
            success(player, Messages.ITEM_GIVEN.get());
        } else if (type.equalsIgnoreCase("CARE")) {
            var care = Care.of(name);
            if (care == null) {
                error(player, Messages.ITEM_NOT_EXISTING.get());
                return;
            }

            target.getInventory().addItem(care.getItem());
            success(player, Messages.ITEM_GIVEN.get());
        } else {
            error(player, Messages.ITEM_TYPE_NOT_EXISTING.get());
        }
    }

    @SubCommandName("listItems")
    @SubCommandPermission(Perms.LIST_ITEMS)
    public void listItems(CommandSender sender, String label, String[] args) {
        sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Items-List.Header")));
        for (String key : PocketHorses.getItemsFile().getConfigurationSection("").getKeys(false))
            sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Items-List.Item"))
                    .replaceAll("%item%", key)
                    .replaceAll("%type%", PocketHorses.getItemsFile().getString("%s.Type".formatted(key))));
    }

}
