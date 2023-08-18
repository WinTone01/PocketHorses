package it.pika.pockethorses.listeners;

import it.pika.libs.chat.Chat;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.objects.horses.SpawnedHorse;
import it.pika.pockethorses.utils.UpdateChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        if (!player.isOp())
            return;

        new UpdateChecker(Main.getInstance(), 111158).getVersion(version -> {
            if (!version.equals(Main.VERSION))
                player.sendMessage(Chat.parseColors(Messages.NEW_UPDATE.get()));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();

        var horses = Main.getSpawnedHorses().get(player.getName());
        if (horses == null)
            return;

        for (SpawnedHorse horse : horses)
            horse.remove(player);
    }

}
