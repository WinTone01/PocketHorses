package it.pika.pockethorses.objects.horses;

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

    public SpawnedHorse(UUID uuid, String name, String owner, String customName, String storedItems,
                        Entity entity, double speed, boolean sit, boolean autoRecall) {
        super(uuid, name, owner, customName, storedItems);

        this.entity = entity;
        this.speed = speed;
        this.sit = sit;
        this.autoRecall = autoRecall;
    }

}
