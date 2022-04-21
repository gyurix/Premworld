package gyurix.huntinggames.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LocUtils {
    public static Location fixLoc(Location loc, float yaw) {
        loc = fixLoc(loc);
        loc.setYaw(yaw);
        return loc;
    }

    public static Location fixLoc(Location loc) {
        Block b = loc.getBlock();
        while (!b.isSolid() && b.getY() > -64)
            b = b.getRelative(BlockFace.DOWN);
        loc = loc.clone();
        loc.setY(b.getY() + 1);
        return loc;
    }
}
