package gyurix.coliseumgames.data;

import com.google.common.collect.Lists;
import com.nftworlds.wallet.objects.NFTPlayer;
import com.nftworlds.wallet.objects.Network;
import gyurix.coliseumgames.CGAPI;
import gyurix.coliseumgames.enums.GameState;
import gyurix.coliseumgames.util.LocUtils;
import gyurix.coliseumgames.util.ScoreboardUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static gyurix.coliseumgames.conf.ConfigManager.conf;
import static gyurix.coliseumgames.conf.ConfigManager.msg;

@Getter
public class Game {
    private final Arena arena;
    private final HashMap<String, PlayerData> players = new HashMap<>();
    private final HashMap<String, PlayerData> spectators = new HashMap<>();
    private final Scoreboard scoreboard;
    private int counter = 0;
    private Area pointsArea;
    private int prize = 0;
    private GameState state = GameState.WAITING;
    private String winner;

    public Game(Arena arena) {
        this.arena = arena;
        arena.getArea().clearEntities();
        pointsArea = arena.getArea().clone();
        pointsArea.setMaxY(pointsArea.getMaxY() - 2);
        scoreboard = ScoreboardUtils.createScoreboard(
            msg.get("scoreboard.title"),
            msg.getList("scoreboard.waiting"),
            "players", "0",
            "maxplayers", conf.getMaxPlayers(),
            "needed", conf.getMinPlayers());
        CGAPI.games.add(this);
    }

    public void fastTick() {
        if (state == GameState.INGAME) {
            players.values().forEach(PlayerData::updateBossBars);
        }
    }

    public void finish() {
        Player plr = players.values().iterator().next().getPlayer();
        winner = plr.getName();
        titleAll(msg.get("finish.title", "winner", winner),
            msg.get("finish.subtitle", "winner", winner),
            msg.get("finish.actionbar", "winner", winner), null);
        try {
            NFTPlayer.getByUUID(plr.getUniqueId()).getPrimaryWallet().payWRLD(prize, Network.POLYGON, "Winning Coliseum Games");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        switchToNextState();
    }

    public void forceStart(CommandSender sender) {
        if (state != GameState.STARTING && state != GameState.WAITING) {
            msg.msg(sender, "game.running");
            return;
        }
        state = GameState.STARTING;
        counter = 3;
        msg.msg(sender, "game.start");
    }

    public boolean join(Player plr) {
        if (state != GameState.WAITING && state != GameState.STARTING)
            return false;

        if (players.size() >= conf.getMaxPlayers())
            return false;

        players.put(plr.getName(), new PlayerData(plr, LocUtils.fixLoc(arena.getQueue().randomLoc())));
        CGAPI.playerGames.put(plr.getName(), this);
        plr.setGameMode(GameMode.ADVENTURE);

        if (state == GameState.WAITING && players.size() >= conf.getMinPlayers()) {
            state = GameState.STARTING;
            counter = conf.getCounters().getStarting();
        }
        if (players.size() >= conf.getMaxPlayers()) {
            counter = conf.getCounters().getMaxplayer();
        }

        ScoreboardUtils.updateScoreboard(scoreboard,
            msg.getList(state == GameState.WAITING ? "scoreboard.waiting" : "scoreboard.starting"),
            "players", players.size(),
            "maxplayers", conf.getMaxPlayers(),
            "needed", conf.getMinPlayers() - players.size(),
            "counter", counter);

        plr.setScoreboard(scoreboard);
        msg.msg(plr, "game.join", "arena", arena.getName());
        return true;
    }

    public void quit(Player plr) {
        String pln = plr.getName();
        PlayerData pd = players.remove(pln);
        if (plr.getVehicle() != null)
            plr.getVehicle().remove();
        pd.reset(plr);

        CGAPI.playerGames.remove(pln);
        if (players.isEmpty()) {
            CGAPI.games.remove(this);
            return;
        } else if (state == GameState.INGAME && players.size() == 1) {
            finish();
            return;
        }

        if (state == GameState.WAITING || state == GameState.STARTING) {
            if (players.size() < conf.getMinPlayers()) {
                counter = 0;
                state = GameState.WAITING;
            }
            ScoreboardUtils.updateScoreboard(scoreboard,
                msg.getList(state == GameState.WAITING ? "scoreboard.waiting" : "scoreboard.starting"),
                "players", players.size(),
                "maxplayers", conf.getMaxPlayers(),
                "needed", conf.getMinPlayers() - players.size(),
                "counter", counter);
        }
    }

    public void stop() {
        if (state == GameState.INGAME || state == GameState.FINISH) {
            arena.getArea().clearEntities();
        }

        players.forEach((pln, pd) -> {
            CGAPI.playerGames.remove(pln);
            Player p = Bukkit.getPlayerExact(pln);
            if (p == null)
                return;
            pd.reset(p);
        });

        spectators.forEach((pln, pd) -> {
            CGAPI.playerGames.remove(pln);
            Player p = Bukkit.getPlayerExact(pln);
            if (p == null)
                return;
            pd.reset(p);
        });

        players.clear();
        spectators.clear();
        CGAPI.games.remove(this);
    }

    public void tick() {
        --counter;
        if (counter == 0) {
            switchToNextState();
            return;
        }
        if (counter > 0) {
            players.values().forEach(pd -> pd.getPlayer().setLevel(counter));
            spectators.values().forEach(pd -> pd.getPlayer().setLevel(counter));
        }
        switch (state) {
            case STARTING -> {
                ScoreboardUtils.updateScoreboard(scoreboard, msg.getList("scoreboard.starting"),
                    "players", players.size(),
                    "maxplayers", conf.getMaxPlayers(),
                    "counter", counter);
            }
            case INARENA -> {
                ScoreboardUtils.updateScoreboard(scoreboard, msg.getList("scoreboard.inarena"),
                    "prize", prize,
                    "players", players.size(),
                    "spec", spectators.size(),
                    "counter", counter);
            }
            case INGAME -> {
                ScoreboardUtils.updateScoreboard(scoreboard, msg.getList("scoreboard.ingame"),
                    getVariables());
            }
            case FINISH -> {

            }
        }
    }

    private Object[] getVariables() {
        List<Object> out = Lists.newArrayList(
            "prize", prize,
            "players", players.size(),
            "spec", spectators.size(),
            "counter", counter,
            "winner", winner);
        List<PlayerData> players = this.players.values().stream().sorted(Comparator.comparing(PlayerData::getName)).toList();
        for (int i = 1; i <= conf.getMaxPlayers(); ++i) {
            if (players.size() < i) {
                out.addAll(List.of(
                    "top" + i, ""));
                continue;
            }
            PlayerData pd = players.get(i - 1);
            out.addAll(List.of(
                "top" + i, pd.getName()));
        }
        return out.toArray();
    }


    private void switchToNextState() {
        switch (state) {
            case STARTING -> {
                for (String pln : players.keySet()) {
                    Player p = Bukkit.getPlayerExact(pln);
                    p.teleport(LocUtils.fixLoc(arena.getStart().randomLoc()));
                }
                state = GameState.INARENA;
                counter = conf.getCounters().getInarena();

                ScoreboardUtils.updateScoreboard(scoreboard, msg.getList("scoreboard.inarena"),
                    "prize", prize,
                    "players", players.size(),
                    "spec", spectators.size(),
                    "counter", counter);
            }
            case INARENA -> {
                state = GameState.INGAME;
                ScoreboardUtils.updateScoreboard(scoreboard, msg.getList("scoreboard.ingame"),
                    getVariables());
                counter = conf.getCounters().getIngame();
            }
            case INGAME -> {
                state = GameState.FINISH;
                ScoreboardUtils.updateScoreboard(scoreboard, msg.getList("scoreboard.finish"),
                    getVariables());
                counter = conf.getCounters().getFinish();
            }
            case FINISH -> {
                stop();
            }
        }
    }

    private void titleAll(String title, String subtitle, String actionBar, String skip) {
        players.values().forEach(pd -> {
            if (pd.getName().equals(skip))
                return;
            Player p = pd.getPlayer();
            p.sendTitle(title, subtitle, conf.getTitleFadeIn(), conf.getTitleShowTime(), conf.getTitleFadeOut());
            p.sendActionBar(actionBar);
        });
        spectators.values().forEach(pd -> {
            if (pd.getName().equals(skip))
                return;
            Player p = pd.getPlayer();
            p.sendTitle(title, subtitle, conf.getTitleFadeIn(), conf.getTitleShowTime(), conf.getTitleFadeOut());
            p.sendActionBar(actionBar);
        });
    }
}
