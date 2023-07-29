package it.pika.pockethorses.objects.horses;

import it.pika.pockethorses.PocketHorses;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

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

}
