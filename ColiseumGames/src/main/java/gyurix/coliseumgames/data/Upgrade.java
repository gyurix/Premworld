package gyurix.coliseumgames.data;

import com.nftworlds.wallet.objects.NFTPlayer;
import com.nftworlds.wallet.objects.Network;
import com.nftworlds.wallet.objects.Wallet;
import gyurix.coliseumgames.CGAPI;
import gyurix.coliseumgames.enums.GameState;
import gyurix.coliseumgames.gui.UpgradeRunnable;
import gyurix.coliseumgames.util.ItemUtils;
import gyurix.coliseumgames.util.StrUtils;
import gyurix.shopsystem.PlayerManager;
import gyurix.shopsystem.ShopAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import static gyurix.coliseumgames.conf.ConfigManager.msg;

@Getter
public class Upgrade {
    private double cost;
    private ItemStack icon;
    private ItemStack iconSel;
    private HashMap<Integer, ItemStack> items;
    @Setter
    private String name;
    private String type;

    public void apply(PlayerData pd) {
        PlayerInventory pi = pd.getPlayer().getInventory();
        items.forEach(pi::setItem);
    }

    public ItemStack getIconSel(long expiration) {
        return ItemUtils.fillVariables(iconSel, "expire", StrUtils.formatTime(expiration));
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
        if (cost == 0 || plr.hasPermission("shopsystem.free")) {
            selectNow(game, plr);
            return;
        }
        Wallet wallet = NFTPlayer.getByUUID(plr.getUniqueId()).getPrimaryWallet();
        wallet.requestWRLD(cost, Network.POLYGON, game.getArena().getType() + " - " + icon.getItemMeta().getDisplayName(), false,
                (UpgradeRunnable) () -> selectNow(game, plr));
    }

    public void selectNow(Game game, Player plr) {
        Game curGame = CGAPI.playerGames.get(plr.getName());
        if (game != curGame || game != null && game.getState() != GameState.STARTING && game.getState() != GameState.WAITING)
            return;
        if (game != null) {
            PlayerData pd = game.getTeam1().get(plr.getName());
            if (pd == null)
                pd = game.getTeam2().get(plr.getName());
            pd.getUpgrades().put(type, name);
        }
        msg.msg(plr, "game.upgrade", "upgrade", icon.getItemMeta().getDisplayName());
        ShopAPI.activateUpgrade(plr, name);
    }
}
