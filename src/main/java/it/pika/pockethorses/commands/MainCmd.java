package it.pika.pockethorses.commands;

import it.pika.libs.command.SubCommand;
import it.pika.libs.config.Config;
import it.pika.libs.reflection.Reflections;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.menu.editor.EditingHorseMenu;
import it.pika.pockethorses.menu.editor.EditorMainMenu;
import it.pika.pockethorses.objects.Voucher;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.EditingHorse;
import it.pika.pockethorses.objects.items.Care;
import it.pika.pockethorses.objects.items.Supplement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class MainCmd extends SubCommand {

    public MainCmd(JavaPlugin plugin, String label, List<String> aliases) {
        super(plugin, label, aliases);
    }

    @Override
    public void noArgs(CommandSender sender) {
        for (String s : Main.getLanguageManager().getMainHelp())
            sender.sendMessage(Main.parseColors(s));
    }

    @SubCommandName("give")
    @SubCommandUsage("<horse|voucher|item> <player> <name>")
    @SubCommandMinArgs(3)
    @SubCommandPermission(Perms.GIVE)
    public void give(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        if (args.length == 3) {
            var target = Validator.getOnlinePlayer(args[1]);

            if (args[0].equalsIgnoreCase("horse")) {
                var horse = Main.getLoadedHorse(args[2]);

                if (horse == null) {
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

                Main.getStorage().giveHorse(target, horse);
                success(player, Messages.HORSE_GIVEN.get().formatted(target.getName()));
            } else if (args[0].equalsIgnoreCase("voucher")) {
                var voucher = Voucher.of(args[2]);
                if (voucher == null) {
                    error(player, Messages.VOUCHER_NOT_EXISTING.get());
                    return;
                }

                target.getInventory().addItem(voucher.getItem());
                success(player, Messages.VOUCHER_GIVEN.get().formatted(target.getName()));
            } else {
                for (String s : Main.getLanguageManager().getMainHelp())
                    player.sendMessage(Main.parseColors(s));
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("item")) {
                var type = args[1];
                var target = Validator.getOnlinePlayer(args[2]);
                var name = args[3];

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
            } else {
                for (String s : Main.getLanguageManager().getMainHelp())
                    player.sendMessage(Main.parseColors(s));
            }
        } else {
            for (String s : Main.getLanguageManager().getMainHelp())
                player.sendMessage(Main.parseColors(s));
        }
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

        if (Main.getLoadedHorse(name) != null) {
            error(player, Messages.HORSE_ALREADY_EXISTS.get());
            return;
        }

        var editingHorse = new EditingHorse();
        editingHorse.setId(name);

        new EditingHorseMenu(editingHorse, true).get().open(player);
    }

    @SubCommandName("list")
    @SubCommandUsage("<horses|vouchers|items>")
    @SubCommandMinArgs(1)
    @SubCommandPermission(Perms.LIST)
    public void list(CommandSender sender, String label, String[] args) {
        if (args[0].equalsIgnoreCase("horses")) {
            sender.sendMessage(Main.parseColors(Main.getConfigFile().getString("Horses-List.Header")));
            for (ConfigHorse horse : Main.getLoadedHorses())
                sender.sendMessage(Main.parseColors(Main.getConfigFile().getString("Horses-List.Horse"))
                        .replaceAll("%horse%", horse.getId()));
        } else if (args[0].equalsIgnoreCase("vouchers")) {
            sender.sendMessage(Main.parseColors(Main.getConfigFile().getString("Vouchers.List.Header")));
            for (String key : Objects.requireNonNull(Main.getVouchersFile().getConfigurationSection("")).getKeys(false))
                sender.sendMessage(Main.parseColors(Main.getConfigFile().getString("Vouchers.List.Voucher"))
                        .replaceAll("%voucher%", key));
        } else if (args[0].equalsIgnoreCase("items")) {
            sender.sendMessage(Main.parseColors(Main.getConfigFile().getString("Items-List.Header")));
            for (String key : Objects.requireNonNull(Main.getItemsFile().getConfigurationSection("")).getKeys(false))
                sender.sendMessage(Main.parseColors(Main.getConfigFile().getString("Items-List.Item"))
                        .replaceAll("%item%", key)
                        .replaceAll("%type%",
                                Objects.requireNonNull(Main.getItemsFile().getString("%s.Type".formatted(key)))));
        } else {
            for (String s : Main.getLanguageManager().getMainHelp())
                sender.sendMessage(Main.parseColors(s));
        }
    }

    @SubCommandName("reload")
    @SubCommandPermission(Perms.RELOAD)
    public void reload(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Main.getConfigFile().reload();
            Main.getVouchersFile().reload();
            Main.getItemsFile().reload();

            Main.getLanguageManager().init();

            Main.getLoadedHorses().clear();
            Main.getInstance().loadHorses();

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

        var file = new File(Main.getInstance().getDataFolder(), path.toString());
        if (!file.exists() || !file.getName().endsWith(".yml")) {
            error(sender, Messages.INVALID_FILE.get());
            return;
        }

        switch (file.getName()) {
            case "config.yml" -> Main.getConfigFile().reload();
            case "vouchers.yml" -> Main.getVouchersFile().reload();
            case "items.yml" -> Main.getItemsFile().reload();
            default -> {
                var config = new Config(Main.getInstance(), file, false);
                config.reload();
            }
        }

        success(sender, Messages.FILE_RELOADED.get().formatted(file.getName()));
    }

    @SubCommandName("help")
    @SubCommandPermission(Perms.HELP_MAIN)
    public void help(CommandSender sender, String label, String[] args) {
        for (String s : Main.getLanguageManager().getMainHelp())
            sender.sendMessage(Main.parseColors(s));
    }

    @SubCommandName("debug")
    @SubCommandPermission(Perms.DEBUG)
    public void debug(CommandSender sender, String label, String[] args) {
        sender.sendMessage(Main.parseColors("&6------------------------------------"));
        sender.sendMessage(Main.parseColors("&eStorage type: &f%s".formatted(Main.getStorage().getType())));
        sender.sendMessage(Main.parseColors("&eEconomy type: &f%s")
                .formatted(Main.getEconomy() == null ? "//" : Main.getEconomy().getType()));
        sender.sendMessage(Main.parseColors("&ePlugin version: &f%s".formatted(Main.VERSION)));
        sender.sendMessage(Main.parseColors("&eServer version: &f%s".formatted(Reflections.getVersion())));
        sender.sendMessage(Main.parseColors("&eCache size: &f%s".formatted(Main.getCache().size())));
        sender.sendMessage(Main.parseColors("&eSpawned horses: &f%s".formatted(Main.getSpawnedHorses().size())));
        sender.sendMessage(Main.parseColors("&eShop enabled: &f%s".formatted(Main.isShopEnabled())));
        sender.sendMessage(Main.parseColors("&ePlaceholderAPI Hook: &f%s".formatted(Main.isPlaceholdersEnabled())));
        sender.sendMessage(Main.parseColors("&eWorldGuard Hook: &f%s".formatted(Main.isWorldGuardEnabled())));
        sender.sendMessage(Main.parseColors("&eModelEngine Hook: &f%s".formatted(Main.isModelEngineEnabled())));
        sender.sendMessage(Main.parseColors("&6------------------------------------"));
    }

    @SubCommandName("remove")
    @SubCommandUsage("<range>")
    @SubCommandMinArgs(1)
    @SubCommandPermission(Perms.REMOVE)
    public void remove(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        if (!isInt(args[0])) {
            error(player, Messages.INVALID_NUMBER.get());
            return;
        }

        var range = Integer.parseInt(args[0]);
        var entities = player.getNearbyEntities(range, range, range);

        int removed = 0;
        for (Entity entity : entities) {
            var horse = Main.getSpawnedHorse(entity);
            if (horse == null)
                continue;

            horse.remove(player);
            removed++;
        }

        success(player, Messages.REMOVED_HORSES.get().formatted(removed));
    }

    @SubCommandName("info")
    @SubCommandUsage("<horse>")
    @SubCommandMinArgs(1)
    @SubCommandPermission(Perms.HORSE_INFO)
    public void info(CommandSender sender, String label, String[] args) {
        var horse = ConfigHorse.of(args[0]);

        if (horse == null) {
            error(sender, Messages.HORSE_NOT_EXISTING.get());
            return;
        }

        sender.sendMessage(Main.parseColors("&6------------------------------------"));
        sender.sendMessage(Main.parseColors("&eName: &f%s".formatted(horse.getId())));
        sender.sendMessage(Main.parseColors("&eDisplay Name: &f%s".formatted(horse.getDisplayName())));
        sender.sendMessage(Main.parseColors("&eConfig File: &f%s".formatted(horse.getConfig().getFileName())));
        sender.sendMessage(Main.parseColors("&eColor: &f%s".formatted(horse.getColor().name())));
        sender.sendMessage(Main.parseColors("&eStyle: &f%s".formatted(horse.getStyle().name())));
        sender.sendMessage(Main.parseColors("&eSpeed: &f%s km/h".formatted(horse.getSpeed())));
        sender.sendMessage(Main.parseColors("&eJump Strength: &f%s".formatted(horse.getJumpStrength())));
        sender.sendMessage(Main.parseColors("&eMax Health: &f%s".formatted(horse.getMaxHealth())));
        sender.sendMessage(Main.parseColors("&eIs buyable: &f%s".formatted(horse.isBuyable())));
        sender.sendMessage(Main.parseColors("&ePrice: &f%s".formatted(horse.getPrice())));
        sender.sendMessage(Main.parseColors("&eRequires permission: &f%s".formatted(horse.isPermission())));
        sender.sendMessage(Main.parseColors("&eHas storage: &f%s".formatted(horse.isStorage())));
        sender.sendMessage(Main.parseColors("&eIs recyclable: &f%s".formatted(horse.isRecyclable())));
        sender.sendMessage(Main.parseColors("&eRecycle price: &f%s".formatted(horse.getRecyclePrice())));
        sender.sendMessage(Main.parseColors("&eModelEngine model: &f%s"
                .formatted(horse.getModel() == null ? "None" : horse.getModel())));
        sender.sendMessage(Main.parseColors("&6------------------------------------"));
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
