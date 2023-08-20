package it.pika.pockethorses.hooks.economy.impl;

import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.EconomyType;
import it.pika.pockethorses.hooks.economy.Economy;
import org.bukkit.entity.Player;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

public class CoinsEngineEconomy extends Economy {

    private Currency currency;

    public CoinsEngineEconomy(String currencyName) {
        Currency currency = CoinsEngineAPI.getCurrency(currencyName);
        if (currency == null) {
            Main.getConsole().warning("The '%s' currency could not be found!".formatted(currencyName));
            return;
        }

        this.currency = currency;
    }

    @Override
    public boolean has(Player player, double price) {
        if (currency == null)
            return false;

        return CoinsEngineAPI.getBalance(player, currency) >= price;
    }

    @Override
    public void deposit(Player player, double price) {
        if (currency == null)
            return;

        CoinsEngineAPI.addBalance(player, currency, price);
    }

    @Override
    public void withdraw(Player player, double price) {
        if (currency == null)
            return;

        CoinsEngineAPI.removeBalance(player, currency, price);
    }

    @Override
    public EconomyType getType() {
        return EconomyType.COINSENGINE;
    }
}
