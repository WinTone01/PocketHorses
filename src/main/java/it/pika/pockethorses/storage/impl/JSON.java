package it.pika.pockethorses.storage.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;

public class JSON extends Storage {

    @Getter
    private File file;
    @Getter
    private static final Gson gson = new Gson();

    @Override
    @SneakyThrows
    public void init() {
        file = new File(PocketHorses.getInstance().getDataFolder(), "storage.json");
        if (!file.exists())
            file.createNewFile();

        var reader = new FileReader(file);
        var horses = gson.fromJson(reader, Horse[].class);

        if (horses == null)
            return;

        PocketHorses.setCache(Lists.newArrayList(horses));
    }

    @Override
    @SneakyThrows
    public void close() {
        var writer = new FileWriter(file, false);
        gson.toJson(PocketHorses.getCache(), writer);
        writer.flush();
        writer.close();
    }

    @Override
    public void giveHorse(Player player, ConfigHorse horse) {
        PocketHorses.getCache().add(new Horse(UUID.randomUUID(), horse.getId(), player.getName(),
                null, null));
    }

    @Override
    @SneakyThrows
    public void takeHorse(Player player, Horse horse) {
        PocketHorses.getCache().remove(horse);
    }

    @Override
    public void setCustomName(Horse horse, String name) {
        PocketHorses.getCache().get(PocketHorses.getCache().lastIndexOf(PocketHorses.getHorse(horse.getUuid()))).setCustomName(name);
    }

    @Override
    public void setStoredItems(Horse horse, ItemStack[] items) {
        PocketHorses.getCache().get(PocketHorses.getCache().lastIndexOf(PocketHorses.getHorse(horse.getUuid())))
                .setStoredItems(Serializer.serialize(items));
    }

}
