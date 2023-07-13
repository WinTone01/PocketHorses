package it.pika.pockethorses;

public class Perms {

    public static final String
            GIVE = "pockethorses.give",
            HORSES_GUI = "pockethorses.gui.horses",
            HORSE_GUI = "pockethorses.gui.horse",
            STORAGE_GUI = "pockethorses.gui.storage",
            SHOP_GUI = "pockethorses.gui.shop",
            LIST = "pockethorses.list",
            GIVE_VOUCHER = "pockethorses.give.voucher",
            LIST_VOUCHERS = "pockethorses.list.vouchers",
            RELOAD = "pockethorses.reload";

    public static String getHorse(String name) {
        return "pockethorses.horse.%s".formatted(name);
    }

    public static String getVoucher(String name) {
        return "pockethorses.voucher.%s".formatted(name);
    }


}
