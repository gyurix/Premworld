package gyurix.shopsystem.conf;

import gyurix.shopsystem.db.MySQLDatabase;
import gyurix.shopsystem.gui.GUIConfig;
import gyurix.shopsystem.gui.JoinGUIConfig;

import java.util.HashSet;

public class Config {
    public HashSet<String> allowedCommands;
    public GUIConfig shop;
    public JoinGUIConfig joinGUI;
    public MySQLDatabase mySQL;
    public long upgradeExpiration;
}
