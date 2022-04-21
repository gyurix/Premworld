package gyurix.huntinggames.gui;

import gyurix.huntinggames.HGAPI;
import gyurix.huntinggames.data.Game;
import gyurix.huntinggames.data.PlayerData;
import gyurix.huntinggames.data.Upgrade;
import gyurix.huntinggames.conf.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.huntinggames.conf.ConfigManager.conf;

public class UpgradesGUI extends CustomGUI {
    private Game game;
    private PlayerData pd;

    public UpgradesGUI(Player plr) {
        super(plr, conf.getUpgradesGUI());
    }

    @Override
    public void create() {
        game = HGAPI.playerGames.get(plr.getName());
        pd = game.getPlayers().get(plr.getName());
        super.create();
    }

    @Override
    public ItemStack getCustomItem(String name) {
        Upgrade upg = conf.getUpgrades().get(name);
        return upg.getGUIItem(pd);
    }

    @Override
    public void onClick(int slot, boolean right, boolean shift) {
        if (slot >= inv.getSize() || slot < 0)
            return;
        String type = config.getLayout().get(slot);
        if (type.equals("exit")) {
            plr.closeInventory();
            return;
        }
        Upgrade upg = conf.getUpgrades().get(type);
        if (upg == null)
            return;
        upg.select(game, plr);
        plr.closeInventory();
    }
}
