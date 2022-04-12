package gyurix.shopsystem.gui;

import com.nftworlds.wallet.objects.NFTPlayer;
import com.nftworlds.wallet.objects.Network;
import com.nftworlds.wallet.objects.Wallet;
import gyurix.shopsystem.conf.ShopRunnable;
import gyurix.shopsystem.data.TicketSettings;
import gyurix.shopsystem.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.conf.ConfigManager.msg;

public class ShopGUI extends CustomGUI {
    public ShopGUI(Player plr, ShopGUIConfig config) {
        super(plr, config);
    }

    @Override
    public ItemStack getCustomItem(String name) {
        return conf.tickets.get(name).getIcon();
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
        String category = ((ShopGUIConfig) config).getCategories().get(slotName);
        if (category != null) {
            openCategory(category);
            return;
        }

        TicketSettings ticketSettings = conf.tickets.get(slotName);
        if (ticketSettings != null)
            buyTicket(ticketSettings);
    }

    private void buyTicket(TicketSettings ticketSettings) {
        double price = ticketSettings.getPrice();
        ItemStack is = ticketSettings.getItem(plr);
        if (ItemUtils.countItemSpace(plr, is) < 1) {
            msg.msg(plr, "notenoughspace");
            return;
        }
        Wallet wallet = NFTPlayer.getByUUID(plr.getUniqueId()).getPrimaryWallet();
        double balance = wallet.getPolygonWRLDBalance();
        if (plr.hasPermission("shopsystem.free")) {
            plr.getInventory().addItem(is);
            return;
        }
        if (balance < price) {
            msg.msg(plr, "notenoughmoney");
            return;
        }
        wallet.requestWRLD(price, Network.POLYGON, "Buying " + ticketSettings.getItem().getItemMeta().getDisplayName(), false,
                (ShopRunnable) () -> plr.getInventory().addItem(is));
    }

    private void openCategory(String category) {
        ShopGUIConfig shopConfig = conf.shops.get(category);
        if (shopConfig == null) {
            Bukkit.getConsoleSender().sendMessage("§f[ShopSystem]§c Error, category §e" + category + "§f is not configured");
            return;
        }
        if (category.equals("exit")) {
            plr.closeInventory();
            return;
        }
        new ShopGUI(plr, shopConfig);
    }
}
