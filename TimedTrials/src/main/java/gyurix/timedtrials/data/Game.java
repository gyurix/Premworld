package gyurix.timedtrials.data;

import com.google.common.collect.Lists;
import gyurix.levelingsystem.LevelingAPI;
import gyurix.timedtrials.TTAPI;
import gyurix.timedtrials.enums.GameState;
import gyurix.timedtrials.util.LocUtils;
import gyurix.timedtrials.util.ScoreboardUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static gyurix.timedtrials.conf.ConfigManager.conf;
import static gyurix.timedtrials.conf.ConfigManager.msg;
import static gyurix.timedtrials.util.ScoreboardUtils.updateScoreboard;
import static gyurix.timedtrials.util.StrUtils.DF;

@Getter
public class Game {
    private final Arena arena;
    private final Scoreboard board;
    private final HashMap<String, PlayerData> players = new HashMap<>();
    private int counter = 0;
    private GameState state = GameState.WAITING;
    @Setter
    private Player winner;

    public Game(Arena arena) {
        this.arena = arena;
        arena.getArea().clearEntities();
        board = ScoreboardUtils.createScoreboard(
                msg.get("scoreboard.title"),
                msg.getList("scoreboard.waiting"),
                "players", "0",
                "maxplayers", conf.getMaxPlayers(),
                "needed", conf.getMinPlayers());
        TTAPI.games.add(this);
    }

    public void finish() {
        if (players.isEmpty()) {
            forceStop();
            return;
        }
        if (winner == null) {
            players.values().forEach(pd -> sendTitle(pd.getPlayer(), "draw"));
            if (conf.getDrawExp() > 0) {
                players.values().forEach(pd ->
                        LevelingAPI.withPlayer(pd.getPlayer(),
                                lpd -> lpd.addExp(conf.getDrawExp(), "Draw @ Timed Trials")));
            }
            return;
        }
        sendTitle(winner, "win");
        if (conf.getWinExp() > 0)
            LevelingAPI.withPlayer(winner, lpd -> lpd.addExp(conf.getWinExp(), "Winning the Timed Trials"));

        players.values().forEach(pd -> {
            if (!pd.getName().equals(winner.getName()))
                sendTitle(pd.getPlayer(), "lose");
        });
        if (conf.getLoseExp() > 0)
            for (PlayerData pd : players.values()) {
                if (pd.getName().equals(winner.getName()))
                    continue;
                LevelingAPI.withPlayer(pd.getPlayer(),
                        lpd -> lpd.addExp(conf.getLoseExp(), "Losing the Timed Trials"));
            }
    }

    public void forceStart(CommandSender sender) {
        if (state != GameState.STARTING && state != GameState.WAITING) {
            msg.msg(sender, "game.running");
            return;
        }
        state = GameState.STARTING;
        counter = 2;
        msg.msg(sender, "game.start");
    }

    public void forceStop() {
        players.values().forEach(pd -> sendTitle(pd.getPlayer(), "forcestop"));
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
        List<PlayerData> sortedPlayers = this.players.values().stream().sorted(Comparator.comparing(PlayerData::getDistance)).toList();
        for (int i = 1; i <= sortedPlayers.size(); ++i) {
            PlayerData pd = sortedPlayers.get(i - 1);
            out.addAll(List.of(
                    "top" + i, pd.getName(),
                    "top" + i + "dist", DF.format((int) (pd.getDistance() + 0.999))));
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
            TTAPI.playerGames.put(plr.getName(), this);
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
        TTAPI.playerGames.remove(pln);

        if (players.isEmpty()) {
            TTAPI.games.remove(this);
            return;
        }

        Entity vehicle = plr.getVehicle();
        if (vehicle != null)
            vehicle.remove();

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

    private void sendTitle(Player plr, String key, Object... vars) {
        String title = msg.get(key + ".title", vars);
        String subtitle = msg.get(key + ".subtitle", vars);
        String actionbar = msg.get(key + ".actionbar", vars);
        plr.sendTitle(title, subtitle, conf.getTitleFadeIn(), conf.getTitleShowTime(), conf.getTitleFadeOut());
        plr.sendActionBar(actionbar);
    }

    private void stop() {
        arena.getArea().clearEntities();

        players.forEach((pln, pd) -> {
            TTAPI.playerGames.remove(pln);
            Player p = Bukkit.getPlayerExact(pln);
            if (p == null)
                return;
            pd.reset(p);
        });
        players.clear();

        TTAPI.games.remove(this);
    }

    public void switchToNextState() {
        switch (state) {
            case STARTING -> {
                int spawnId = 0;
                for (PlayerData pd : players.values()) {
                    Location loc = arena.getSpawns().get(spawnId).toLoc();
                    Boat boat = (Boat) loc.getWorld().spawnEntity(loc, EntityType.BOAT);
                    boat.addPassenger(pd.getPlayer());
                    /*ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel("jetski");
                    ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(boat);
                    modeledEntity.addActiveModel(model);
                    modeledEntity.setInvisible(true);*/
                    ++spawnId;
                }
                arena.getWall().changeBlock(Material.RED_STAINED_GLASS);
                state = GameState.INARENA;
                counter = conf.getCounters().getInarena();
                updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.inarena")), getVariables());
            }
            case INARENA -> {
                state = GameState.INGAME;
                arena.getWall().changeBlock(Material.AIR);
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
            case STARTING ->
                    updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.starting")), getVariables());
            case INARENA ->
                    updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.inarena")), getVariables());
            case INGAME -> updateScoreboard(board, removeEmptyTeams(msg.getList("scoreboard.ingame")), getVariables());
        }
    }
}
