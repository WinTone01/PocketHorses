package it.pika.pockethorses.storage;

import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class Storage {

    public abstract void init();

    public abstract void close();

    public abstract void giveHorse(Player player, ConfigHorse horse);
    public abstract void takeHorse(Player player, Horse horse);

    public abstract void setStoredItems(Horse horse, ItemStack[] items);

}
