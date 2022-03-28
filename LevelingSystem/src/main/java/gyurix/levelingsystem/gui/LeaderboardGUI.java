package gyurix.levelingsystem.gui;

import gyurix.levelingsystem.LevelingAPI;
import gyurix.levelingsystem.data.PlayerData;
import gyurix.levelingsystem.util.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.levelingsystem.conf.ConfigManager.conf;
import static gyurix.levelingsystem.util.StrUtils.DF;

public class LeaderboardGUI extends CustomGUI {
    public LeaderboardGUI(Player plr) {
        super(plr, conf.leaderBoardGUI);
        plr.openInventory(inv);
    }

    @Override
    public ItemStack getCustomItem(String name) {
        int position = Integer.parseInt(name.substring(3));
        if (position > LevelingAPI.top.size())
            return config.getItem("noplayer", "position", position);
        PlayerData pd = LevelingAPI.top.get(position - 1);
        ItemStack is = config.getItem("top",
            "position", position,
            "player", pd.getName(),
            "level", DF.format(pd.getLevel()),
            "exp", DF.format(pd.getExp()));
        return ItemUtils.setOwner(is, pd.getName());
    }

    @Override
    public void onClick(int slot, boolean right, boolean shift) {
    }
}
