package it.pika.pockethorses;

public class Perms {

    public static final String
            GIVE = "pockethorses.give",
            HORSES_GUI = "pockethorses.gui.horses",
            HORSE_GUI = "pockethorses.gui.horse",
            STORAGE_GUI = "pockethorses.gui.storage",
            SHOP_GUI = "pockethorses.gui.shop",
            EDITOR = "pockethorses.editor",
            CREATE = "pockethorses.create",
            LIST = "pockethorses.list",
            RECALL = "pockethorses.recall",
            RELOAD = "pockethorses.reload",
            HELP_MAIN = "pockethorses.help.main",
            HELP_HORSES = "pockethorses.help.horses",
            BYPASS_REGION = "pockethorses.bypass_region",
            DEBUG = "pockethorses.debug",
            REMOVE = "pockethorses.remove",
            HORSE_INFO = "pockethorses.info",
            BUY = "pockethorses.buy",
            AUTO_REMOVE = "pockethorses.autoremove";

    public static String getHorse(String name) {
        return "pockethorses.horse.%s".formatted(name);
    }

    public static String getVoucher(String name) {
        return "pockethorses.voucher.%s".formatted(name);
    }

    public static String getLimit(int n) {
        return "pockethorses.max.%s".formatted(n);
    }


}
