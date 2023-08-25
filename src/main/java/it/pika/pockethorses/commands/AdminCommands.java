package it.pika.pockethorses.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import it.pika.libs.chat.Chat;
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
import org.bukkit.entity.Player;

import java.util.Objects;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class AdminCommands {

    public CommandAPICommand get() {
        var config = Main.getConfigFile();

        return new CommandAPICommand("pockethorses")
                .withAliases(config.getStringList("Commands.PocketHorses.Aliases").toArray(new String[0]))
                .withSubcommands(give(), editor(), create(), list(), reload(), help(), debug(), remove(), info())
                .executes((sender, args) -> {
                    sendHelp(sender);
                });
    }

    private CommandAPICommand give() {
        return new CommandAPICommand("give")
                .withPermission(Perms.GIVE)
                .withSubcommands(giveHorse(), giveVoucher(), giveItem())
                .executes((sender, args) -> {
                    sendHelp(sender);
                });
    }

    private void sendHelp(CommandSender sender) {
        for (String s : Main.getLanguageManager().getMainHelp())
            sender.sendMessage(Chat.parseColors(s));
    }

    private CommandAPICommand giveHorse() {
        return new CommandAPICommand("horse")
                .withPermission(Perms.GIVE)
                .withArguments(new PlayerArgument("player"),
                        new StringArgument("name")
                                .replaceSuggestions(ArgumentSuggestions.strings(Main.getHorseNames())))
                .executes((sender, args) -> {
                    var target = (Player) args.get("player");
                    var horse = Main.getLoadedHorse((String) args.get("name"));

                    if (target == null)
                        return;

                    if (horse == null) {
                        error(sender, Messages.HORSE_NOT_EXISTING.get());
                        return;
                    }

                    if (Main.has(target, horse.getId()) &&
                            !Main.getConfigFile().getBoolean("Options.More-Than-Once-Same-Horse")) {
                        error(sender, Messages.ALREADY_OWNED.get());
                        return;
                    }

                    if (Main.notRespectsLimit(target)) {
                        error(sender, Messages.LIMIT_REACHED.get());
                        return;
                    }

                    Main.getStorage().giveHorse(target, horse);
                    success(sender, Messages.HORSE_GIVEN.get().formatted(target.getName()));
                });
    }

    private CommandAPICommand giveVoucher() {
        return new CommandAPICommand("voucher")
                .withPermission(Perms.GIVE)
                .withArguments(new PlayerArgument("player"),
                        new StringArgument("name")
                                .replaceSuggestions(ArgumentSuggestions.strings(Main.getVoucherNames())))
                .executes((sender, args) -> {
                    var target = (Player) args.get("player");
                    var voucher = Voucher.of((String) args.get("name"));

                    if (target == null)
                        return;

                    if (voucher == null) {
                        error(sender, Messages.VOUCHER_NOT_EXISTING.get());
                        return;
                    }

                    target.getInventory().addItem(voucher.getItem());
                    success(sender, Messages.VOUCHER_GIVEN.get().formatted(target.getName()));
                });
    }

    private CommandAPICommand giveItem() {
        return new CommandAPICommand("item")
                .withPermission(Perms.GIVE)
                .withArguments(new StringArgument("itemType")
                                .replaceSuggestions(ArgumentSuggestions.strings("SUPPLEMENT", "CARE")),
                        new PlayerArgument("player"),
                        new StringArgument("name")
                                .replaceSuggestions(ArgumentSuggestions.strings(Main.getItemNames())))
                .executes((sender, args) -> {
                    var itemType = (String) args.get("itemType");
                    var target = (Player) args.get("player");
                    var name = (String) args.get("name");

                    if (itemType == null || target == null || name == null)
                        return;

                    if (itemType.equalsIgnoreCase("SUPPLEMENT")) {
                        var supplement = Supplement.of(name);
                        if (supplement == null) {
                            error(sender, Messages.ITEM_NOT_EXISTING.get());
                            return;
                        }

                        target.getInventory().addItem(supplement.getItem());
                        success(sender, Messages.ITEM_GIVEN.get());
                    } else if (itemType.equalsIgnoreCase("CARE")) {
                        var care = Care.of(name);
                        if (care == null) {
                            error(sender, Messages.ITEM_NOT_EXISTING.get());
                            return;
                        }

                        target.getInventory().addItem(care.getItem());
                        success(sender, Messages.ITEM_GIVEN.get());
                    } else {
                        error(sender, Messages.ITEM_TYPE_NOT_EXISTING.get());
                    }
                });
    }

    private CommandAPICommand editor() {
        return new CommandAPICommand("editor")
                .withPermission(Perms.EDITOR)
                .executesPlayer((player, args) -> {
                    new EditorMainMenu().get().open(player);
                });
    }

    private CommandAPICommand create() {
        return new CommandAPICommand("create")
                .withPermission(Perms.CREATE)
                .withArguments(new StringArgument("name"))
                .executesPlayer((player, args) -> {
                    var name = (String) args.get("name");

                    if (Main.getLoadedHorse(name) != null) {
                        error(player, Messages.HORSE_ALREADY_EXISTS.get());
                        return;
                    }

                    var editingHorse = new EditingHorse();
                    editingHorse.setId(name);

                    new EditingHorseMenu(editingHorse, true).get().open(player);
                });
    }

    private CommandAPICommand list() {
        return new CommandAPICommand("list")
                .withPermission(Perms.LIST)
                .withSubcommands(listHorses(), listVouchers(), listItems())
                .executes((sender, args) -> {
                    sendHelp(sender);
                });
    }

    private CommandAPICommand listHorses() {
        return new CommandAPICommand("horses")
                .withPermission(Perms.LIST)
                .executes((sender, args) -> {
                    sender.sendMessage(Chat.parseColors(Main.getConfigFile().getString("Horses-List.Header")));
                    for (ConfigHorse horse : Main.getLoadedHorses())
                        sender.sendMessage(Chat.parseColors(Main.getConfigFile().getString("Horses-List.Horse"))
                                .replaceAll("%horse%", horse.getId()));
                });
    }

    private CommandAPICommand listVouchers() {
        return new CommandAPICommand("vouchers")
                .withPermission(Perms.LIST)
                .executes((sender, args) -> {
                    sender.sendMessage(Chat.parseColors(Main.getConfigFile().getString("Vouchers.List.Header")));
                    for (String key : Objects.requireNonNull(Main.getVouchersFile().getConfigurationSection("")).getKeys(false))
                        sender.sendMessage(Chat.parseColors(Main.getConfigFile().getString("Vouchers.List.Voucher"))
                                .replaceAll("%voucher%", key));
                });
    }

    private CommandAPICommand listItems() {
        return new CommandAPICommand("items")
                .withPermission(Perms.LIST)
                .executes((sender, args) -> {
                    sender.sendMessage(Chat.parseColors(Main.getConfigFile().getString("Items-List.Header")));
                    for (String key : Objects.requireNonNull(Main.getItemsFile().getConfigurationSection("")).getKeys(false))
                        sender.sendMessage(Chat.parseColors(Main.getConfigFile().getString("Items-List.Item"))
                                .replaceAll("%item%", key)
                                .replaceAll("%type%",
                                        Objects.requireNonNull(Main.getItemsFile().getString("%s.Type".formatted(key)))));
                });
    }

    private CommandAPICommand reload() {
        return new CommandAPICommand("reload")
                .withPermission(Perms.RELOAD)
                .executes((sender, args) -> {
                    Main.getConfigFile().reload();
                    Main.getVouchersFile().reload();
                    Main.getItemsFile().reload();

                    Main.getLanguageManager().init();

                    Main.getLoadedHorses().clear();
                    Main.getInstance().loadHorses();

                    success(sender, Messages.RELOAD.get());
                });
    }

    private CommandAPICommand help() {
        return new CommandAPICommand("help")
                .withPermission(Perms.HELP_MAIN)
                .executes((sender, args) -> {
                    sendHelp(sender);
                });
    }

    private CommandAPICommand debug() {
        return new CommandAPICommand("debug")
                .withPermission(Perms.DEBUG)
                .executes((sender, args) -> {
                    sender.sendMessage(Chat.parseColors("&6------------------------------------"));
                    sender.sendMessage(Chat.parseColors("&eStorage type: &f%s".formatted(Main.getStorage().getType())));
                    sender.sendMessage(Chat.parseColors("&eEconomy type: &f%s")
                            .formatted(Main.getEconomy() == null ? "//" : Main.getEconomy().getType()));
                    sender.sendMessage(Chat.parseColors("&ePlugin version: &f%s".formatted(Main.VERSION)));
                    sender.sendMessage(Chat.parseColors("&eServer version: &f%s".formatted(Reflections.getVersion())));
                    sender.sendMessage(Chat.parseColors("&eCache size: &f%s".formatted(Main.getCache().size())));
                    sender.sendMessage(Chat.parseColors("&eSpawned horses: &f%s".formatted(Main.getSpawnedHorses().size())));
                    sender.sendMessage(Chat.parseColors("&eShop enabled: &f%s".formatted(Main.isShopEnabled())));
                    sender.sendMessage(Chat.parseColors("&ePlaceholderAPI Hook: &f%s".formatted(Main.isPlaceholdersEnabled())));
                    sender.sendMessage(Chat.parseColors("&eWorldGuard Hook: &f%s".formatted(Main.isWorldGuardEnabled())));
                    sender.sendMessage(Chat.parseColors("&eModelEngine Hook: &f%s".formatted(Main.isModelEngineEnabled())));
                    sender.sendMessage(Chat.parseColors("&6------------------------------------"));
                });
    }

    private CommandAPICommand remove() {
        return new CommandAPICommand("remove")
                .withPermission(Perms.REMOVE)
                .withArguments(new IntegerArgument("range"))
                .executesPlayer((player, args) -> {
                    var range = (Integer) args.get("range");

                    if (range == null)
                        return;

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
                });
    }

    private CommandAPICommand info() {
        return new CommandAPICommand("info")
                .withPermission(Perms.HORSE_INFO)
                .withArguments(new StringArgument("horse")
                        .replaceSuggestions(ArgumentSuggestions.strings(Main.getHorseNames())))
                .executes((sender, args) -> {
                    var horse = ConfigHorse.of((String) args.get("horse"));

                    if (horse == null) {
                        error(sender, Messages.HORSE_NOT_EXISTING.get());
                        return;
                    }

                    sender.sendMessage(Chat.parseColors("&6------------------------------------"));
                    sender.sendMessage(Chat.parseColors("&eName: &f%s".formatted(horse.getId())));
                    sender.sendMessage(Chat.parseColors("&eDisplay Name: &f%s".formatted(horse.getDisplayName())));
                    sender.sendMessage(Chat.parseColors("&eConfig File: &f%s".formatted(horse.getConfig().getFileName())));
                    sender.sendMessage(Chat.parseColors("&eColor: &f%s".formatted(horse.getColor().name())));
                    sender.sendMessage(Chat.parseColors("&eStyle: &f%s".formatted(horse.getStyle().name())));
                    sender.sendMessage(Chat.parseColors("&eSpeed: &f%s km/h".formatted(horse.getSpeed())));
                    sender.sendMessage(Chat.parseColors("&eJump Strength: &f%s".formatted(horse.getJumpStrength())));
                    sender.sendMessage(Chat.parseColors("&eMax Health: &f%s".formatted(horse.getMaxHealth())));
                    sender.sendMessage(Chat.parseColors("&eIs buyable: &f%s".formatted(horse.isBuyable())));
                    sender.sendMessage(Chat.parseColors("&ePrice: &f%s".formatted(horse.getPrice())));
                    sender.sendMessage(Chat.parseColors("&eRequires permission: &f%s".formatted(horse.isPermission())));
                    sender.sendMessage(Chat.parseColors("&eHas storage: &f%s".formatted(horse.isStorage())));
                    sender.sendMessage(Chat.parseColors("&eIs recyclable: &f%s".formatted(horse.isRecyclable())));
                    sender.sendMessage(Chat.parseColors("&eRecycle price: &f%s".formatted(horse.getRecyclePrice())));
                    sender.sendMessage(Chat.parseColors("&eModelEngine model: &f%s"
                            .formatted(horse.getModel() == null ? "None" : horse.getModel())));
                    sender.sendMessage(Chat.parseColors("&eCooldown: &f%s".formatted(horse.getCooldown())));
                    sender.sendMessage(Chat.parseColors("&eConsole commands on mount: %s"
                            .formatted(horse.getConsoleCommandsOnMount().isEmpty() ? "&fNone" : "")));
                    if (!horse.getConsoleCommandsOnMount().isEmpty())
                        for (String s : horse.getConsoleCommandsOnMount())
                            sender.sendMessage(Chat.parseColors("&8- &f%s".formatted(s)));
                    sender.sendMessage(Chat.parseColors("&eConsole commands on dismount: %s"
                            .formatted(horse.getConsoleCommandsOnDismount().isEmpty() ? "&fNone" : "")));
                    if (!horse.getConsoleCommandsOnDismount().isEmpty())
                        for (String s : horse.getConsoleCommandsOnDismount())
                            sender.sendMessage(Chat.parseColors("&8- &f%s".formatted(s)));
                    sender.sendMessage(Chat.parseColors("&6------------------------------------"));
                });
    }

}
