package gyurix.huntinggames.data;

import gyurix.huntinggames.conf.PostProcessable;
import gyurix.huntinggames.util.StrUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import static gyurix.huntinggames.conf.ConfigManager.conf;
import static gyurix.huntinggames.util.StrUtils.DF;

@Getter
public class Mob implements PostProcessable {
    private EntityType type;
    private String name;
    private double chance;
    private int hp, points;

    @Override
    public void postProcess() {
        name = StrUtils.colorize(name);
    }

    public LivingEntity summon(Location loc) {
        LivingEntity ent = (LivingEntity) loc.getWorld().spawnEntity(loc, type);
        ent.setMaxHealth(hp);
        ent.setHealth(hp);
        ent.setCustomName(name + conf.getMobHpSuffix().replace("<hp>", DF.format(hp)));
        ent.setCustomNameVisible(true);
        return ent;
    }
}
