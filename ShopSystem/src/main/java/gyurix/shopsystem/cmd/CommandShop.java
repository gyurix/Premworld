package gyurix.shopsystem.cmd;

import gyurix.shopsystem.ShopSystem;
import gyurix.shopsystem.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.conf.ConfigManager.msg;

@SuppressWarnings("NullableProblems")
public class CommandShop implements CommandExecutor {
    public CommandShop() {
        PluginCommand cmd = ShopSystem.pl.getCommand("shop");
        cmd.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player plr)) {
            msg.msg(sender, "noconsole");
            return true;
        }
        new ShopGUI(plr, conf.shops.get("main"));
        return true;
    }
}
