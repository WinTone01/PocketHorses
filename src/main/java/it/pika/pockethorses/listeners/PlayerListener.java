package it.pika.pockethorses.listeners;

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
                player.sendMessage(Main.parseColors(Messages.NEW_UPDATE.get()));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();

        var horses = Main.getSpawnedHorses().get(player.getName());
        if (horses == null)
            return;

        for (SpawnedHorse horse : horses) {
            if (horses.size() == 1)
                Main.getSpawnedHorses().remove(player.getName());
            else
                Main.getSpawnedHorses().get(player.getName()).remove(horse);

            horse.getEntity().remove();
            if (Main.getModelEngineHook() != null)
                Main.getModelEngineHook().remove(horse);
        }
    }

}
