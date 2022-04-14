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
        String pln = plr.getName();
        Game game = CGAPI.playerGames.get(pln);
        if (game == null)
            return false;
        boolean secondTeam = game.getTeam2().containsKey(pln);
        PlayerData pd = (secondTeam ? game.getTeam2() : game.getTeam1()).get(pln);
        Arena arena = game.getArena();
        Area area = arena.getArea();
        if (pd == null)
            return !area.contains(to) && !arena.getSpec().contains(to);
        switch (game.getState()) {
            case WAITING, STARTING -> {
                return !arena.getQueue().contains(to);
            }
            case INARENA -> {
                return !(secondTeam ? arena.getTeam2() : arena.getTeam1()).contains(to);
            }
            case INGAME, FINISH -> {
                if (pln.equals(game.getTeam1Carrier()) && arena.getTeam1().contains(to)) {
                    game.depositFlag(plr,false);
                } else if (pln.equals(game.getTeam2Carrier()) && arena.getTeam2().contains(to)) {
                    game.depositFlag(plr,true);
                }
                return !area.contains(to);
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
