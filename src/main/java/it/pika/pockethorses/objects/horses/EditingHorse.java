package it.pika.pockethorses.objects.horses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Horse;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EditingHorse {

    private String id;
    private String displayName;
    private org.bukkit.entity.Horse.Color color;
    private Horse.Style style;
    private double speed;
    private double jumpStrength;
    private int maxHealth;
    private boolean buyable;
    private double price;
    private boolean permission;
    private boolean storage;

}
