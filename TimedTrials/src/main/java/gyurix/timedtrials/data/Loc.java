package gyurix.timedtrials.data;

import gyurix.timedtrials.conf.StringSerializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter
@EqualsAndHashCode
public class Loc implements StringSerializable {
    private String world;
    private int x, y, z;
    private float yaw;

    public Loc(Block b) {
        this.world = b.getWorld().getName();
        this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
    }

    public Loc(String in) {
        String[] d = in.split(" ", 5);
        world = d[0];
        x = Integer.parseInt(d[1]);
        y = Integer.parseInt(d[2]);
        z = Integer.parseInt(d[3]);
        if (d.length > 4)
            yaw = Float.parseFloat(d[4]);
    }

    public Loc(Location loc) {
        world = loc.getWorld().getName();
        x = (int) loc.getX();
        y = (int) loc.getY();
        z = (int) loc.getZ();
        yaw = loc.getYaw();
    }

    public Block toBlock() {
        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

    public Location toLoc() {
        return new Location(Bukkit.getWorld(world), x + 0.5, y, z + 0.5, yaw, 0);
    }

    @Override
    public String toString() {
        return world + " " + x + " " + y + " " + z + " " + yaw;
    }
}
