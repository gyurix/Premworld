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
        boolean secondTeam = game.getTeam2().containsKey(plr.getName());
        PlayerData pd = (secondTeam ? game.getTeam2() : game.getTeam1()).get(plr.getName());
        Arena arena = game.getArena();
        if (pd == null)
            return !arena.getSpec().contains(to);
        switch (game.getState()) {
            case WAITING, STARTING -> {
                return !arena.getQueue().contains(to);
            }
            case INARENA -> {
                return !(secondTeam ? arena.getTeam2() : arena.getTeam1()).contains(to);
            }
            case INGAME -> {
                Area area = arena.getArea();
                return !area.contains(to);
            }
            case FINISH -> {
                Area area = arena.getArea();
                return area.contains(to);
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
