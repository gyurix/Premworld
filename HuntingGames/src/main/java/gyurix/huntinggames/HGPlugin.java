package gyurix.huntinggames;

import gyurix.huntinggames.conf.ConfigManager;
import gyurix.huntinggames.data.Game;
import gyurix.huntinggames.gui.GUIListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class HGPlugin extends JavaPlugin {
    public static HGPlugin pl;

    @Override
    public void onDisable() {
        new ArrayList<>(HGAPI.games).forEach(Game::forceStop);
    }

    @Override
    public void onEnable() {
        pl = this;
        ConfigManager.reload();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> new ArrayList<>(HGAPI.games).forEach(Game::tick), 20, 20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> new ArrayList<>(HGAPI.games).forEach(Game::fastTick), 1, 1);
        Bukkit.getPluginManager().registerEvents(new HGListener(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        new CommandHunt();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new MoveDetector(), 5, 5);
    }
}
