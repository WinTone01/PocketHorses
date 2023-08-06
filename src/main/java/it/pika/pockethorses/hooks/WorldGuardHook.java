package it.pika.pockethorses.hooks;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class WorldGuardHook {

    public void init() {
        var registry = WorldGuard.getInstance().getFlagRegistry();
        var flag = new StateFlag("allow-horses", true);
        registry.register(flag);
    }

}
