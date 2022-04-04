package gyurix.shopsystem.conf;

import gyurix.shopsystem.data.TicketSettings;
import gyurix.shopsystem.gui.ShopGUIConfig;

import java.util.HashMap;

public class Config {
    public HashMap<String, ShopGUIConfig> shops;
    public HashMap<String, TicketSettings> tickets;
    public String expirationFormat;
}
