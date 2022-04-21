package gyurix.huntinggames.data;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import gyurix.huntinggames.conf.StringSerializable;
import gyurix.huntinggames.util.StrUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;

@AllArgsConstructor
@Getter
@Setter
public class Area implements StringSerializable {
    private final String world;
    private int minX, minY, minZ, maxX, maxY, maxZ;

    public Area(String in) {
        String[] d = in.split(" ");
        world = d[0];
        minX = Integer.parseInt(d[1]);
        minY = Integer.parseInt(d[2]);
        minZ = Integer.parseInt(d[3]);
        maxX = Integer.parseInt(d[4]);
        maxY = Integer.parseInt(d[5]);
        maxZ = Integer.parseInt(d[6]);
    }

    public Area(Location min, Location max) {
        world = min.getWorld().getName();
        minX = min.getBlockX();
        minY = min.getBlockY();
        minZ = min.getBlockZ();
        maxX = max.getBlockX();
        maxY = max.getBlockY();
        maxZ = max.getBlockZ();
    }

    public Area(String world, Region region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        this.world = world;
        minX = min.getX();
        minY = min.getY();
        minZ = min.getZ();
        maxX = max.getX();
        maxY = max.getY();
        maxZ = max.getZ();
    }

    public void changeBlock(Material type) {
        World w = Bukkit.getWorld(world);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block b = w.getBlockAt(x, y, z);
                    b.setType(type, false);
                }
            }
        }
    }

    public void clearEntities() {
        for (Entity ent : Bukkit.getWorld(world).getEntities()) {
            if (!(ent instanceof Player) && contains(ent.getLocation()))
                ent.remove();
        }
    }

    public Area clone() {
        return new Area(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean contains(Location loc) {
        return loc.getWorld().getName().equals(world) && loc.getX() >= minX - 1 && loc.getY() >= minY - 1 && loc.getZ() >= minZ - 1 && loc.getX() <= maxX + 1 && loc.getY() <= maxY + 1 && loc.getZ() <= maxZ + 1;
    }

    public boolean contains(Loc loc) {
        return loc != null && loc.getWorld().equals(world) && loc.getX() >= minX && loc.getY() >= minY && loc.getZ() >= minZ && loc.getX() <= maxX && loc.getY() <= maxY && loc.getZ() <= maxZ;
    }

    public Location randomLoc(Area... exclude) {
        double xDif = maxX - minX;
        double yDif = maxY - minY;
        double zDif = maxZ - minZ;
        for (int i = 0; i < 100; ++i) {
            Location loc = new Location(Bukkit.getWorld(world), StrUtils.rand.nextDouble() * xDif + minX + 0.5,
                StrUtils.rand.nextDouble() * yDif + minY + 0.5,
                StrUtils.rand.nextDouble() * zDif + minZ + 0.5);
            boolean correct = true;
            for (Area a : exclude) {
                if (a.contains(loc)) {
                    correct = false;
                    break;
                }
            }
            if (correct)
                return loc;
        }
        throw new RuntimeException("Failed to find a valid random location in area " + this + ", excluding " + Arrays.toString(exclude));
    }

    @Override
    public String toString() {
        return world + " " + minX + " " + minY + " " + minZ + " " + maxX + " " + maxY + " " + maxZ;
    }
}
