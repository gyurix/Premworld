package gyurix.levelingsystem.conf;

import gyurix.levelingsystem.db.MySQLDatabase;
import gyurix.levelingsystem.gui.GUIConfig;

import java.util.List;

public class Config {
    public String levelPrefix;
    public int dbSaveSeconds, leaderboardUpdateTicks;
    public MySQLDatabase mySQL;
    public GUIConfig leaderBoardGUI;
    public List<Integer> levelExp;
}
