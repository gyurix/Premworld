package gyurix.levelingsystem;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static gyurix.levelingsystem.LevelingAPI.objective;

public class LevelingListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player plr = e.getPlayer();
        plr.setScoreboard(LevelingAPI.scoreboard);
        LevelingAPI.loadPlayer(plr, (pd) -> {
            objective.getScore(plr.getName()).setScore(pd.getLevel());
            LevelingAPI.data.put(plr.getUniqueId(),pd);
        });
    }

    public void onQuit(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        LevelingAPI.unloadPlayer(plr);
    }
}
