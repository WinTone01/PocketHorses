package it.pika.pockethorses;

public class Perms {

    public static final String
            GIVE = "pockethorses.give",
            SHOP = "pockethorses.shop",
            LIST = "pockethorses.list",
            RELOAD = "pockethorses.reload";

    public static String getHorse(String name) {
        return "pockethorses.horse.%s".formatted(name);
    }


}
