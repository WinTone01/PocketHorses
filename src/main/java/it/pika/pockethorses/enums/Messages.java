package it.pika.pockethorses.enums;

import it.pika.libs.config.Config;
import it.pika.pockethorses.Main;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Messages {

    NO_PERMISSION("no-permission"),
    WRONG_PARAMETERS("wrong-parameters"),
    ONLY_BY_PLAYERS("only-by-players"),
    MUST_BE_ONLINE("must-be-online"),
    HORSE_NOT_EXISTING("horse-not-existing"),
    HORSE_GIVEN("horse-given"),
    HORSE_SPAWNED("horse-spawned"),
    RELOAD("reload"),
    CHANGE_NAME("change-name"),
    CUSTOM_NAME_SET("custom-name-set"),
    GET_UP("get-up"),
    MAKE_SIT("make-sit"),
    SET_SPEED("set-speed"),
    INVALID_NUMBER("invalid-number"),
    INVALID_SPEED("invalid-speed"),
    SPEED_SET("speed-set"),
    HORSE_REMOVED("horse-removed"),
    NOT_THE_OWNER("not-the-owner"),
    RIDING_HORSE("riding-horse"),
    CANNOT_SPAWN("cannot-spawn"),
    SHOP_NOT_ENABLED("shop-not-enabled"),
    NOT_ENOUGH_MONEY("not-enough-money"),
    PURCHASE_COMPLETED("purchase-completed"),
    ALREADY_SPAWNED("already-spawned"),
    ALREADY_OWNED("already-owned"),
    NO_HORSES_SPAWNED("no-horses-spawned"),
    HORSES_RECALLED("horses-recalled"),
    NEW_UPDATE("new-update"),
    AUTO_RECALL_DISABLED("auto-recall-disabled"),
    AUTO_RECALL_ENABLED("auto-recall-enabled"),
    AUTO_RECALLED("auto-recalled"),
    VOUCHER_NOT_EXISTING("voucher-not-existing"),
    VOUCHER_GIVEN("voucher-given"),
    IN_COOLDOWN("in-cooldown"),
    ENABLED("enabled"),
    DISABLED("disabled"),
    UNDEFINED("undefined"),
    HORSE_EDITED("horse-edited"),
    HORSE_CREATED("horse-created"),
    SET_ALL_SETTINGS("set-all-settings"),
    HORSE_ALREADY_EXISTS("horse-already-exists"),
    INVALID_FILE("invalid-file"),
    FILE_RELOADED("file-reloaded"),
    HORSE_RECYCLED("horse-recycled"),
    COMMAND_DOESNT_EXIST("command-doesnt-exist"),
    ITEM_NOT_EXISTING("item-not-existing"),
    ITEM_GIVEN("item-given"),
    ITEM_TYPE_NOT_EXISTING("item-type-not-existing"),
    ITEM_USED("item-used"),
    SUPPLEMENT_EXPIRED("supplement-expired"),
    LIMIT_REACHED("limit-reached"),
    REMOVED_HORSES("removed-horses");

    private final String path;

    public String get() {
        var language = Main.getLanguageManager().getLanguage();
        if (!language.getFile().exists())
            return "Undefined";

        var config = new Config(Main.getInstance(), language.getFile(), false);
        return Main.parseColors(config.getString(path));
    }

}
