package gyurix.shopsystem.conf;

import gyurix.shopsystem.data.TicketSettings;
import gyurix.shopsystem.gui.JoinGUI;
import gyurix.shopsystem.gui.JoinGUIConfig;
import gyurix.shopsystem.gui.ShopGUIConfig;

import java.util.HashMap;
import java.util.HashSet;

public class Config {
    public HashSet<String> allowedCommands;
    public String expirationFormat;
    public JoinGUIConfig joinGUI;
    public HashMap<String, ShopGUIConfig> shops;
    public HashMap<String, TicketSettings> tickets;
}
