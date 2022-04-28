package gyurix.shopsystem.gui;

import gyurix.shopsystem.ShopAPI;
import gyurix.shopsystem.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.conf.ConfigManager.msg;

public class JoinGUI extends CustomGUI {
    public JoinGUI(Player plr) {
        super(plr, conf.joinGUI);
    }

    @Override
    public ItemStack getCustomItem(String name) {
        return ItemUtils.fillVariables(config.getCustomItems().get(name), "players", ShopAPI.getPlayers(name),
                "ticket", ShopAPI.hasTicket(plr, ((JoinGUIConfig) config).getTicketCommands().get(name)) ? "§ayes" : "§cno");
    }

    @Override
    public void onClick(int slot, boolean right, boolean shift) {
        if (slot < 0 || slot >= inv.getSize())
            return;
        String slotName = config.getLayout().get(slot);
        if (slotName == null)
            return;
        if (slotName.equals("exit")) {
            plr.closeInventory();
            return;
        }

        String cmd = ((JoinGUIConfig) config).getTicketCommands().get(slotName);
        if (cmd == null)
            return;
        if (!ShopAPI.hasTicket(plr, cmd)) {
            msg.msg(plr, "noticket");
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>", plr.getName()));
    }
}
