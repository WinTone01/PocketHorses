package it.pika.pockethorses.utils;

import com.google.common.collect.Maps;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Cooldowns {

    private final Map<UUID, Instant> cooldowns = Maps.newHashMap();

    public void setCooldown(UUID key, Duration duration)  {
        cooldowns.put(key, Instant.now().plus(duration));
    }

    public boolean hasCooldown(UUID key) {
        var cooldown = cooldowns.get(key);
        return cooldown != null && Instant.now().isBefore(cooldown);
    }

    public void removeCooldown(UUID key) {
        cooldowns.remove(key);
    }

    public Duration getRemainingCooldown(UUID key) {
        var cooldown = cooldowns.get(key);
        var now = Instant.now();

        if (cooldown != null && now.isBefore(cooldown)) {
            return Duration.between(now, cooldown);
        } else {
            removeCooldown(key);
            return Duration.ZERO;
        }
    }

}
