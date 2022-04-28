package gyurix.timedtrials;

import gyurix.timedtrials.conf.ConfigManager;
import gyurix.timedtrials.data.Game;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

import static gyurix.timedtrials.TTAPI.games;

public class TTPlugin extends JavaPlugin {
    public static TTPlugin pl;

    @Override
    public void onDisable() {
        new ArrayList<>(games).forEach(Game::forceStop);
    }

    @Override
    public void onEnable() {
        pl = this;
        ConfigManager.reload();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> new ArrayList<>(games).forEach(Game::tick), 20, 20);
        Bukkit.getPluginManager().registerEvents(new TTListener(), this);
        new CommandTrials();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new MoveDetector(), 5, 5);
    }
}
