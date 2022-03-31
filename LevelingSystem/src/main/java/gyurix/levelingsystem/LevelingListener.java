package gyurix.levelingsystem;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LevelingListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player plr = e.getPlayer();
        plr.setScoreboard(LevelingAPI.scoreboard);
        LevelingAPI.loadPlayer(plr, (pd) -> {
            LevelingAPI.createScoreboard(pd);
            LevelingAPI.data.put(plr.getUniqueId(),pd);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        LevelingAPI.unloadPlayer(plr);
    }
}
