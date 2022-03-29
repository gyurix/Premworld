package gyurix.levelingsystem.gui;

import gyurix.levelingsystem.conf.PostProcessable;
import gyurix.levelingsystem.util.ItemUtils;
import gyurix.levelingsystem.util.StrUtils;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

@Getter
public class GUIConfig implements PostProcessable {
    private HashMap<String, ItemStack> customItems;
    private HashMap<String, ItemStack> staticItems;
    private transient HashMap<Integer, String> customMap = new HashMap<>();
    private List<String> layout;
    private transient HashMap<Integer, ItemStack> staticMap = new HashMap<>();
    private String title;

    @Override
    public void postProcess() {
        title = StrUtils.colorize(title);
        int slot = 0;
        for (String row : layout) {
            String[] cols = row.split(" +");
            if (cols.length != 9)
                throw new RuntimeException("Invalid gui layout, 9 columns excepted, found " + cols.length + " in row " + row);
            for (String col : cols) {
                if (staticItems.containsKey(col))
                    staticMap.put(slot, staticItems.get(col));
                else
                    customMap.put(slot, col);
                ++slot;
            }
        }
    }

    public ItemStack getItem(String key, Object... vars) {
        return ItemUtils.fillVariables(customItems.get(key), vars);
    }

    public ItemStack getStaticItem(String key) {
        return ItemUtils.fillVariables(staticItems.get(key));
    }
}
