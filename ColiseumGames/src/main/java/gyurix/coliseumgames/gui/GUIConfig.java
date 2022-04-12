package gyurix.coliseumgames.gui;

import gyurix.coliseumgames.conf.PostProcessable;
import gyurix.coliseumgames.util.ItemUtils;
import gyurix.coliseumgames.util.StrUtils;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class GUIConfig implements PostProcessable {
    private HashMap<String, ItemStack> customItems;
    private transient HashMap<Integer, String> customMap = new HashMap<>();
    private List<String> layout;
    private HashMap<String, ItemStack> staticItems;
    private transient HashMap<Integer, ItemStack> staticMap = new HashMap<>();
    private String title;

    public ItemStack getItem(String key, Object... vars) {
        return ItemUtils.fillVariables(customItems.get(key), vars);
    }

    public ItemStack getStaticItem(String key) {
        return ItemUtils.fillVariables(staticItems.get(key));
    }

    @Override
    public void postProcess() {
        title = StrUtils.colorize(title);
        int slot = 0;
        List<String> fixedLayout = new ArrayList<>();
        for (String row : layout) {
            String[] cols = row.split(" +");
            if (cols.length != 9)
                throw new RuntimeException("Invalid gui layout, 9 columns excepted, found " + cols.length + " in row " + row);
            for (String col : cols) {
                fixedLayout.add(col);
                if (staticItems.containsKey(col))
                    staticMap.put(slot, staticItems.get(col));
                else
                    customMap.put(slot, col);
                ++slot;
            }
        }
        layout = fixedLayout;
    }
}
