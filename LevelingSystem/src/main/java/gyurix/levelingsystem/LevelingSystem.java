package gyurix.levelingsystem;

import gyurix.levelingsystem.cmd.CommandLeaderboard;
import gyurix.levelingsystem.cmd.CommandLevel;
import gyurix.levelingsystem.cmd.CommandLevelA;
import gyurix.levelingsystem.conf.ConfigManager;
import gyurix.levelingsystem.data.PlayerData;
import gyurix.levelingsystem.gui.GUIListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;

import static gyurix.levelingsystem.LevelingAPI.objective;
import static gyurix.levelingsystem.LevelingAPI.scoreboard;
import static gyurix.levelingsystem.conf.ConfigManager.conf;

public class LevelingSystem extends JavaPlugin {
    public static LevelingSystem pl;

    @Override
    public void onDisable() {
        LevelingAPI.toSave.forEach(PlayerData::saveNow);
    }

    @Override
    public void onEnable() {
        pl = this;
        ConfigManager.reload();
        registerCommands();
        registerTasks();
        registerScoreboard();
    }

    private void registerCommands() {
        new CommandLevel();
        new CommandLevelA();
        new CommandLeaderboard();
    }

    private void registerScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("levelingexp", "levelingexp", conf.expSuffix, RenderType.INTEGER);
        //objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        Bukkit.getOnlinePlayers().forEach(plr -> {
            plr.setScoreboard(scoreboard);
            LevelingAPI.loadPlayer(plr, (pd) -> {
                LevelingAPI.createScoreboard(pd);
                LevelingAPI.data.put(plr.getUniqueId(), pd);
            });
        });
    }

    private void registerTasks() {
        Bukkit.getPluginManager().registerEvents(new LevelingListener(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);

        BukkitScheduler sch = Bukkit.getScheduler();
        sch.runTaskAsynchronously(pl,
            () -> conf.mySQL.command("CREATE TABLE IF NOT EXISTS `" + conf.mySQL.table + "` (`uuid` CHAR(40) PRIMARY KEY, `level` INT, `exp` INT)"));
        sch.runTaskTimerAsynchronously(pl, LevelingAPI::refreshLeaderboard, 5L, conf.leaderboardUpdateTicks);
        sch.runTaskTimerAsynchronously(pl, LevelingAPI::saveAll, 20L * conf.dbSaveSeconds, 20L * conf.dbSaveSeconds);
    }
}
