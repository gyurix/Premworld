package gyurix.partysystem;

import gyurix.partysystem.conf.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PartySystem extends JavaPlugin {
    public static PartySystem pl;

    @Override
    public void onEnable() {
        pl = this;
        ConfigManager.reload();
        Bukkit.getPluginManager().registerEvents(new PartyListener(), this);
        new CommandParty();
    }
}
