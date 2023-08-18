package it.pika.pockethorses.utils;

import com.google.common.collect.Maps;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Cooldown {

    private final Map<String, Instant> cooldownMap = Maps.newHashMap();

    public void setCooldown(UUID key, String horse, Duration duration) {
        cooldownMap.put(key.toString() + "-" + horse, Instant.now().plus(duration));
    }

    public boolean hasCooldown(UUID key, String horse) {
        var cooldown = cooldownMap.get(key.toString() + "-" + horse);
        return cooldown != null && Instant.now().isBefore(cooldown);
    }

    public void removeCooldown(UUID key, String horse) {
        cooldownMap.remove(key.toString() + "-" + horse);
    }

    public Duration getRemainingCooldown(UUID key, String horse) {
        var cooldown = cooldownMap.get(key.toString() + "-" + horse);
        var now = Instant.now();

        if (cooldown != null && now.isBefore(cooldown)) {
            return Duration.between(now, cooldown);
        } else {
            removeCooldown(key, horse);
            return Duration.ZERO;
        }
    }

}
