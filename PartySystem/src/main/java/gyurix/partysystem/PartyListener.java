package gyurix.partysystem;

import gyurix.partysystem.data.Party;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Party party = PartyAPI.partiesByPlayer.remove(e.getPlayer().getName());
        if (party != null)
            party.leave(e.getPlayer().getName(), false);
    }
}
