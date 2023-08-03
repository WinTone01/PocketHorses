package it.pika.pockethorses.hooks.economy.impl;

import it.pika.pockethorses.enums.EconomyType;
import it.pika.pockethorses.hooks.economy.Economy;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class VaultEconomy extends Economy {

    private final net.milkbowl.vault.economy.Economy economy;

    @Override
    public boolean has(Player player, double price) {
        return economy.has(player, price);
    }

    @Override
    public void deposit(Player player, double price) {
        economy.depositPlayer(player, price);
    }

    @Override
    public void withdraw(Player player, double price) {
        economy.withdrawPlayer(player, price);
    }

    @Override
    public EconomyType getType() {
        return EconomyType.VAULT;
    }
}
