package gyurix.levelingsystem.conf;

import gyurix.levelingsystem.db.MySQLDatabase;
import gyurix.levelingsystem.gui.GUIConfig;
import gyurix.levelingsystem.util.StrUtils;

import java.util.List;

public class Config implements PostProcessable {
    public String levelPrefix, levelSuffix, expSuffix;
    public int dbSaveSeconds, leaderboardUpdateTicks;
    public MySQLDatabase mySQL;
    public GUIConfig leaderBoardGUI;
    public List<Integer> levelExp;

    @Override
    public void postProcess() {
        levelPrefix = StrUtils.colorize(levelPrefix);
        levelSuffix = StrUtils.colorize(levelSuffix);
        expSuffix = StrUtils.colorize(expSuffix);
    }
}
