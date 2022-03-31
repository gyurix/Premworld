package gyurix.levelingsystem.data;

import gyurix.levelingsystem.LevelingAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

import static gyurix.levelingsystem.LevelingAPI.objective;
import static gyurix.levelingsystem.LevelingAPI.updateScoreboard;
import static gyurix.levelingsystem.LevelingSystem.pl;
import static gyurix.levelingsystem.conf.ConfigManager.conf;
import static gyurix.levelingsystem.conf.ConfigManager.msg;

@Getter
public class PlayerData implements Comparable<PlayerData> {
    private long exp;
    private int level;
    private String name;
    private UUID uuid;

    public PlayerData(OfflinePlayer plr) {
        name = plr.getName();
        uuid = plr.getUniqueId();
        level = 1;
    }

    public PlayerData(UUID uuid, int level, int exp) {
        this.uuid = uuid;
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
        this.level = level;
        this.exp = exp;
    }

    public void addExp(int amount, String reason) {
        exp += amount;
        withPlayer(plr -> msg.msg(plr, "receive", "amount", amount, "reason", reason));
        while (level < conf.levelExp.size() && conf.levelExp.get(level - 1) <= exp) {
            ++level;
            withPlayer(plr -> {
                msg.msg(plr, "levelUp", "level", level);
            });
        }
        updateScoreboard(this);
        LevelingAPI.toSave.add(this);
    }

    public void reset() {
        level = 1;
        exp = 0;
        LevelingAPI.toSave.add(this);
        withPlayer(plr -> objective.getScore(name).setScore(level));
    }

    public int getNextLevelExp() {
        return level >= conf.levelExp.size() ? 0 : conf.levelExp.get(level-1);
    }

    public void withPosition(Consumer<Integer> con) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            if (LevelingAPI.toSave.remove(this))
                saveNow();
            conf.mySQL.query("SELECT COUNT(*)+1 FROM `" + conf.mySQL.table + "` WHERE `exp` > " + exp, (rs) -> {
                if (rs.next())
                    con.accept(rs.getInt(1));
            });
        });
    }

    public double getProgress() {
        int nextLevelExp = getNextLevelExp();
        return nextLevelExp == 0 ? 100 : exp * 100.0 / nextLevelExp;
    }

    @Override
    public int compareTo(@NotNull PlayerData o) {
        return exp > o.exp ? -1 : exp < o.exp ? 1 : Bukkit.getOfflinePlayer(uuid).getName().compareTo(Bukkit.getOfflinePlayer(o.uuid).getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public void insert() {
        conf.mySQL.command("INSERT INTO `" + conf.mySQL.table + "` VALUES ( ?, ?, ? )", uuid, level, exp);
    }

    public void saveNow() {
        conf.mySQL.command("UPDATE `" + conf.mySQL.table + "` SET `level` = ?, `exp` = ? WHERE `uuid` = ? LIMIT 1", level, exp, uuid);
    }

    public void withPlayer(Consumer<Player> con) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null)
            con.accept(p);
    }
}
