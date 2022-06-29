package gyurix.huntinggames.data;

import gyurix.huntinggames.util.StrUtils;
import gyurix.shopsystem.ShopAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static gyurix.huntinggames.conf.ConfigManager.conf;
import static gyurix.huntinggames.conf.ConfigManager.msg;
import static gyurix.huntinggames.util.StrUtils.rand;

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
    private final BossBar trapBar, lureBar;
    private final LinkedHashSet<String> upgrades = new LinkedHashSet<>();
    private final int xp;
    private int points;
    private int targets;
    private long trapReset, lureReset, shotgunReset, rifleReset;

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
        upgrades.addAll(conf.getDefaultUpgrades());
        conf.getUpgrades().values().forEach((upgrade) ->
                ShopAPI.hasUpgrade(plr, upgrade.getName(), (result) -> {
                    if (result)
                        upgrades.add(upgrade.getName());
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

        trapBar = createBar(plr, conf.getUpgrades().get("trap"));
        lureBar = createBar(plr, conf.getUpgrades().get("lure"));
    }

    public void addPoints(Mob mob) {
        int pointsToAdd = mob.getPoints();
        points += pointsToAdd;
        ++targets;
        Player plr = getPlayer();
        if (pointsToAdd > 1) {
            plr.sendTitle(msg.get("points.title", "amount", pointsToAdd, "mob", mob.getName())
                    , msg.get("points.subtitle", "amount", pointsToAdd, "mob", mob.getName()), conf.getTitleFadeIn(), conf.getTitleShowTime(), conf.getTitleFadeOut());
            plr.sendActionBar(msg.get("points.actionbar", "amount", pointsToAdd, "mob", mob.getName()));
        } else {
            plr.sendTitle(msg.get("point.title", "amount", pointsToAdd, "mob", mob.getName())
                    , msg.get("point.subtitle", "amount", pointsToAdd, "mob", mob.getName()), conf.getTitleFadeIn(), conf.getTitleShowTime(), conf.getTitleFadeOut());
            plr.sendActionBar(msg.get("point.actionbar", "amount", pointsToAdd, "mob", mob.getName()));
        }
    }

    private BossBar createBar(Player plr, Upgrade upgrade) {
        BossBar bar = Bukkit.createBossBar(StrUtils.colorize(upgrade.getBarText()), upgrade.getBarColor(), BarStyle.SOLID);
        bar.setProgress(0);
        bar.setVisible(false);
        bar.addPlayer(plr);
        return bar;
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(name);
    }

    public void reset(Player plr) {
        trapBar.removeAll();
        lureBar.removeAll();
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
        long time = System.currentTimeMillis();
        updateBossBar(time, trapReset, trapBar, conf.getUpgrades().get("trap").getDelay());
        updateBossBar(time, lureReset, lureBar, conf.getUpgrades().get("lure").getDelay());
    }

    public void useLure() {
        long time = System.currentTimeMillis();
        if (lureReset > time)
            return;
        Upgrade settings = conf.getUpgrades().get("lure");
        Player plr = getPlayer();
        Location to = plr.getLocation().add(plr.getLocation().getDirection().multiply(3));
        if (!game.getArena().getArea().contains(to)) {
            msg.msg(plr, "outside");
            return;
        }
        int count = 0;
        for (LivingEntity ent : plr.getWorld().getLivingEntities()) {
            if (ent instanceof Player)
                continue;
            if (ent.getLocation().distance(plr.getLocation()) < settings.getMaxDist()) {
                Location idealLoc = to.clone().add(rand.nextDouble() * 3 - 1.5, 0, rand.nextDouble() * 3 - 1.5);
                if (!game.getArena().getArea().contains(idealLoc))
                    idealLoc = to;
                ent.teleport(idealLoc);
                ++count;
            }
        }
        if (count == 0) {
            msg.msg(plr, "lure0");
            return;
        }
        lureReset = time + settings.getDelay();
        msg.msg(plr, count > 1 ? "lure" : "lure1", "count", count);
    }

    public void useRifle() {
        long time = System.currentTimeMillis();
        if (rifleReset > time)
            return;
        Upgrade settings = conf.getUpgrades().get("rifle");
        rifleReset = time + settings.getDelay();
        Player plr = getPlayer();
        Snowball ball = getPlayer().launchProjectile(Snowball.class);
        ball.setVelocity(ball.getVelocity().multiply(3));
        ball.setCustomName("rifle");
        plr.playSound(plr.getLocation(), Sound.valueOf(conf.getRifleSound()), 1, 1);
    }

    public void useShotgun() {
        long time = System.currentTimeMillis();
        if (shotgunReset > time)
            return;
        Upgrade settings = conf.getUpgrades().get("shotgun");
        shotgunReset = time + settings.getDelay();
        Player plr = getPlayer();
        plr.playSound(plr.getLocation(), Sound.valueOf(conf.getShotgunSound()), 1, 1);
        Location shotPoint = plr.getLocation().add(plr.getLocation().getDirection());
        for (LivingEntity ent : plr.getWorld().getLivingEntities()) {
            if (ent instanceof Player)
                continue;
            double dist = ent.getLocation().distance(shotPoint);
            if (dist > settings.getMaxDist())
                continue;
            if (dist <= settings.getMinDist()) {
                game.damageMob(plr, ent, settings.getDamage());
                continue;
            }
            double damage = 1 - (dist - settings.getMinDist()) / (settings.getMaxDist() - settings.getMinDist());
            damage = settings.getMinDamage() + damage * (settings.getDamage() - settings.getMinDamage());
            game.damageMob(plr, ent, damage);
        }
    }

    public void useTrap() {
        long time = System.currentTimeMillis();
        if (trapReset > time)
            return;
        Upgrade settings = conf.getUpgrades().get("trap");
        Player plr = getPlayer();
        Block target = plr.getTargetBlock(6);
        if (target == null) {
            msg.msg(plr, "trap.notlooking");
            return;
        }
        target = target.getRelative(BlockFace.UP);
        if (target.getType() != Material.AIR) {
            msg.msg(plr, "trap.notair");
            return;
        }
        Loc loc = new Loc(target);
        if (!game.getArena().getArea().contains(loc)) {
            msg.msg(plr, "outside");
            return;
        }
        msg.msg(plr, "trap.place");
        trapReset = time + settings.getDelay();
        target.setType(Material.NETHER_PORTAL);
        game.getPortalOwners().put(loc, name);
    }
}
