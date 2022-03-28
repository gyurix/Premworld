package gyurix.levelingsystem.cmd;

import gyurix.levelingsystem.LevelingSystem;
import gyurix.levelingsystem.gui.LeaderboardGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static gyurix.levelingsystem.conf.ConfigManager.msg;

public class CommandLeaderboard implements CommandExecutor {
    public CommandLeaderboard() {
        PluginCommand cmd = LevelingSystem.pl.getCommand("leaderboard");
        cmd.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (!(sender instanceof Player plr)) {
            msg.msg(sender, "noconsole");
            return true;
        }
        new LeaderboardGUI(plr);
        return true;
    }
}
