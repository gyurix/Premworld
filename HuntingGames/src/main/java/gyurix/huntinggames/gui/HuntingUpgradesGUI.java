package gyurix.huntinggames.gui;

import gyurix.huntinggames.HGAPI;
import gyurix.huntinggames.data.Game;
import gyurix.huntinggames.data.PlayerData;
import gyurix.huntinggames.data.Upgrade;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.huntinggames.conf.ConfigManager.conf;

public class HuntingUpgradesGUI extends CustomGUI {
    private Game game;
    private PlayerData pd;

    public HuntingUpgradesGUI(Player plr) {
        super(plr, conf.getUpgradesGui());
    }

    @Override
    public void create() {
        game = HGAPI.playerGames.get(plr.getName());
        pd = game == null ? null : game.getPlayers().get(plr.getName());
        super.create();
    }

    @Override
    public ItemStack getCustomItem(String name) {
        Upgrade upg = conf.getUpgrades().get(name);
        return upg.getGUIItem(plr);
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
        update();
    }
}
