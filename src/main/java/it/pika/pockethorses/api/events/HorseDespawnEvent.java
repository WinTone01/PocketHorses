package it.pika.pockethorses.api.events;

import it.pika.pockethorses.objects.Horse;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The event is triggered when despawing a horse through the GUI
 */
public class HorseDespawnEvent extends Event implements Cancellable {

    @Getter @Setter private boolean cancelled = false;
    @Getter private final Player player;
    @Getter private final Horse horse;

    public HorseDespawnEvent(Player player, Horse horse) {
        this.player = player;
        this.horse = horse;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
