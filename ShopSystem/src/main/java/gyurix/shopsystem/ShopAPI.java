package gyurix.shopsystem;

import gyurix.coliseumgames.CGAPI;
import gyurix.huntinggames.HGAPI;
import gyurix.timedtrials.TTAPI;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static gyurix.shopsystem.conf.ConfigManager.conf;

public class ShopAPI {
    public static void activateUpgrade(Player plr, String upgrade) {
        PlayerManager.withPlayerData(plr.getUniqueId(), (pd) -> {
            pd.addBoughtItem(upgrade, System.currentTimeMillis() + conf.upgradeExpiration * 1000L);
        });
    }

    public static void hasUpgrade(Player plr, String upgrade, Consumer<Boolean> resultHandler) {
        PlayerManager.withPlayerData(plr.getUniqueId(), (pd) -> {
            long expiration = pd.getBoughtItems().getOrDefault(upgrade, 0L);
            resultHandler.accept(expiration > System.currentTimeMillis());
        });
    }

    public static int getPlayers(String gameMode) {
        return switch (gameMode) {
            case "hunting" -> HGAPI.playerGames.size();
            case "trials" -> TTAPI.playerGames.size();
            default ->
                    (int) CGAPI.playerGames.values().stream().filter(game -> game.getArena().getType().equals(gameMode)).count();
        };
    }
}
