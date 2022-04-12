package gyurix.coliseumgames.gui;

import gyurix.coliseumgames.CGAPI;
import gyurix.coliseumgames.data.Game;
import gyurix.coliseumgames.data.PlayerData;
import gyurix.coliseumgames.data.Upgrade;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.coliseumgames.conf.ConfigManager.conf;

public class UpgradesGUI extends CustomGUI {
    private Game game;
    private PlayerData pd;

    public UpgradesGUI(Player plr) {
        super(plr, conf.getUpgradesGUI());
    }

    @Override
    public void create() {
        game = CGAPI.playerGames.get(plr.getName());
        pd = game.getTeam1().get(plr.getName());
        if (pd == null)
            pd = game.getTeam2().get(plr.getName());
        super.create();
    }

    @Override
    public ItemStack getCustomItem(String name) {
        Upgrade upg = conf.getUpgrades().get(name);
        return upg.getName().equals(pd.getUpgrades().get(upg.getType())) ? upg.getIconSel() : upg.getIcon();
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
