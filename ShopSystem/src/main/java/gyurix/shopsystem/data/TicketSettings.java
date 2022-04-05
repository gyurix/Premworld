package gyurix.shopsystem.data;

import gyurix.shopsystem.util.ItemUtils;
import gyurix.shopsystem.util.StrUtils;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;

import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.util.StrUtils.DF;

@Getter
public class TicketSettings {
    private String command;
    private double durationMin, price;
    private ItemStack icon;
    private ItemStack item;

    public ItemStack getIcon() {
        return ItemUtils.fillVariables(icon, "price", DF.format(price), "duration", StrUtils.formatTime((long) (durationMin * 60000L)));
    }

    public ItemStack getItem(Player plr) {
        long expiration = (long) (System.currentTimeMillis() + durationMin * 60000L);
        return ItemUtils.fillVariables(item, "owner", plr.getName(), "expiration",
            new SimpleDateFormat(conf.expirationFormat).format(expiration) + StrUtils.toInvisibleText(expiration + " " + command));
    }
}
