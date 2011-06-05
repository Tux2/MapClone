package tux2.MapClone;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * MapClone block listener
 * @author tux2
 */
public class MapCloneBlockListener extends BlockListener {
    private final MapClone plugin;

    public MapCloneBlockListener(final MapClone plugin) {
        this.plugin = plugin;
    }

    //put all Block related code here
}
