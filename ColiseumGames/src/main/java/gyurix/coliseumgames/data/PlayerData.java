package gyurix.coliseumgames.data;

import gyurix.shopsystem.ShopAPI;
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
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gyurix.coliseumgames.conf.ConfigManager.conf;

@Getter
public class PlayerData {
    private final boolean allowFlight;
    private final boolean flying;
    private final int food;
    private final Game game;
    private final GameMode gameMode;
    private final double health;
    private final List<ItemStack> items = new ArrayList<>();
    private final int level;
    private final Location loc;
    private final double maxHealth;
    private final String name;
    private final float saturation;
    private final Scoreboard scoreboard;
    private final HashMap<String, String> upgrades = new HashMap<>();
    private final int xp;

    public PlayerData(Game game, Player plr, Location loc) {
        this.name = plr.getName();
        this.game = game;

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
        this.scoreboard = plr.getScoreboard();
        game.getType().getDefaultUpgrades().forEach((upg) -> upgrades.put(conf.getUpgrades().get(upg).getType(), upg));
        conf.getUpgrades().values().forEach(upgrade ->
                ShopAPI.hasUpgrade(plr, upgrade.getName(), (result) -> {
                    if (result)
                        upgrades.put(upgrade.getType(), upgrade.getName());
                }));

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

    private BossBar createBar(Player plr, String title, BarColor color) {
        BossBar bar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        bar.setProgress(0);
        bar.setVisible(false);
        bar.addPlayer(plr);
        return bar;
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(name);
    }

    public void reset(Player plr) {
        plr.teleport(loc);
        plr.setGameMode(gameMode);
        plr.setTotalExperience(xp);
        plr.setLevel(level);
        plr.setAllowFlight(allowFlight);
        plr.setFlying(flying);
        plr.setScoreboard(scoreboard);
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
        inv.setItem(conf.getUpgradeItemSlot(), conf.getUpgradeItem());
    }

    private void updateBossBar(long time, long until, BossBar bar, int duration) {
        bar.setProgress(Math.max(0, (until - time) * 1.0 / duration));
        bar.setVisible(bar.getProgress() > 0);
    }

    public void updateBossBars() {

    }
}
