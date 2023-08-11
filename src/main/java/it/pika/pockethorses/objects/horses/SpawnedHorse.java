package it.pika.pockethorses.objects.horses;

import it.pika.pockethorses.Main;
import it.pika.pockethorses.api.events.HorseDespawnEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class SpawnedHorse extends Horse {

    private Entity entity;
    private double speed;
    private boolean sit;
    private boolean autoRecall;
    private Object modeledEntity;

    public SpawnedHorse(UUID uuid, String name, String owner, String customName, String storedItems,
                        Entity entity, double speed, boolean sit, boolean autoRecall, Object modeledEntity) {
        super(uuid, name, owner, customName, storedItems);

        this.entity = entity;
        this.speed = speed;
        this.sit = sit;
        this.autoRecall = autoRecall;
        this.modeledEntity = modeledEntity;
    }

    public void remove(Player player) {
        var event = new HorseDespawnEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        if (Main.getSpawnedHorses().get(player.getName()).size() == 1)
            Main.getSpawnedHorses().remove(player.getName());
        else
            Main.getSpawnedHorses().get(player.getName()).remove(this);

        entity.remove();
        if (Main.getModelEngineHook() != null)
            Main.getModelEngineHook().remove(this);
    }

}
