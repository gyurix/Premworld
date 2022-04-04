package gyurix.coliseumgames.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

import static gyurix.coliseumgames.conf.ConfigManager.conf;

@Getter
public class PlayerData {
    private final GameMode gameMode;
    private final List<ItemStack> items = new ArrayList<>();
    private final Location loc;
    private final String name;
    private final int xp;
    private final int level;
    private final double health;
    private final double maxHealth;
    private final boolean flying;
    private final boolean allowFlight;
    private final int food;
    private final float saturation;

    public PlayerData(Player plr, Location loc) {
        this.name = plr.getName();

        this.allowFlight = plr.getAllowFlight();
        this.flying = plr.isFlying();
        this.gameMode = plr.getGameMode();
        this.level = plr.getLevel();
        this.loc = plr.getLocation();
        this.health = plr.getHealth();
        this.maxHealth = plr.getMaxHealth();
        this.xp = plr.getTotalExperience();
        this.food = plr.getFoodLevel();
        this.saturation = plr.getSaturation();

        saveAndClearInv(plr);

        plr.setGameMode(GameMode.ADVENTURE);
        plr.setTotalExperience(0);
        plr.setLevel(0);
        plr.setAllowFlight(false);
        plr.setFlying(false);
        plr.setFoodLevel(20);
        plr.setSaturation(100);
        plr.teleport(loc);
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(name);
    }

    public void giveItems() {
        Player plr = getPlayer();
        PlayerInventory pi = plr.getInventory();
        for (int i = 0; i < conf.getIngameItems().size(); ++i) {
            pi.setItem(i, conf.getIngameItems().get(i));
        }
    }

    public void reset(Player plr) {
        plr.teleport(loc);
        plr.setGameMode(gameMode);
        plr.setTotalExperience(xp);
        plr.setLevel(level);
        plr.setAllowFlight(allowFlight);
        plr.setFlying(flying);
        plr.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        plr.setMaxHealth(maxHealth);
        plr.setHealth(health);
        plr.setFoodLevel(food);
        plr.setSaturation(saturation);
        resetInv(plr);
    }

    public void resetInv(Player plr) {
        PlayerInventory inv = plr.getInventory();
        for (int i = 0; i < 41; ++i) {
            inv.setItem(i, items.get(i));
        }
    }

    public void saveAndClearInv(Player plr) {
        PlayerInventory inv = plr.getInventory();
        for (int i = 0; i < 41; ++i) {
            items.add(inv.getItem(i));
            inv.setItem(i, null);
        }
    }

    public void updateBossBars() {

    }

    private BossBar createBar(Player plr, String title, BarColor color) {
        BossBar bar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        bar.setProgress(0);
        bar.setVisible(false);
        bar.addPlayer(plr);
        return bar;
    }

    private void updateBossBar(long time, long until, BossBar bar, int duration) {
        bar.setProgress(Math.max(0, (until - time) * 1.0 / duration));
        bar.setVisible(bar.getProgress() > 0);
    }
}
