package gyurix.timedtrials;

import gyurix.timedtrials.data.Arena;
import gyurix.timedtrials.data.Game;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class MoveDetector implements Runnable {
    public HashMap<String, Location> oldLoc = new HashMap<>();

    public boolean cancelMove(Player plr, Location to) {
        String pln = plr.getName();
        Game game = TTAPI.playerGames.get(pln);
        if (game == null)
            return false;
        Arena arena = game.getArena();
        switch (game.getState()) {
            case WAITING, STARTING -> {
                return !arena.getQueue().contains(to);
            }
            case INARENA, INGAME, FINISH -> {
                if (game.getWinner() == null && arena.getFinish().contains(to)) {
                    game.setWinner(plr);
                    game.switchToNextState();
                    return false;
                }
                return !arena.getArea().contains(to);
            }
        }
        return false;
    }

    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            String pln = p.getName();
            Location old = oldLoc.get(pln);
            Location cur = p.getLocation();
            if (old == null) {
                oldLoc.put(pln, cur);
                continue;
            }
            if (old.getWorld().equals(cur.getWorld()) && old.distance(cur) < 0.5)
                continue;
            if (cancelMove(p, cur)) {
                p.teleport(old);
                continue;
            }
            oldLoc.put(pln, cur);
        }
    }
}
