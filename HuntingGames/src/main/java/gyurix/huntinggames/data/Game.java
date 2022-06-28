package gyurix.huntinggames.data;

import com.google.common.collect.Lists;
import gyurix.huntinggames.HGAPI;
import gyurix.huntinggames.enums.GameState;
import gyurix.huntinggames.util.ScoreboardUtils;
import gyurix.huntinggames.util.LocUtils;
import gyurix.levelingsystem.LevelingAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static gyurix.huntinggames.conf.ConfigManager.conf;
import static gyurix.huntinggames.conf.ConfigManager.msg;
import static gyurix.huntinggames.util.LocUtils.fixLoc;
import static gyurix.huntinggames.util.ScoreboardUtils.updateScoreboard;
import static gyurix.huntinggames.util.StrUtils.DF;
import static gyurix.huntinggames.util.StrUtils.rand;

@Getter
public class Game {
    private final Arena arena;
    private final Scoreboard board;
    private final HashMap<UUID, Mob> mobs = new HashMap<>();
    private final HashMap<String, PlayerData> players = new HashMap<>();
    private final HashMap<Loc, String> portalOwners = new HashMap<>();
    private int counter = 0;
    private GameState state = GameState.WAITING;
    private PlayerData winner;

    public Game(Arena arena) {
        this.arena = arena;
        arena.getArea().clearEntities();

        board = ScoreboardUtils.createScoreboard(
                msg.get("scoreboard.title"),
                msg.getList("scoreboard.waiting"),
                "players", "0",
                "maxplayers", conf.getMaxPlayers(),
                "needed", conf.getMinPlayers());
        HGAPI.games.add(this);
    }

    public void damageMob(Player plr, LivingEntity ent, double amount) {
        Mob mob = mobs.get(ent.getUniqueId());
        if (mob == null) {
            ent.remove();
            return;
        }
        if (amount >= ent.getHealth()) {
            ent.playEffect(EntityEffect.WITCH_MAGIC);
            mobs.remove(ent.getUniqueId());
            ent.remove();
            if (plr != null) {
                PlayerData pd = players.get(plr.getName());
                pd.addPoints(mob);
            }
            return;
        }
        ent.setHealth(ent.getHealth() - amount);
        ent.setCustomName(mob.getName() + conf.getMobHpSuffix().replace("<hp>", DF.format(ent.getHealth())));
        ent.playEffect(EntityEffect.HURT);
    }

    public void fastTick() {
        if (state == GameState.INGAME)
            players.values().forEach(PlayerData::updateBossBars);
    }

    public void finish() {
        if (players.isEmpty()) {
            forceStop();
            return;
        }
        if (players.size() > 1) {
            int drawScore = players.values().iterator().next().getPoints();
            boolean draw = true;
            for (PlayerData pd : players.values()) {
                if (pd.getPoints() != drawScore) {
                    draw = false;
                    break;
                }
            }
            if (draw) {
                sendTitle("draw");
                if (conf.getDrawExp() > 0) {
                    players.values().forEach(pd ->
                            LevelingAPI.withPlayer(pd.getPlayer(),
                                    lpd -> lpd.addExp(conf.getDrawExp(), "Draw @ Hunting Games")));
                }
                switchToNextState();
                return;
            }
        }
        List<PlayerData> sortedPlayers = this.players.values().stream().sorted(Comparator.comparing(PlayerData::getPoints).reversed()).toList();
        winner = sortedPlayers.get(0);
        if (conf.getWinExp() > 0)
            LevelingAPI.withPlayer(winner.getPlayer(),
                    lpd -> lpd.addExp(conf.getWinExp(), "Winning the Hunting Games"));
        if (conf.getLoseExp() > 0)
            for (int i = 1; i < sortedPlayers.size(); ++i)
                LevelingAPI.withPlayer(sortedPlayers.get(i).getPlayer(),
                        lpd -> lpd.addExp(conf.getLoseExp(), "Losing the Hunting Games"));
    }

    public void forceStart(CommandSender sender) {
        if (state != GameState.STARTING && state != GameState.WAITING) {
            msg.msg(sender, "game.running");
            return;
        }
        state = GameState.STARTING;
        counter = 7;
        msg.msg(sender, "game.start");
    }

    public void forceStop() {
        sendTitle("forcestop");
        stop();
    }

    private int getNeededPlayerCount() {
        return conf.getMinPlayers() - players.size();
    }

    private Object[] getVariables() {
        List<Object> out = Lists.newArrayList(
                "players", players.size(),
                "needed", getNeededPlayerCount(),
                "maxplayers", conf.getMaxPlayers(),
                "counter", counter,
                "winner", winner == null ? "" : winner.getName());
        List<PlayerData> players = this.players.values().stream().sorted(Comparator.comparing(PlayerData::getPoints).reversed()).toList();
        for (int i = 1; i <= players.size(); ++i) {
            PlayerData pd = players.get(i - 1);
            out.addAll(List.of(
                    "top" + i, pd.getName(),
                    "top" + i + "points", DF.format(pd.getPoints()),
                    "top" + i + "targets", DF.format(pd.getTargets())));
        }
        return out.toArray();
    }

    public boolean join(List<Player> newPlayers) {
        if (state != GameState.WAITING && state != GameState.STARTING)
            return false;

        if (this.players.size() + newPlayers.size() > conf.getMaxPlayers())
            return false;

        for (Player plr : newPlayers)
            players.put(plr.getName(), new PlayerData(this, plr, LocUtils.fixLoc(arena.getQueue().randomLoc(), arena.getQueueRot())));

        if (state == GameState.WAITING && this.players.size() >= conf.getMinPlayers()) {
            state = GameState.STARTING;
            counter = conf.getCounters().getStarting();
        }
        if (this.players.size() >= conf.getMaxPlayers()) {
            counter = conf.getCounters().getMaxplayer();
        }

        ScoreboardUtils.updateScoreboard(board,
                msg.getList(state == GameState.WAITING ? "scoreboard.waiting" : "scoreboard.starting"),
                "players", players.size(),
                "maxplayers", conf.getMaxPlayers(),
                "needed", getNeededPlayerCount(),
                "counter", counter);

        for (Player plr : newPlayers) {
            HGAPI.playerGames.put(plr.getName(), this);
            plr.setGameMode(GameMode.ADVENTURE);
            plr.setScoreboard(board);
            msg.msg(plr, "game.join", "arena", arena.getName());
        }
        return true;
    }

    public void quit(Player plr) {
        String pln = plr.getName();

        PlayerData pd = players.remove(pln);
        if (pd == null)
            return;

        pd.reset(plr);
        HGAPI.playerGames.remove(pln);

        if (players.isEmpty()) {
            HGAPI.games.remove(this);
            return;
        }

        if (state == GameState.WAITING || state == GameState.STARTING) {
            if (players.size() < conf.getMinPlayers()) {
                counter = 0;
                state = GameState.WAITING;
            }
            ScoreboardUtils.updateScoreboard(board,
                    msg.getList(state == GameState.WAITING ? "scoreboard.waiting" : "scoreboard.starting"),
                    "players", players.size(),
                    "maxplayers", conf.getMaxPlayers(),
                    "needed", getNeededPlayerCount(),
                    "counter", counter);
        }
    }

    private List<String> removeEmptyTeams(List<String> list) {
        for (int i = players.size() + 1; i <= conf.getMaxPlayers(); ++i) {
            int topId = i;
            list.removeIf(s -> s.contains("<top" + topId));
        }
        return list;
    }

    public Player removePortalOwner(Loc loc) {
        String pln = portalOwners.remove(loc);
        loc.toBlock().setType(Material.AIR);
        return pln == null ? null : Bukkit.getPlayerExact(pln);
    }

    private void sendTitle(String key, Object... vars) {
        String title = msg.get(key + ".title", vars);
        String subtitle = msg.get(key + ".subtitle", vars);
        String actionbar = msg.get(key + ".actionbar", vars);
        for (PlayerData pd : players.values()) {
            Player plr = pd.getPlayer();
            plr.sendTitle(title, subtitle, conf.getTitleFadeIn(), conf.getTitleShowTime(), conf.getTitleFadeOut());
            plr.sendActionBar(actionbar);
        }
    }

    private void spawnMobs() {
        int maxMobs = players.size() * conf.getMaxMobsPerPlayer();
        int spawnCount = Math.min(maxMobs - mobs.size(), conf.getMobSpawnCount() * players.size());
        int spawned = 0;
        while (spawned < spawnCount) {
            for (Mob mob : conf.getMobs().values()) {
                if (rand.nextDouble() >= mob.getChance())
                    continue;
                ++spawned;
                Location loc = arena.getArea().randomLoc();
                Material bottomBlock = loc.getBlock().getRelative(BlockFace.DOWN).getType();
                if (!bottomBlock.isSolid() || bottomBlock.name().endsWith("_LEAVES"))
                    continue;
                mobs.put(mob.summon(loc).getUniqueId(), mob);
                if (spawned >= spawnCount)
                    return;
            }
        }
    }

    private void stop() {
        portalOwners.keySet().forEach(loc -> loc.toBlock().setType(Material.AIR));
        arena.getArea().clearEntities();

        players.forEach((pln, pd) -> {
            HGAPI.playerGames.remove(pln);
            Player p = Bukkit.getPlayerExact(pln);
            if (p == null)
                return;
            pd.reset(p);
        });
        players.clear();

        HGAPI.games.remove(this);
    }

    private void switchToNextState() {
        switch (state) {
            case STARTING -> {
                players.values().forEach(pd -> pd.getPlayer().teleport(LocUtils.fixLoc(arena.getSpawn().randomLoc(), arena.getSpawnRot())));
                state = GameState.INARENA;
                counter = conf.getCounters().getInarena();
                players.values().forEach(pd -> {
                    PlayerInventory pi = pd.getPlayer().getInventory();
                    pi.setItem(conf.getUpgradeItemSlot(), null);
                    if (pd.getUpgrades().contains("bow"))
                        pi.setItem(9, new ItemStack(Material.ARROW));
                    pd.getUpgrades().forEach((upg) -> conf.getUpgrades().get(upg).apply(pd));
                });
                updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.inarena")), getVariables());
            }
            case INARENA -> {
                state = GameState.INGAME;
                counter = conf.getCounters().getIngame();
                updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.ingame")), getVariables());
            }
            case INGAME -> {
                state = GameState.FINISH;
                finish();
                updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.finish")), getVariables());
                counter = conf.getCounters().getFinish();
            }
            case FINISH -> {
                stop();
            }
        }
    }

    public void tick() {
        --counter;
        if (counter == 0) {
            switchToNextState();
            players.values().forEach(pd -> pd.getPlayer().setLevel(counter));
            return;
        }
        if (counter > 0) {
            players.values().forEach(pd -> pd.getPlayer().setLevel(counter));
        }
        switch (state) {
            case STARTING -> {
                updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.starting")), getVariables());
            }
            case INARENA -> {
                updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.inarena")), getVariables());
            }
            case INGAME -> {
                if (rand.nextDouble() < conf.getMobSpawnChance())
                    spawnMobs();
                updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.ingame")), getVariables());
            }
            case FINISH -> {

            }
        }
    }
}
