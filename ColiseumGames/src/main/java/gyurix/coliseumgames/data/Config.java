package gyurix.coliseumgames.data;

import gyurix.coliseumgames.conf.PostProcessable;
import gyurix.coliseumgames.gui.GUIConfig;
import gyurix.coliseumgames.util.StrUtils;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
@Getter
public class Config implements PostProcessable {
    private HashMap<String, Counter> counters;
    private List<String> defaultUpgrades;
    private String healthSuffix;
    private List<ItemStack> ingameItems;
    private HashMap<String, Integer> minPlayersPerTeam, maxPlayersPerTeam;
    private int titleFadeIn, titleShowTime, titleFadeOut, blindnessDuration, noBowDuration, spawnedPointsPerSecond, maxPointEntities;
    private ItemStack upgradeItem;
    private int upgradeItemSlot;
    private HashMap<String, Upgrade> upgrades;
    private GUIConfig upgradesGUI;

    @Override
    public void postProcess() {
        healthSuffix = StrUtils.colorize(healthSuffix);
        upgrades.forEach((name, upgrade) -> upgrade.setName(name));
    }
}
