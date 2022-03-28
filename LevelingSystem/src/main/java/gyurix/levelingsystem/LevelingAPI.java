package gyurix.levelingsystem;

import gyurix.levelingsystem.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

import static gyurix.levelingsystem.LevelingSystem.pl;
import static gyurix.levelingsystem.conf.ConfigManager.conf;

public class LevelingAPI {
    public static ConcurrentHashMap<UUID, PlayerData> data = new ConcurrentHashMap<>();
    public static Objective objective;
    public static Scoreboard scoreboard;
    public static ConcurrentSkipListSet<PlayerData> toSave = new ConcurrentSkipListSet<>();
    public static List<PlayerData> top = new ArrayList<>();

    public static void loadPlayer(OfflinePlayer plr, Consumer<PlayerData> con) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            conf.mySQL.query("SELECT `level`, `exp` FROM `" + conf.mySQL.table + "` WHERE `uuid` = ? LIMIT 1", (rs) -> {
                if (rs.next()) {
                    PlayerData pd = new PlayerData(plr.getUniqueId(), rs.getInt(1), rs.getInt(2));
                    con.accept(pd);
                    return;
                }
                PlayerData pd = new PlayerData(plr);
                pd.insert();
                con.accept(pd);
            }, plr.getUniqueId());
        });
    }

    public static void withPlayer(OfflinePlayer plr, Consumer<PlayerData> con) {
        PlayerData pd = data.get(plr.getUniqueId());
        if (pd != null) {
            con.accept(pd);
            return;
        }
        loadPlayer(plr, con);
    }

    public static void refreshLeaderboard() {
        conf.mySQL.query("SELECT * FROM `" + conf.mySQL.table + "` ORDER BY `exp` DESC LIMIT 10", (rs) -> {
            TreeSet<PlayerData> players = new TreeSet<>();
            while (rs.next()) {
                players.add(new PlayerData(UUID.fromString(rs.getString(1)), rs.getInt(2), rs.getInt(3)));
            }
            players.addAll(data.values());
            top = new ArrayList<>(players).subList(0, Math.min(players.size(), 10));
        });
    }


    public static void saveAll() {
        ConcurrentSkipListSet<PlayerData> saveable = toSave;
        toSave = new ConcurrentSkipListSet<>();
        for (PlayerData pd : saveable) {
            pd.saveNow();
        }
    }

    public static void unloadPlayer(Player plr) {
        objective.getScore(plr.getName()).resetScore();
        data.remove(plr.getUniqueId());
    }
}
