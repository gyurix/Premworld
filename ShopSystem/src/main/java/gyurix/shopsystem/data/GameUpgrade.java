package gyurix.shopsystem.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class GameUpgrade {
    private String name;
    private long duration;
    private double price;
    private ItemStack icon;
}
