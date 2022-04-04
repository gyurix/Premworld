package gyurix.shopsystem;

import gyurix.shopsystem.util.StrUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static gyurix.shopsystem.conf.ConfigManager.msg;

public class TicketUseListener implements Listener {
    @EventHandler
    public void onTicketUse(PlayerInteractEvent e) {
        ItemStack is = e.getItem();
        if (is == null || !is.hasItemMeta())
            return;
        List<String> lore = is.getItemMeta().getLore();
        String ticketData = StrUtils.getInvisibleText(lore);
        if (ticketData == null)
            return;
        String[] d = ticketData.split(" ", 2);
        long expiration = Long.parseLong(d[0]);
        Player plr = e.getPlayer();
        if (expiration < System.currentTimeMillis()) {
            msg.msg(plr, "expired");
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), d[1].replace("<player>", plr.getName()));
    }
}
