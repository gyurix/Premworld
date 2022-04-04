package gyurix.coliseumgames;

import gyurix.coliseumgames.data.Area;
import gyurix.coliseumgames.data.Arena;
import gyurix.coliseumgames.data.Game;
import gyurix.coliseumgames.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class MoveDetector implements Runnable {
    public HashMap<String, Location> oldLoc = new HashMap<>();

    public boolean cancelMove(Player plr, Location to) {
        Game game = CGAPI.playerGames.get(plr.getName());
        if (game == null)
            return false;
        PlayerData pd = game.getPlayers().get(plr.getName());
        Arena arena = game.getArena();
        switch (game.getState()) {
            case WAITING, STARTING -> {
                return !arena.getQueue().contains(to);
            }
            case INARENA -> {
                return !arena.getStart().contains(to);
            }
            case INGAME -> {
                if (pd == null)
                    return !arena.getSpec().contains(to);
                Area area = arena.getArea();
                Area finish = arena.getFinish();
                if (finish.contains(to))
                    game.finish();
                return !(area.contains(to) || finish.contains(to));
            }
            case FINISH -> {
                if (pd == null)
                    return !arena.getSpec().contains(to);
                Area area = arena.getArea();
                Area finish = arena.getFinish();
                return !(area.contains(to) || finish.contains(to));
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
            if (old.distance(cur) < 0.5)
                continue;
            if (cancelMove(p, cur)) {
                p.teleport(old);
                continue;
            }
            oldLoc.put(pln, cur);
        }
    }
}
