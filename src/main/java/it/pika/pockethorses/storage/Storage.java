package it.pika.pockethorses.storage;

import it.pika.pockethorses.enums.StorageType;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Storage {

    public abstract void init();

    public abstract void close();

    public abstract void giveHorse(Player player, ConfigHorse horse);

    public abstract void takeHorse(Player player, Horse horse);
    public abstract void setCustomName(Horse horse, String name);

    public abstract void setStoredItems(Horse horse, ItemStack[] items);
    public abstract StorageType getType();

}
