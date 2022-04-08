package gyurix.coliseumgames;

import gyurix.coliseumgames.conf.ConfigManager;
import gyurix.coliseumgames.data.Arena;
import gyurix.coliseumgames.data.Game;
import gyurix.partysystem.PartyAPI;
import gyurix.partysystem.data.Party;
import org.bukkit.entity.Player;

import java.util.*;

import static gyurix.coliseumgames.conf.ConfigManager.arenas;
import static gyurix.coliseumgames.conf.ConfigManager.msg;

public class CGAPI {
    public static List<Game> games = new ArrayList<>();
    public static Map<String, Game> playerGames = new HashMap<>();

    public static boolean queue(Player plr, String type) {
        List<Player> pls = List.of(plr);
        Party party = PartyAPI.partiesByPlayer.get(plr.getName());
        if (party != null) {
            if (!party.getOwners().contains(plr.getName())) {
                msg.msg(plr, "party.notowner");
                return true;
            }
            pls = party.getAllPlayers();
        }
        for (Game g : games) {
            if (g.getArena().getType().equals(type) && g.join(pls))
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
            if (!arena.isConfigured() || !arena.getType().equals(type))
                continue;
            Game game = new Game(arena);
            game.join(pls);
            return true;
        }
        msg.msg(plr, "arena.allinuse");
        return false;
    }
}
