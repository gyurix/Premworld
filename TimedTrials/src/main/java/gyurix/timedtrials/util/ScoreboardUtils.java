package gyurix.timedtrials.util;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

import static gyurix.timedtrials.util.StrUtils.fillVariables;


public class ScoreboardUtils {
    public static Scoreboard createScoreboard(String title, List<String> lines, Object... vars) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("hunt", "hunt", title, RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < lines.size(); ++i) {
            String pln = "§" + ((char) (4400 + i));
            Score line = objective.getScore(pln);
            line.setScore(15 - i);
            Team team = scoreboard.registerNewTeam(pln);
            team.addEntry(pln);
            String[] prefixSuffix = StrUtils.specialSplit(StrUtils.fillVariables(lines.get(i), vars));
            team.setPrefix(prefixSuffix[0]);
            team.setSuffix(prefixSuffix[1]);
        }
        return scoreboard;
    }

    public static void updateScoreboard(Scoreboard scoreboard, List<String> lines, Object... vars) {
        Objective objective = scoreboard.getObjective("hunt");
        int teamCount = scoreboard.getTeams().size();
        for (int i = 0; i < lines.size(); ++i) {
            String pln = "§" + ((char) (4400 + i));
            String[] prefixSuffix = StrUtils.specialSplit(StrUtils.fillVariables(lines.get(i), vars));
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
