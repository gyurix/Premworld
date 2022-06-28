package gyurix.shopsystem.gui;

import com.nftworlds.wallet.objects.NFTPlayer;
import com.nftworlds.wallet.objects.Network;
import com.nftworlds.wallet.objects.Wallet;
import gyurix.shopsystem.conf.ShopRunnable;
import gyurix.shopsystem.data.GameUpgrade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static gyurix.shopsystem.PlayerManager.withPlayerData;
import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.conf.ConfigManager.msg;

public class ShopGUI extends CustomGUI {
    public ShopGUI(Player plr, ShopGUIConfig config) {
        super(plr, config);
    }

    private void buyTicket(GameUpgrade upgrade) {
        double price = upgrade.getPrice();
        Wallet wallet = NFTPlayer.getByUUID(plr.getUniqueId()).getPrimaryWallet();
        double balance = wallet.getPolygonWRLDBalance();
        if (plr.hasPermission("shopsystem.free")) {
            withPlayerData(plr.getUniqueId(),
                    pd -> pd.addBoughtItem(upgrade.getName(), System.currentTimeMillis() + upgrade.getDuration()));
            return;
        }
        if (balance < price) {
            msg.msg(plr, "notenoughmoney");
            return;
        }
        wallet.requestWRLD(price, Network.POLYGON, "Buying " + upgrade.getName(), false,
                (ShopRunnable) () -> withPlayerData(plr.getUniqueId(),
                        pd -> pd.addBoughtItem(upgrade.getName(), System.currentTimeMillis() + upgrade.getDuration())));
    }

    @Override
    public ItemStack getCustomItem(String name) {
        return conf.upgrades.get(name).getIcon();
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
