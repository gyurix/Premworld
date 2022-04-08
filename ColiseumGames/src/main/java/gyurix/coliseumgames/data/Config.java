package gyurix.coliseumgames.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
@Getter
public class Config {
    private Counter counters;
    private HashMap<String, Integer> minPlayersPerTeam, maxPlayersPerTeam;
    private List<ItemStack> ingameItems;
    private int titleFadeIn, titleShowTime, titleFadeOut, blindnessDuration, noBowDuration, spawnedPointsPerSecond, maxPointEntities;
    private double rewardMultiplier;
}
