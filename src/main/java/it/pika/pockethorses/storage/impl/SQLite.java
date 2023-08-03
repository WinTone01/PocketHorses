package it.pika.pockethorses.storage.impl;

import it.pika.libs.sql.sqlite.Connection;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.StorageType;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.storage.Storage;
import it.pika.pockethorses.utils.Serializer;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.SQLException;
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
        if (!file.exists()) {
            if (!file.createNewFile())
                PocketHorses.getConsole().warning("An error occurred");
        }

        connection = new Connection(file);
        connection.connect();

        if (connection.isConnectionValid()) {
            Bukkit.getScheduler().runTaskAsynchronously(PocketHorses.getInstance(), () -> {
                try {
                    connection.update("CREATE TABLE IF NOT EXISTS `horses` (" +
                            "`uuid` VARCHAR(255) NOT NULL PRIMARY KEY," +
                            "`name` VARCHAR(255) NOT NULL," +
                            "`owner` VARCHAR(255) NOT NULL," +
                            "`customName` VARCHAR(255) NULL," +
                            "`storedItems` TEXT NULL);");

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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
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
        Bukkit.getScheduler().runTaskAsynchronously(PocketHorses.getInstance(), () -> {
            var uuid = UUID.randomUUID();

            PocketHorses.getCache().add(new Horse(uuid, horse.getId(), player.getName(),
                    null, null));

            try {
                connection.preparedUpdate("INSERT INTO horses(uuid,name,owner) VALUES(?,?,?)",
                        uuid.toString(), horse.getId(), player.getName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    @SneakyThrows
    public void takeHorse(Player player, Horse horse) {
        Bukkit.getScheduler().runTaskAsynchronously(PocketHorses.getInstance(), () -> {
            PocketHorses.getCache().remove(horse);

            try {
                connection.preparedUpdate("DELETE FROM horses WHERE uuid = ?", horse.getUuid().toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void setCustomName(Horse horse, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(PocketHorses.getInstance(), () -> {
            PocketHorses.getCache().get(PocketHorses.getCache().lastIndexOf(PocketHorses.getHorse(horse.getUuid()))).setCustomName(name);

            try {
                connection.preparedUpdate("UPDATE horses SET customName = ? WHERE uuid = ?", name,
                        horse.getUuid().toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    @SneakyThrows
    public void setStoredItems(Horse horse, ItemStack[] items) {
        Bukkit.getScheduler().runTaskAsynchronously(PocketHorses.getInstance(), () -> {
            PocketHorses.getCache().get(PocketHorses.getCache().lastIndexOf(PocketHorses.getHorse(horse.getUuid())))
                    .setStoredItems(Serializer.serialize(items));

            try {
                connection.preparedUpdate("UPDATE horses SET storedItems = ? WHERE uuid = ?",
                        Serializer.serialize(items), horse.getUuid().toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public StorageType getType() {
        return StorageType.SQLITE;
    }

}
