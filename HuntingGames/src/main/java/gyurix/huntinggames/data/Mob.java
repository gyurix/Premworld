package gyurix.huntinggames.data;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import gyurix.huntinggames.conf.PostProcessable;
import gyurix.huntinggames.util.StrUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static gyurix.huntinggames.conf.ConfigManager.conf;
import static gyurix.huntinggames.util.StrUtils.DF;

@Getter
public class Mob implements PostProcessable {
    private double chance;
    private int hp, points;
    private String model;
    private String name;
    private EntityType type;

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
        if (model != null) {
            ent.setInvulnerable(true);
            ent.setGravity(false);
            //ent.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,10000000,0,false, false));
            ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel(getModel());
            ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(ent);
            modeledEntity.addActiveModel(model);
            modeledEntity.detectPlayers();
        }
        return ent;
    }
}
