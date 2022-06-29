package gyurix.coliseumgames.data;

import gyurix.coliseumgames.conf.PostProcessable;
import gyurix.coliseumgames.gui.GUIConfig;
import gyurix.coliseumgames.util.StrUtils;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.TreeMap;

@SuppressWarnings("unused")
@Getter
public class Config implements PostProcessable {
    private TreeMap<String, GameType> gameTypes;
    private GUIConfig upgradesGUI;
    private String healthSuffix;
    private int titleFadeIn, titleShowTime, titleFadeOut;
    private ItemStack upgradeItem, flag1, flag2;
    private int upgradeItemSlot;
    private HashMap<String, Upgrade> upgrades;

    @Override
    public void postProcess() {
        healthSuffix = StrUtils.colorize(healthSuffix);
        upgrades.forEach((name, upgrade) -> upgrade.setName(name));
        gameTypes.forEach((name, gameType) -> gameType.setName(name));
    }
}
