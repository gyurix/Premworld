package gyurix.levelingsystem;

import gyurix.levelingsystem.data.PlayerData;
import gyurix.levelingsystem.gui.CustomGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

import static gyurix.levelingsystem.LevelingSystem.pl;
import static gyurix.levelingsystem.conf.ConfigManager.conf;
import static gyurix.levelingsystem.util.StrUtils.DF;

public class LevelingAPI {
    public static ConcurrentHashMap<UUID, PlayerData> data = new ConcurrentHashMap<>();
    public static Objective objective;
    public static Scoreboard scoreboard;
    public static ConcurrentSkipListSet<PlayerData> toSave = new ConcurrentSkipListSet<>();
    public static List<PlayerData> top = new ArrayList<>();

    public static void createScoreboard(PlayerData pd) {
        Player p = Bukkit.getPlayer(pd.getUuid());
        if (p == null)
            return;
        Team team = scoreboard.registerNewTeam(pd.getName());
        team.addEntry(pd.getName());
        team.setPrefix(conf.levelPrefix.replace("<level>", DF.format(pd.getLevel())));
        team.setSuffix(conf.levelSuffix.replace("<level>", DF.format(pd.getLevel())));
        objective.getScore(pd.getName()).setScore((int) pd.getExp());
    }

    public static void updateScoreboard(PlayerData pd) {
        Player p = Bukkit.getPlayer(pd.getUuid());
        if (p == null)
            return;
        Team team = scoreboard.getTeam(pd.getName());
        team.setPrefix(conf.levelPrefix.replace("<level>", DF.format(pd.getLevel())));
        team.setSuffix(conf.levelSuffix.replace("<level>", DF.format(pd.getLevel())));
        objective.getScore(pd.getName()).setScore((int) pd.getExp());
    }

    public static void deleteScoreboard(PlayerData pd) {
        objective.getScore(pd.getName()).resetScore();
        scoreboard.getTeam(pd.getName()).unregister();
    }

    public static void loadPlayer(OfflinePlayer plr, Consumer<PlayerData> con) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            conf.mySQL.query("SELECT `level`, `exp` FROM `" + conf.mySQL.table + "` WHERE `uuid` = ? LIMIT 1", (rs) -> {
                if (rs.next()) {
                    PlayerData pd = new PlayerData(plr.getUniqueId(), rs.getInt(1), rs.getInt(2));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> con.accept(pd));
                    return;
                }
                PlayerData pd = new PlayerData(plr);
                pd.insert();
                Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> con.accept(pd));
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
        saveAll();
        conf.mySQL.query("SELECT * FROM `" + conf.mySQL.table + "` ORDER BY `exp` DESC LIMIT 10", (rs) -> {
            TreeSet<PlayerData> players = new TreeSet<>();
            while (rs.next()) {
                players.add(new PlayerData(UUID.fromString(rs.getString(1)), rs.getInt(2), rs.getInt(3)));
            }
            top = new ArrayList<>(players).subList(0, Math.min(players.size(), 10));
            Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    InventoryView view = p.getOpenInventory();
                    Inventory top = view.getTopInventory();
                    if (top.getHolder() instanceof CustomGUI) {
                        ((CustomGUI) top.getHolder()).update();
                    }
                }
            });
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
        PlayerData pd = data.remove(plr.getUniqueId());
        deleteScoreboard(pd);
    }
}
