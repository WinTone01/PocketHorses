package it.pika.pockethorses.storage.impl;

import it.pika.libs.sql.mysql.Connection;
import it.pika.pockethorses.PocketHorses;
import it.pika.pockethorses.enums.StorageType;
import it.pika.pockethorses.objects.horses.ConfigHorse;
import it.pika.pockethorses.objects.horses.Horse;
import it.pika.pockethorses.storage.Storage;
import it.pika.pockethorses.utils.Serializer;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.UUID;

public class MySQL extends Storage {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;

    public MySQL(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    @SneakyThrows
    public void init() {
        connection = new Connection(host, port, database, username, password);
        connection.connect();

        if (connection.isConnectionValid()) {
            Bukkit.getScheduler().runTaskAsynchronously(PocketHorses.getInstance(), () -> {
                try {
                    connection.update("CREATE TABLE IF NOT EXISTS `horses` (" +
                            "`uuid` VARCHAR(255) NOT NULL," +
                            "`name` VARCHAR(255) NOT NULL," +
                            "`owner` VARCHAR(255) NOT NULL," +
                            "`customName` VARCHAR(255) NULL," +
                            "`storedItems` VARCHAR(255) NULL," +
                            "PRIMARY KEY (`uuid`));");

                    connection.update("ALTER TABLE `horses` MODIFY COLUMN storedItems TEXT");

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
        return StorageType.MYSQL;
    }

}
