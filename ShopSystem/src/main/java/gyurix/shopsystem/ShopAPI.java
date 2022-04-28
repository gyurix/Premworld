package gyurix.shopsystem;

import gyurix.coliseumgames.CGAPI;
import gyurix.huntinggames.HGAPI;
import gyurix.shopsystem.util.ItemUtils;
import gyurix.timedtrials.TTAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ShopAPI {

    public static int getPlayers(String gameMode) {
        return switch (gameMode) {
            case "hunting" -> HGAPI.playerGames.size();
            case "trials" -> TTAPI.playerGames.size();
            default ->
                    (int) CGAPI.playerGames.values().stream().filter(game -> game.getArena().getType().equals(gameMode)).count();
        };
    }

    public static boolean hasTicket(Player plr, String command) {
        return hasTicket(plr.getInventory(), command) || hasTicket(plr.getEnderChest(), command);
    }

    public static boolean hasTicket(Inventory inv, String command) {
        for (int i = 0; i < inv.getSize(); ++i) {
            ItemStack is = inv.getItem(i);
            String ticketData = ItemUtils.getCustomNBTTag(is, "ticketData");
            if (ticketData == null)
                continue;
            String[] d = ticketData.split(" ", 2);
            long expiration = Long.parseLong(d[0]);
            if (expiration >= System.currentTimeMillis() && d[1].equals(command))
                return true;
        }
        return false;
    }
}
