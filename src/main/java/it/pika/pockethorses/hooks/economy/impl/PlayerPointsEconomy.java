package it.pika.pockethorses.hooks.economy.impl;

import it.pika.pockethorses.enums.EconomyType;
import it.pika.pockethorses.hooks.economy.Economy;
import lombok.AllArgsConstructor;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class PlayerPointsEconomy extends Economy {

    private final PlayerPointsAPI economy;

    @Override
    public boolean has(Player player, double price) {
        return economy.look(player.getUniqueId()) >= price;
    }

    @Override
    public void deposit(Player player, double price) {
        economy.give(player.getUniqueId(), (int) Math.round(price));
    }

    @Override
    public void withdraw(Player player, double price) {
        economy.take(player.getUniqueId(), (int) Math.round(price));
    }

    @Override
    public EconomyType getType() {
        return EconomyType.PLAYERPOINTS;
    }

}
