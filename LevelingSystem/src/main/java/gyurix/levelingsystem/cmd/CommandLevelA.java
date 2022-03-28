package gyurix.levelingsystem.cmd;

import gyurix.levelingsystem.LevelingAPI;
import gyurix.levelingsystem.LevelingSystem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import static gyurix.levelingsystem.conf.ConfigManager.msg;

@SuppressWarnings("NullableProblems")
public class CommandLevelA implements CommandExecutor {
    public CommandLevelA() {
        PluginCommand cmd = LevelingSystem.pl.getCommand("levela");
        cmd.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("leveling.admin")) {
            msg.msg(sender, "noperm");
            return true;
        }
        if (args.length == 0) {
            msg.msg(sender, "admin");
            return true;
        }
        if (args.length == 1) {
            msg.msg(sender, "noplayer");
            return true;
        }
        OfflinePlayer target = Bukkit.getPlayer(args[1]);
        if (target == null)
            target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.hasPlayedBefore()) {
            msg.msg(sender, "wrongplayer", "player", args[1]);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add", "give" -> {

                int amount = 0;
                try {
                    amount = Math.max(1, Integer.parseInt(args[2]));
                } catch (Throwable ignored) {
                }
                String reason = args.length > 3 ? StringUtils.join(args, " ", 3, args.length) :
                    msg.get("giveDefReason", "sender", sender.getName());
                int finalAmount = amount;
                LevelingAPI.withPlayer(target, (pd) -> {
                    pd.addExp(finalAmount, reason);
                    msg.msg(sender, "give", "player", pd.getName(), "amount", finalAmount, "reason", reason);
                });
                return true;
            }
            case "reset" -> {
                LevelingAPI.withPlayer(target, (pd) -> {
                    pd.reset();
                    msg.msg(sender, "reset", "player", pd.getName());
                });
                return true;
            }
        }
        return true;
    }
}
