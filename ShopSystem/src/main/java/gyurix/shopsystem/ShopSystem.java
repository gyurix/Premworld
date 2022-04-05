package gyurix.shopsystem;

import gyurix.shopsystem.cmd.CommandShop;
import gyurix.shopsystem.conf.ConfigManager;
import gyurix.shopsystem.gui.GUIListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopSystem extends JavaPlugin {
    public static ShopSystem pl;

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        pl = this;
        ConfigManager.reload();
        registerCommands();
        registerTasks();
    }

    private void registerCommands() {
        new CommandShop();
    }

    private void registerTasks() {
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
    }
}
