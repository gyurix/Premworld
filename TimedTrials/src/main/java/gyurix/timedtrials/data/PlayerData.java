package gyurix.timedtrials.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

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

    public double getDistance() {
        return game.getArena().getFinish().distance(getPlayer().getLocation());
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
}
