package gyurix.shopsystem;

import gyurix.coliseumgames.CGAPI;
import gyurix.huntinggames.HGAPI;
import gyurix.timedtrials.TTAPI;

public class ShopAPI {

    public static int getPlayers(String gameMode) {
        return switch (gameMode) {
            case "hunting" -> HGAPI.playerGames.size();
            case "trials" -> TTAPI.playerGames.size();
            default ->
                    (int) CGAPI.playerGames.values().stream().filter(game -> game.getArena().getType().equals(gameMode)).count();
        };
    }
}
