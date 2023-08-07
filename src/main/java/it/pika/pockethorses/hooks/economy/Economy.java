package it.pika.pockethorses.hooks.economy;

import it.pika.pockethorses.enums.EconomyType;
import org.bukkit.entity.Player;

public abstract class Economy {

    public abstract boolean has(Player player, double price);

    public abstract void deposit(Player player, double price);

    public abstract void withdraw(Player player, double price);

    public abstract EconomyType getType();

}
