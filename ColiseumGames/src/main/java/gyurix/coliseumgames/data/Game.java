package gyurix.coliseumgames.data;

import com.google.common.collect.Lists;
import gyurix.coliseumgames.CGAPI;
import gyurix.coliseumgames.enums.GameState;
import gyurix.coliseumgames.util.ScoreboardUtils;
import gyurix.levelingsystem.LevelingAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static gyurix.coliseumgames.conf.ConfigManager.conf;
import static gyurix.coliseumgames.conf.ConfigManager.msg;
import static gyurix.coliseumgames.util.LocUtils.fixLoc;
import static gyurix.coliseumgames.util.ScoreboardUtils.updateScoreboard;

@Getter
public class Game {
    private final Arena arena;
    private final Counter counters;
    private final HashMap<String, PlayerData> team1 = new HashMap<>();
    private final Scoreboard team1Board, team2Board;
    private final HashMap<String, PlayerData> team1Spec = new HashMap<>();
    private final HashMap<String, PlayerData> team2 = new HashMap<>();
    private final HashMap<String, PlayerData> team2Spec = new HashMap<>();
    private int counter = 0;
    private int maxPlayersPerTeam;
    private int minPlayersPerTeam;
    private GameState state = GameState.WAITING;
    private String team1Carrier, team2Carrier;
    private int team1Collected, team2Collected;
    private ArmorStand team1Flag, team2Flag;
    private GameType type;

    public Game(Arena arena) {
        this.arena = arena;
        this.type = conf.getGameTypes().get(arena.getType());
        arena.getArea().clearEntities();
        minPlayersPerTeam = type.getMinPlayersPerTeam();
        maxPlayersPerTeam = type.getMaxPlayersPerTeam();

        counters = type.getCounters();
        team1Board = ScoreboardUtils.createScoreboard(
                msg.get("scoreboard." + type + ".title"),
                msg.getList("scoreboard." + type + ".waiting"),
                "players", "0",
                "maxplayers", maxPlayersPerTeam * 2,
                "needed", minPlayersPerTeam * 2);
        team2Board = ScoreboardUtils.createScoreboard(
                msg.get("scoreboard." + type + ".title"),
                msg.getList("scoreboard." + type + ".waiting"),
                "players", "0",
                "maxplayers", maxPlayersPerTeam * 2,
                "needed", minPlayersPerTeam * 2);
        CGAPI.games.add(this);
    }

    public void depositFlag(Player plr, boolean secondTeam) {
        sendTitle((secondTeam ? team2 : team1).values(), "deposityou", "player", plr.getName());
        sendTitle((secondTeam ? team1 : team2).values(), "depositopponent", "player", plr.getName());
        if (secondTeam)
            ++team2Collected;
        else
            ++team1Collected;
        resetFlag(!secondTeam);
        if (team1Collected >= type.getFlagCount() || team2Collected >= type.getFlagCount())
            finish();
    }

    public void fastTick() {
        if (state == GameState.INGAME) {
            team1.values().forEach(PlayerData::updateBossBars);
            team2.values().forEach(PlayerData::updateBossBars);
        }
    }

    public void finish() {
        if (type.getFlagCount() > 0 ? team1Collected == team2Collected : team1.size() == team2.size()) {
            for (HashMap<String, PlayerData> team : List.of(team1, team2, team1Spec, team2Spec))
                sendTitle(team.values(), "draw");
            if (type.getDrawExp() > 0) {
                team1.values().forEach(pd ->
                        LevelingAPI.withPlayer(pd.getPlayer(),
                                lpd -> lpd.addExp(type.getDrawExp(), "Playing " + arena.getType().toUpperCase() + " Coliseum Games")));
                team2.values().forEach(pd ->
                        LevelingAPI.withPlayer(pd.getPlayer(),
                                lpd -> lpd.addExp(type.getDrawExp(), "Playing " + arena.getType().toUpperCase() + " Coliseum Games")));
            }
            switchToNextState();
            return;
        }
        boolean secondTeamWin = type.getFlagCount() > 0 ? team2Collected > team1Collected : team2.size() > team1.size();
        if (secondTeamWin) {
            for (HashMap<String, PlayerData> team : List.of(team1, team1Spec))
                sendTitle(team.values(), "lose");
            for (HashMap<String, PlayerData> team : List.of(team2, team2Spec))
                sendTitle(team.values(), "win");
        } else {
            for (HashMap<String, PlayerData> team : List.of(team1, team1Spec))
                sendTitle(team.values(), "win");
            for (HashMap<String, PlayerData> team : List.of(team2, team2Spec))
                sendTitle(team.values(), "lose");
        }
        if (type.getWinExp() > 0) {
            (secondTeamWin ? team2 : team1).values().forEach(pd ->
                    LevelingAPI.withPlayer(pd.getPlayer(),
                            lpd -> lpd.addExp(type.getWinExp(), "Winning " + arena.getType().toUpperCase() + " Coliseum Games")));
        }
        if (type.getLoseExp() > 0) {
            (secondTeamWin ? team1 : team2).values().forEach(pd ->
                    LevelingAPI.withPlayer(pd.getPlayer(),
                            lpd -> lpd.addExp(type.getLoseExp(), "Playing " + arena.getType().toUpperCase() + " Coliseum Games")));
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

    public void forceStop() {
        for (HashMap<String, PlayerData> team : List.of(team1, team2, team1Spec, team2Spec))
            sendTitle(team.values(), "forcestop");
        stop();
    }

    private int getNeededPlayerCount() {
        int neededIndividual = Math.max(minPlayersPerTeam - team1.size(), minPlayersPerTeam - team2.size());
        return Math.max(neededIndividual, minPlayersPerTeam * 2 - team1.size() - team2.size());
    }

    private Object[] getVariables(boolean secondTeam) {
        List<Object> out = Lists.newArrayList(
                "players", team1.size() + team2.size(),
                "spec", team1Spec.size() + team2Spec.size(),
                "counter", counter,
                "flagCount", type.getFlagCount(),
                (secondTeam ? "opponent" : "team") + "FlagCarrier", team1Carrier == null ? "Noone" : team1Carrier,
                (secondTeam ? "team" : "opponent") + "FlagCarrier", team2Carrier == null ? "Noone" : team2Carrier,
                (secondTeam ? "opponent" : "team") + "Collected", team1Collected,
                (secondTeam ? "team" : "opponent") + "Collected", team2Collected);
        List<PlayerData> team1List = team1.values().stream().sorted(Comparator.comparing(PlayerData::getName)).toList();
        List<PlayerData> team2List = team2.values().stream().sorted(Comparator.comparing(PlayerData::getName)).toList();
        for (int i = 1; i <= maxPlayersPerTeam; ++i) {
            out.addAll(List.of((secondTeam ? "opponent" : "team") + i, team1List.size() >= i ? team1List.get(i - 1).getName() : ""));
            out.addAll(List.of((secondTeam ? "team" : "opponent") + i, team2List.size() >= i ? team2List.get(i - 1).getName() : ""));
        }

        out.add("winner");
        boolean secondTeamWin = type.getFlagCount() > 0 ? team2Collected > team1Collected : team2.size() > team1.size();
        boolean type1v1 = arena.getType().equals("1v1");
        if (team1.size() == team2.size())
            out.add("Noone");
        else if (secondTeam == secondTeamWin)
            out.add(type1v1 ? "You" : "Your Team");
        else
            out.add(type1v1 ? (secondTeamWin ? team2 : team1).keySet().iterator().next() : "Opponent Team");

        return out.toArray();
    }

    private boolean join(HashMap<String, PlayerData> team, List<Player> players) {
        for (Player plr : players)
            team.put(plr.getName(), new PlayerData(this, plr, fixLoc(arena.getQueue().randomLoc())));

        boolean secondTeam = team == team2;
        if (state == GameState.WAITING && team1.size() >= minPlayersPerTeam && team2.size() >= minPlayersPerTeam) {
            state = GameState.STARTING;
            counter = counters.getStarting();
        }
        if (team1.size() >= maxPlayersPerTeam && team2.size() >= maxPlayersPerTeam) {
            counter = counters.getMaxplayer();
        }

        String type = arena.getType();
        for (Scoreboard board : List.of(team1Board, team2Board)) {
            Team sbTeam = board.getTeam(secondTeam ? "team2" : "team1");
            Objective hpObj = board.getObjective("hp");
            players.forEach(plr -> {
                sbTeam.addEntry(plr.getName());
                hpObj.getScore(plr.getName()).setScore((int) plr.getHealth());
            });
            updateScoreboard(board,
                    msg.getList(state == GameState.WAITING ? "scoreboard." + type + ".waiting" : "scoreboard." + type + ".starting"),
                    "needed", getNeededPlayerCount(),
                    "counter", counter);
        }

        for (Player plr : players) {
            CGAPI.playerGames.put(plr.getName(), this);
            plr.setGameMode(GameMode.ADVENTURE);
            plr.setScoreboard(secondTeam ? team2Board : team1Board);
            msg.msg(plr, "game.join", "arena", arena.getName(), "type", arena.getType());
        }
        return true;
    }

    public boolean join(List<Player> players) {
        if (state != GameState.WAITING && state != GameState.STARTING)
            return false;

        if (team1.size() + players.size() > maxPlayersPerTeam && team2.size() + players.size() > maxPlayersPerTeam)
            return false;

        if (team2.size() + players.size() == minPlayersPerTeam) {
            return join(team2, players);
        }
        if (team1.size() + players.size() == minPlayersPerTeam) {
            return join(team1, players);
        }

        boolean secondTeam = team1.size() > team2.size();
        return join(secondTeam ? team2 : team1, players);
    }

    public void pickupFlag(Player plr, boolean secondTeam) {
        if (secondTeam) {
            team1Flag.remove();
            team2Carrier = plr.getName();
        } else {
            team2Flag.remove();
            team1Carrier = plr.getName();
        }
        plr.getInventory().setItem(8, (secondTeam ? conf.getFlag1() : conf.getFlag2()).clone());
        sendTitle((secondTeam ? team2 : team1).values(), "pickupyou", "player", plr.getName());
        sendTitle((secondTeam ? team1 : team2).values(), "pickupopponent", "player", plr.getName());
    }

    public void quit(Player plr) {
        String pln = plr.getName();
        team1Board.getTeam("team1").removeEntry(pln);
        team2Board.getTeam("team1").removeEntry(pln);
        team1Board.getTeam("team2").removeEntry(pln);
        team2Board.getTeam("team2").removeEntry(pln);

        PlayerData pd = team1.remove(pln);
        if (pd == null)
            pd = team2.remove(pln);
        if (pd == null)
            pd = team1Spec.remove(pln);
        if (pd == null)
            pd = team2Spec.remove(pln);

        pd.reset(plr);

        CGAPI.playerGames.remove(pln);
        if (type.getFlagCount() > 0) {
            if (pln.equals(team1Carrier))
                resetFlag(true);
            else if (pln.equals(team2Carrier))
                resetFlag(false);
        }
        if (team1.isEmpty() && team2.isEmpty()) {
            CGAPI.games.remove(this);
            return;
        } else if (state == GameState.INGAME && (team1.isEmpty() || team2.isEmpty())) {
            finish();
            return;
        }

        String type = arena.getType();
        if (state == GameState.WAITING || state == GameState.STARTING) {
            if (team1.size() < minPlayersPerTeam || team2.size() < minPlayersPerTeam) {
                counter = 0;
                state = GameState.WAITING;
            }
            updateScoreboard(team1Board,
                    msg.getList(state == GameState.WAITING ? "scoreboard." + type + ".waiting" : "scoreboard." + type + ".starting"),
                    "players", team1.size() + team2.size(),
                    "maxplayers", maxPlayersPerTeam * 2,
                    "needed", getNeededPlayerCount(),
                    "counter", counter);
        }
    }

    public void resetFlag(boolean secondTeam) {
        Area area = secondTeam ? arena.getTeam2() : arena.getTeam1();
        Location loc = fixLoc(area.randomLoc()).subtract(0, 1, 0);
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setBasePlate(false);
        as.setVisible(false);
        as.setHelmet(secondTeam ? conf.getFlag2() : conf.getFlag1());
        as.setGravity(false);
        as.addDisabledSlots(EquipmentSlot.values());
        if (secondTeam) {
            if (team1Carrier != null)
                Bukkit.getPlayerExact(team1Carrier).getInventory().setItem(8, null);
            team2Flag = as;
            team1Carrier = null;
        } else {
            if (team2Carrier != null)
                Bukkit.getPlayerExact(team2Carrier).getInventory().setItem(8, null);
            team1Flag = as;
            team2Carrier = null;
        }
    }

    private void sendTitle(Collection<PlayerData> playerList, String key, Object... vars) {
        String title = msg.get(key + ".title", vars);
        String subtitle = msg.get(key + ".subtitle", vars);
        String actionbar = msg.get(key + ".actionbar", vars);
        for (PlayerData pd : playerList) {
            Player plr = pd.getPlayer();
            plr.sendTitle(title, subtitle, conf.getTitleFadeIn(), conf.getTitleShowTime(), conf.getTitleFadeOut());
            plr.sendActionBar(actionbar);
        }
    }

    public void spectate(Player plr) {
        boolean secondTeam = team2.containsKey(plr.getName());
        String pln = plr.getName();
        PlayerData pd = (secondTeam ? team2 : team1).remove(pln);
        if (pd == null)
            return;
        plr.setGameMode(GameMode.SPECTATOR);
        (secondTeam ? team2Spec : team1Spec).put(pln, pd);
        if (state == GameState.INGAME && (team1.isEmpty() || team2.isEmpty()))
            finish();
    }

    private void stop() {
        arena.getArea().clearEntities();

        for (HashMap<String, PlayerData> team : List.of(team1, team2, team1Spec, team2Spec)) {
            team.forEach((pln, pd) -> {
                CGAPI.playerGames.remove(pln);
                Player p = Bukkit.getPlayerExact(pln);
                if (p == null)
                    return;
                pd.reset(p);
            });
            team.clear();
        }

        CGAPI.games.remove(this);
    }

    private void switchToNextState() {
        switch (state) {
            case STARTING -> {
                team1.values().forEach(pd -> pd.getPlayer().teleport(fixLoc(arena.getTeam1().randomLoc(), arena.getTeam1Rot())));
                team2.values().forEach(pd -> pd.getPlayer().teleport(fixLoc(arena.getTeam2().randomLoc(), arena.getTeam2Rot())));
                state = GameState.INARENA;
                counter = counters.getInarena();
                team1.values().forEach(pd -> pd.getUpgrades().values().forEach((upg) -> conf.getUpgrades().get(upg).apply(pd)));
                team2.values().forEach(pd -> pd.getUpgrades().values().forEach((upg) -> conf.getUpgrades().get(upg).apply(pd)));
                updateScoreboard(team1Board, msg.getList("scoreboard." + type + ".inarena"), getVariables(false));
                updateScoreboard(team2Board, msg.getList("scoreboard." + type + ".inarena"), getVariables(true));
            }
            case INARENA -> {
                state = GameState.INGAME;
                counter = counters.getIngame();
                updateScoreboard(team1Board, msg.getList("scoreboard." + type + ".ingame"), getVariables(false));
                updateScoreboard(team2Board, msg.getList("scoreboard." + type + ".ingame"), getVariables(true));
                if (type.getFlagCount() > 0) {
                    resetFlag(false);
                    resetFlag(true);
                }
            }
            case INGAME -> {
                state = GameState.FINISH;
                updateScoreboard(team1Board, msg.getList("scoreboard." + type + ".finish"), getVariables(false));
                updateScoreboard(team2Board, msg.getList("scoreboard." + type + ".finish"), getVariables(true));
                new ArrayList<>(team1.values()).forEach(pd -> spectate(pd.getPlayer()));
                new ArrayList<>(team2.values()).forEach(pd -> spectate(pd.getPlayer()));
                counter = counters.getFinish();
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
            return;
        }
        if (counter > 0) {
            for (HashMap<String, PlayerData> team : List.of(team1, team2, team1Spec, team2Spec))
                team.values().forEach(pd -> pd.getPlayer().setLevel(counter));
        }
        String type = arena.getType();
        switch (state) {
            case STARTING -> {
                updateScoreboard(team1Board, msg.getList("scoreboard." + type + ".starting"), getVariables(false));
                updateScoreboard(team2Board, msg.getList("scoreboard." + type + ".starting"), getVariables(true));
            }
            case INARENA -> {
                updateScoreboard(team1Board, msg.getList("scoreboard." + type + ".inarena"), getVariables(false));
                updateScoreboard(team2Board, msg.getList("scoreboard." + type + ".inarena"), getVariables(true));
            }
            case INGAME -> {
                updateScoreboard(team1Board, msg.getList("scoreboard." + type + ".ingame"), getVariables(false));
                updateScoreboard(team2Board, msg.getList("scoreboard." + type + ".ingame"), getVariables(true));
            }
            case FINISH -> {

            }
        }
    }
}
