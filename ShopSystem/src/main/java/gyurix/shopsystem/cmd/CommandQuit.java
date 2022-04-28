package gyurix.shopsystem.cmd;

import gyurix.coliseumgames.CGAPI;
import gyurix.coliseumgames.data.Game;
import gyurix.huntinggames.HGAPI;
import gyurix.shopsystem.ShopSystem;
import gyurix.shopsystem.gui.JoinGUI;
import gyurix.timedtrials.TTAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import static gyurix.shopsystem.conf.ConfigManager.msg;

@SuppressWarnings("NullableProblems")
public class CommandQuit implements CommandExecutor {
    public CommandQuit() {
        PluginCommand cmd = ShopSystem.pl.getCommand("quit");
        cmd.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player plr)) {
            msg.msg(sender, "noconsole");
            return true;
        }
        msg.msg(plr, quit(plr) ? "quit" : "notin");
        return true;
    }

    private boolean quit(Player plr) {
        Game coliseum = CGAPI.playerGames.get(plr.getName());
        if (coliseum != null) {
            coliseum.quit(plr);
            return true;
        }
        gyurix.huntinggames.data.Game hunt = HGAPI.playerGames.get(plr.getName());
        if (hunt != null) {
            hunt.quit(plr);
            return true;
        }
        gyurix.timedtrials.data.Game trials = TTAPI.playerGames.get(plr.getName());
        if (trials != null) {
            trials.quit(plr);
            return true;
        }
        return false;
    }
}
