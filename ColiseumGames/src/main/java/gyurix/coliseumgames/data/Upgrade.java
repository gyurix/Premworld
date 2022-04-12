package gyurix.coliseumgames.data;

import com.nftworlds.wallet.objects.NFTPlayer;
import com.nftworlds.wallet.objects.Network;
import com.nftworlds.wallet.objects.Wallet;
import gyurix.coliseumgames.CGAPI;
import gyurix.coliseumgames.enums.GameState;
import gyurix.coliseumgames.gui.UpgradeRunnable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

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

    public void select(Game game, Player plr) {
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
        if (game != curGame || game.getState() != GameState.STARTING && game.getState() != GameState.WAITING)
            return;
        PlayerData pd = game.getTeam1().get(plr.getName());
        if (pd == null)
            pd = game.getTeam2().get(plr.getName());
        msg.msg(plr, "game.upgrade", "upgrade", icon.getItemMeta().getDisplayName());
        pd.getUpgrades().put(type, name);
    }
}
