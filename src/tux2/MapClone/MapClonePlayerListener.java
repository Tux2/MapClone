package tux2.MapClone;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author tux2
 */
public class MapClonePlayerListener extends PlayerListener {
    private final MapClone plugin;

    public MapClonePlayerListener(MapClone instance) {
        plugin = instance;
    }

    //Insert Player related code here
}

