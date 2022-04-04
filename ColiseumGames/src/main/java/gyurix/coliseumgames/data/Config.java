package gyurix.coliseumgames.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("unused")
@Getter
public class Config {
    private Counter counters;
    private List<ItemStack> ingameItems;
    private int minPlayers, maxPlayers, titleFadeIn, titleShowTime, titleFadeOut, blindnessDuration, noBowDuration, spawnedPointsPerSecond, maxPointEntities;
    private double rewardMultiplier;
}
