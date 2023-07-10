package it.pika.pockethorses.objects;

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

    public SpawnedHorse(UUID uuid, String name, String owner, String customName, String storedItems,
                        Entity entity, double speed, boolean sit) {
        super(uuid, name, owner, customName, storedItems);

        this.entity = entity;
        this.speed = speed;
        this.sit = sit;
    }

}
