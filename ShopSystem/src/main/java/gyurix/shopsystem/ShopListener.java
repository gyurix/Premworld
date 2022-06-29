package gyurix.shopsystem;

import com.nftworlds.wallet.event.PlayerTransactEvent;
import gyurix.coliseumgames.CGAPI;
import gyurix.coliseumgames.data.Game;
import gyurix.huntinggames.HGAPI;
import gyurix.shopsystem.conf.ShopRunnable;
import gyurix.timedtrials.TTAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.conf.ConfigManager.msg;

public class ShopListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onIngameCommand(PlayerCommandPreprocessEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game coliseum = CGAPI.playerGames.get(plr.getName());
        gyurix.huntinggames.data.Game hunt = HGAPI.playerGames.get(pln);
        gyurix.timedtrials.data.Game trials = TTAPI.playerGames.get(pln);
        if (coliseum != null || hunt != null || trials != null) {
            if (conf.allowedCommands.contains(e.getMessage().split(" ", 2)[0].substring(1).toLowerCase()) || plr.hasPermission("shopsystem.cmd"))
                return;
            e.setCancelled(true);
            msg.msg(plr, "nocmd");
        }
    }

    @EventHandler
    public void onPlayerTransact(PlayerTransactEvent<?> e) {
        if (e.getPayload() instanceof ShopRunnable)
            ((ShopRunnable) e.getPayload()).run();
    }
}
