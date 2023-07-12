package it.pika.pockethorses.listeners;

import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.Messages;
import it.pika.pockethorses.utils.UpdateChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        if (!player.isOp())
            return;

        new UpdateChecker(PocketHorses.getInstance(), 111158).getVersion(version -> {
            if (!version.equals(PocketHorses.VERSION))
                player.sendMessage(PocketHorses.parseColors(Messages.NEW_UPDATE.get()));
        });
    }

}
