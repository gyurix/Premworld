package gyurix.huntinggames;

import gyurix.huntinggames.conf.ConfigManager;
import gyurix.huntinggames.data.Arena;
import gyurix.huntinggames.data.Game;
import gyurix.partysystem.PartyAPI;
import gyurix.partysystem.data.Party;
import org.bukkit.entity.Player;

import java.util.*;

import static gyurix.huntinggames.conf.ConfigManager.arenas;
import static gyurix.huntinggames.conf.ConfigManager.msg;

public class HGAPI {
    public static List<Game> games = new ArrayList<>();
    public static Map<String, Game> playerGames = new HashMap<>();

    public static boolean queue(Player plr) {
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
            if (g.join(pls))
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
            game.join(pls);
            return true;
        }
        msg.msg(plr, "arena.allinuse");
        return false;
    }
}
