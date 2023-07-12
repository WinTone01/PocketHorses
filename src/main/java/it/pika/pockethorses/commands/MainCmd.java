package it.pika.pockethorses.commands;

import it.pika.libs.command.SubCommand;
import it.pika.pockethorses.Perms;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.ConfigHorse;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class MainCmd extends SubCommand {

    public MainCmd(JavaPlugin plugin, String label, String... aliases) {
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

    @SubCommandName("list")
    @SubCommandPermission(Perms.LIST)
    public void list(CommandSender sender, String label, String[] args) {
        sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Horses-List.Header")));
        for (ConfigHorse horse : PocketHorses.getLoadedHorses())
            sender.sendMessage(PocketHorses.parseColors(PocketHorses.getConfigFile().getString("Horses-List.Horse"))
                    .replaceAll("%horse%", horse.getId()));
    }

    @SubCommandName("reload")
    @SubCommandPermission(Perms.RELOAD)
    public void reload(CommandSender sender, String label, String[] args) {
        PocketHorses.getConfigFile().reload();
        PocketHorses.getMessagesFile().reload();
        PocketHorses.getHorsesFile().reload();

        PocketHorses.getLoadedHorses().clear();
        PocketHorses.getInstance().loadHorses();

        success(sender, Messages.RELOAD.get());
    }

}
