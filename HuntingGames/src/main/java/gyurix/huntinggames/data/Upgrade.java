package gyurix.huntinggames.data;

import com.nftworlds.wallet.objects.NFTPlayer;
import com.nftworlds.wallet.objects.Network;
import com.nftworlds.wallet.objects.Wallet;
import gyurix.huntinggames.HGAPI;
import gyurix.huntinggames.enums.GameState;
import gyurix.huntinggames.gui.UpgradeRunnable;
import gyurix.huntinggames.util.ItemUtils;
import gyurix.huntinggames.util.StrUtils;
import gyurix.shopsystem.PlayerManager;
import gyurix.shopsystem.ShopAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static gyurix.huntinggames.conf.ConfigManager.msg;
import static gyurix.huntinggames.util.StrUtils.DF;

@Getter
public class Upgrade {
    private BarColor barColor;
    private String barText;
    private double damage;
    private int delay;
    private ItemStack item;
    private double maxDist;
    private double minDamage;
    private double minDist;
    @Setter
    private String name;
    private double price;

    public void apply(PlayerData pd) {
        PlayerInventory pi = pd.getPlayer().getInventory();
        pi.addItem(item.clone());
    }

    public ItemStack getGUIItem(Player plr) {
        AtomicLong expiration = new AtomicLong();
        PlayerManager.withPlayerData(plr.getUniqueId(),
                pd -> expiration.set(pd.getBoughtItems().getOrDefault(name, 0L)));
        long time = System.currentTimeMillis();
        List<String> loreSuffix = msg.getList("upgrade.lore." + (expiration.get() > time ? "purchased" : "buy"));
        return ItemUtils.addLore(item, loreSuffix, "price", DF.format(price), "expire", StrUtils.formatTime(expiration.get() - time));
    }

    public void select(Game game, Player plr) {
        AtomicLong expiration = new AtomicLong();
        PlayerManager.withPlayerData(plr.getUniqueId(),
                pd -> expiration.set(pd.getBoughtItems().getOrDefault(name, 0L)));
        long time = System.currentTimeMillis();
        if (expiration.get() > time) {
            msg.msg(plr, "upgrade.already");
            return;
        }
        if (price == 0 || plr.hasPermission("shopsystem.free")) {
            selectNow(game, plr);
            return;
        }
        Wallet wallet = NFTPlayer.getByUUID(plr.getUniqueId()).getPrimaryWallet();
        wallet.requestWRLD(price, Network.POLYGON, "Hunting Games Upgrade - " + item.getItemMeta().getDisplayName(), false,
                (UpgradeRunnable) () -> selectNow(game, plr));
    }

    public void selectNow(Game game, Player plr) {
        Game curGame = HGAPI.playerGames.get(plr.getName());
        if (game != curGame || game != null && game.getState() != GameState.STARTING && game.getState() != GameState.WAITING)
            return;
        if (game != null) {
            PlayerData pd = game.getPlayers().get(plr.getName());
            pd.getUpgrades().add(name);
        }
        msg.msg(plr, "upgrade.buy", "upgrade", item.getItemMeta().getDisplayName());
        ShopAPI.activateUpgrade(plr, name);
    }
}
