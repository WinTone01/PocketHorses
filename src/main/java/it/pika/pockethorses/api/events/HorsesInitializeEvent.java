package it.pika.pockethorses.api.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The event is triggered when the PocketHorses plugin finishes its initialization
 */
public class HorsesInitializeEvent extends Event {

    @Getter
    private final Plugin plugin;

    public HorsesInitializeEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
