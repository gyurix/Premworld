package gyurix.shopsystem.cmd;

import gyurix.shopsystem.ShopSystem;
import gyurix.shopsystem.gui.JoinGUI;
import gyurix.shopsystem.gui.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.conf.ConfigManager.msg;

@SuppressWarnings("NullableProblems")
public class CommandJoin implements CommandExecutor {
    public CommandJoin() {
        PluginCommand cmd = ShopSystem.pl.getCommand("join");
        cmd.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player plr)) {
            msg.msg(sender, "noconsole");
            return true;
        }
        new JoinGUI(plr);
        return true;
    }
}
