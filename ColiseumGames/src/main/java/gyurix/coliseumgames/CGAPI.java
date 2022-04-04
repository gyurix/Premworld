package gyurix.coliseumgames;

import gyurix.coliseumgames.conf.ConfigManager;
import gyurix.coliseumgames.data.Arena;
import gyurix.coliseumgames.data.Game;
import org.bukkit.entity.Player;

import java.util.*;

import static gyurix.coliseumgames.conf.ConfigManager.arenas;
import static gyurix.coliseumgames.conf.ConfigManager.msg;

public class CGAPI {
    public static List<Game> games = new ArrayList<>();
    public static Map<String, Game> playerGames = new HashMap<>();

    public static boolean queue(Player plr, String mode) {
        for (Game g : games) {
            if (g.getArena().getType().equals(mode) && g.join(plr))
                return true;
        }
        if (arenas.isEmpty()) {
            msg.msg(plr, "arena.noconfigured");
            return false;
        }
        List<Arena> arenas = new ArrayList<>(ConfigManager.arenas.values());
        for (Game g : games) {
            arenas.remove(g.getArena());
        }
        Collections.shuffle(arenas);
        for (Arena arena : arenas) {
            if (!arena.isConfigured())
                continue;
            Game game = new Game(arena);
            game.join(plr);
            return true;
        }
        msg.msg(plr, "arena.allinuse");
        return false;
    }
}
