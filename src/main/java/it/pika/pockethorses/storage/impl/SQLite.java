package it.pika.pockethorses.storage.impl;

import it.pika.libs.sql.sqlite.Connection;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.storage.Storage;
import it.pika.pockethorses.utils.Serializer;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

public class SQLite extends Storage {

    @Getter
    private File file;
    @Getter
    private Connection connection;

    @Override
    @SneakyThrows
    public void init() {
        file = new File(PocketHorses.getInstance().getDataFolder(), "storage.db");
        if (!file.exists())
            file.createNewFile();

        connection = new Connection(file);
        connection.connect();

        if (connection.isConnectionValid()) {
            connection.update("CREATE TABLE IF NOT EXISTS `horses` (" +
                    "`uuid` VARCHAR(255) NOT NULL PRIMARY KEY," +
                    "`name` VARCHAR(255) NOT NULL," +
                    "`owner` VARCHAR(255) NOT NULL," +
                    "`customName` VARCHAR(255) NULL," +
                    "`storedItems` VARCHAR(255) NULL);");

            var result = connection.query("SELECT * FROM horses");
            while (result.next()) {
                var horse = new Horse(UUID.fromString(result.getString("uuid")),
                        result.getString("name"),
                        result.getString("owner"),
                        result.getString("customName"),
                        result.getString("storedItems"));

                PocketHorses.getCache().add(horse);
            }

            result.close();
        }
    }

    @Override
    public void close() {
        if (connection.isConnectionValid())
            connection.close();
    }

    @Override
    @SneakyThrows
    public void giveHorse(Player player, ConfigHorse horse) {
        var uuid = UUID.randomUUID();

        PocketHorses.getCache().add(new Horse(uuid, horse.getId(), player.getName(),
                null, null));

        connection.preparedUpdate("INSERT INTO horses(uuid,name,owner) VALUES(?,?,?)",
                uuid.toString(), horse.getId(), player.getName());
    }

    @Override
    @SneakyThrows
    public void takeHorse(Player player, Horse horse) {
        PocketHorses.getCache().remove(horse);

        connection.preparedUpdate("DELETE FROM horses WHERE uuid = ?", horse.getUuid().toString());
    }

    @Override
    @SneakyThrows
    public void setStoredItems(Horse horse, ItemStack[] items) {
        PocketHorses.getCache().get(PocketHorses.getCache().lastIndexOf(PocketHorses.getHorse(horse.getUuid())))
                .setStoredItems(Serializer.serialize(items));

        connection.preparedUpdate("UPDATE horses SET storedItems = ? WHERE uuid = ?",
                Serializer.serialize(items), horse.getUuid().toString());
    }

}
