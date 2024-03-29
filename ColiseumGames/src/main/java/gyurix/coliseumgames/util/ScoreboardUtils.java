package gyurix.coliseumgames.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.List;

import static gyurix.coliseumgames.conf.ConfigManager.conf;
import static gyurix.coliseumgames.util.StrUtils.fillVariables;
import static gyurix.coliseumgames.util.StrUtils.specialSplit;

public class ScoreboardUtils {
    public static Scoreboard createScoreboard(String title, List<String> lines, Object... vars) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective hpObjective = scoreboard.registerNewObjective("hp", "health", conf.getHealthSuffix(), RenderType.HEARTS);
        hpObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        Objective objective = scoreboard.registerNewObjective("coliseumgames", "coliseumgames", title, RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < lines.size(); ++i) {
            String pln = "§" + ((char) (4400 + i));
            Score line = objective.getScore(pln);
            line.setScore(15 - i);
            Team team = scoreboard.registerNewTeam(pln);
            team.addEntry(pln);
            String[] prefixSuffix = specialSplit(fillVariables(lines.get(i), vars));
            team.setPrefix(prefixSuffix[0]);
            team.setSuffix(prefixSuffix[1]);
        }
        Team team1 = scoreboard.registerNewTeam("team1");
        team1.setColor(ChatColor.AQUA);
        Team team2 = scoreboard.registerNewTeam("team2");
        team2.setColor(ChatColor.RED);
        return scoreboard;
    }

    public static void updateScoreboard(Scoreboard scoreboard, List<String> lines, Object... vars) {
        Objective objective = scoreboard.getObjective("coliseumgames");
        int teamCount = scoreboard.getTeams().size() - 2;
        for (int i = 0; i < lines.size(); ++i) {
            String pln = "§" + ((char) (4400 + i));
            String[] prefixSuffix = specialSplit(fillVariables(lines.get(i), vars));
            Team team;
            if (i >= teamCount) {
                Score line = objective.getScore(pln);
                line.setScore(15 - i);
                team = scoreboard.registerNewTeam(pln);
                team.addEntry(pln);
            } else {
                team = scoreboard.getTeam(pln);
            }
            if (prefixSuffix[0].equals(team.getPrefix()) && prefixSuffix[1].equals(team.getSuffix()))
                continue;
            team.setPrefix(prefixSuffix[0]);
            team.setSuffix(prefixSuffix[1]);
        }
        for (int i = lines.size(); i < 15; ++i) {
            if (i >= teamCount)
                return;
            String pln = "§" + ((char) (4400 + i));
            scoreboard.getTeam(pln).unregister();
            objective.getScore(pln).resetScore();
        }
    }
}
