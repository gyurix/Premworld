package gyurix.coliseumgames;

import gyurix.coliseumgames.conf.ConfigManager;
import gyurix.coliseumgames.data.Game;
import gyurix.coliseumgames.gui.GUIListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CGPlugin extends JavaPlugin {
    public static CGPlugin pl;

    @Override
    public void onDisable() {
        new ArrayList<>(CGAPI.games).forEach(Game::forceStop);
    }

    @Override
    public void onEnable() {
        pl = this;
        ConfigManager.reload();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> new ArrayList<>(CGAPI.games).forEach(Game::tick), 20, 20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> new ArrayList<>(CGAPI.games).forEach(Game::fastTick), 1, 1);
        Bukkit.getPluginManager().registerEvents(new CGListener(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        new CommandColiseum();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new MoveDetector(), 5, 5);
    }
}
