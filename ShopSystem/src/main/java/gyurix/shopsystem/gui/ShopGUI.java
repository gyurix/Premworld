package gyurix.shopsystem.gui;

import gyurix.coliseumgames.gui.ColiseumUpgradesGUI;
import gyurix.huntinggames.gui.HuntingUpgradesGUI;
import gyurix.shopsystem.ShopAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.shopsystem.conf.ConfigManager.conf;

public class ShopGUI extends CustomGUI {
    public ShopGUI(Player plr) {
        super(plr, conf.shop);
    }

    @Override
    public ItemStack getCustomItem(String name) {
        return conf.shop.getItem(name, "players", ShopAPI.getPlayers(name));
    }

    @Override
    public void onClick(int slot, boolean right, boolean shift) {
        if (slot < 0 || slot >= inv.getSize())
            return;
        String slotName = config.getLayout().get(slot);
        if (slotName == null)
            return;
        switch (slotName) {
            case "exit" -> {
                plr.closeInventory();
            }
            case "coliseum" -> {
                new ColiseumUpgradesGUI(plr);
            }
            case "hunting" -> {
                new HuntingUpgradesGUI(plr);
            }
        }
    }
}
