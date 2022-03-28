package gyurix.levelingsystem.cmd;

import gyurix.levelingsystem.LevelingAPI;
import gyurix.levelingsystem.LevelingSystem;
import gyurix.levelingsystem.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import static gyurix.levelingsystem.conf.ConfigManager.msg;
import static gyurix.levelingsystem.util.StrUtils.DF;

@SuppressWarnings("NullableProblems")
public class CommandLevel implements CommandExecutor {
    public CommandLevel() {
        PluginCommand cmd = LevelingSystem.pl.getCommand("level");
        cmd.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player plr)) {
                msg.msg(sender, "noplayer");
                return true;
            }
            PlayerData pd = LevelingAPI.data.get(plr.getUniqueId());
            pd.withPosition(pos ->
                msg.msg(plr, "info",
                    "player", pd.getName(),
                    "exp", pd.getExp(),
                    "level", pd.getLevel(),
                    "nextLevelExp", pd.getNextLevelExp(),
                    "progress", DF.format(pd.getProgress()),
                    "position", pos));
            return true;
        }
        OfflinePlayer target = Bukkit.getPlayer(args[0]);
        if (target == null)
            target = Bukkit.getOfflinePlayer(args[0]);

        if (!target.hasPlayedBefore()) {
            msg.msg(sender, "wrongplayer", "player", args[0]);
            return true;
        }
        LevelingAPI.withPlayer(target, (pd) ->
            pd.withPosition(pos ->
                msg.msg(sender, "info",
                    "player", pd.getName(),
                    "exp", pd.getExp(),
                    "level", pd.getLevel(),
                    "nextLevelExp", pd.getNextLevelExp(),
                    "progress", DF.format(pd.getProgress()),
                    "position", pos)));
        return true;
    }
}
