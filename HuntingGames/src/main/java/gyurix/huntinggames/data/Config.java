package gyurix.huntinggames.data;

import gyurix.huntinggames.conf.PostProcessable;
import gyurix.huntinggames.gui.GUIConfig;
import gyurix.huntinggames.util.StrUtils;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("unused")
@Getter
public class Config implements PostProcessable {
    private Counter counters;
    private List<String> defaultUpgrades;
    private int drawExp, winExp, loseExp;
    private int minPlayers, maxPlayers, mobSpawnCount, maxMobsPerPlayer;
    private double mobSpawnChance;
    private String mobHpSuffix, rifleSound, shotgunSound;
    private int titleFadeIn, titleShowTime, titleFadeOut;
    private ItemStack upgradeItem;
    private int upgradeItemSlot;
    private HashMap<String, Upgrade> upgrades;
    private HashMap<String, Mob> mobs;
    private GUIConfig upgradesGUI;

    @Override
    public void postProcess() {
        upgrades.forEach((name, upgrade) -> upgrade.setName(name));
        mobHpSuffix = StrUtils.colorize(mobHpSuffix);
    }
}
