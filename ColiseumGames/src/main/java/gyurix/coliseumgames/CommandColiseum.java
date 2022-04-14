package gyurix.coliseumgames;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import gyurix.coliseumgames.data.Area;
import gyurix.coliseumgames.data.Arena;
import gyurix.coliseumgames.data.Game;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static gyurix.coliseumgames.CGAPI.games;
import static gyurix.coliseumgames.CGAPI.playerGames;
import static gyurix.coliseumgames.CGPlugin.pl;
import static gyurix.coliseumgames.conf.ConfigManager.*;
import static gyurix.coliseumgames.util.StrUtils.DF;

public class CommandColiseum implements CommandExecutor, TabCompleter {
    public static List<String> arenaCommands = List.of("create", "remove", "info", "set");
    public static List<String> mainCommands = List.of("help", "arena", "start", "stop", "queue", "ticket");
    public static List<String> settings = Lists.newArrayList("area", "queue", "spec", "team1", "team2", "type");

    public CommandColiseum() {
        PluginCommand cmd = pl.getCommand("coliseum");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @SneakyThrows
    private void arenaCmd(CommandSender sender, String[] args) {
        String sub = args[1].toLowerCase();
        String name = args[2].toLowerCase();
        Arena arena = arenas.get(name);
        if (arenaUseCheck(sender, sub, name, arena))
            return;

        switch (sub) {
            case "create" -> {
                if (arena != null) {
                    msg.msg(sender, "arena.exists", "arena", name);
                    return;
                }
                arenas.put(name, new Arena(name));
                msg.msg(sender, "arena.create", "arena", name);
                saveArenas();
                return;
            }
            case "remove" -> {
                arenas.remove(name);
                saveArenas();
                return;
            }
            case "info" -> {
                msg.msg(sender, "arena.info", "arena", name,
                        "area", toStr(arena.getArea()),
                        "queue", toStr(arena.getQueue()),
                        "spec", toStr(arena.getSpec()),
                        "team1", toStr(arena.getTeam1()),
                        "team2", toStr(arena.getTeam2()),
                        "type", toStr(arena.getType()),
                        "configured", arena.isConfigured() ? "§ayes" : "§cno");
                return;
            }
            case "set" -> {
                if (args.length == 3) {
                    msg.msg(sender, "arena.nosetting", "settings", StringUtils.join(settings, ", "));
                    return;
                }
                String setting = args[3].toLowerCase();
                if (!settings.contains(setting)) {
                    msg.msg(sender, "arena.wrongsetting", "settings", StringUtils.join(settings, ", "));
                    return;
                }
                if (setting.equals("type")) {
                    if (args.length == 4) {
                        msg.msg(sender, "game.notype", "types", StringUtils.join(conf.getGameTypes().keySet(), ", "));
                        return;
                    }
                    String type = args[4].toLowerCase();
                    if (!conf.getGameTypes().containsKey(type)) {
                        msg.msg(sender, "game.wrongtype", "type", type, "types", StringUtils.join(conf.getGameTypes().keySet(), ", "));
                        return;
                    }
                    arena.setType(type);
                    msg.msg(sender, "arena.set", "setting", setting, "arena", name, "value", type);
                } else {
                    Field f = Arena.class.getDeclaredField(setting);
                    f.setAccessible(true);
                    if (f.getType() == Area.class) {
                        Region region = null;
                        BukkitPlayer bPlayer = BukkitAdapter.adapt((Player) sender);
                        try {
                            region = WorldEdit.getInstance().getSessionManager().get(bPlayer).getSelection(bPlayer.getWorld());
                        } catch (Throwable ignored) {
                        }
                        if (region == null) {
                            msg.msg(sender, "arena.nosel");
                            return;
                        }
                        Area area = new Area(bPlayer.getWorld().getName(), region);
                        f.set(arena, area);
                        msg.msg(sender, "arena.set", "setting", setting, "arena", name, "value", area);
                        if (setting.equals("team1")) {
                            arena.setTeam1Rot(((Player) sender).getLocation().getYaw());
                            msg.msg(sender, "arena.set", "setting", "team1Rot", "arena", name, "value", DF.format(arena.getTeam1Rot()));
                        } else if (setting.equals("team2")) {
                            arena.setTeam2Rot(((Player) sender).getLocation().getYaw());
                            msg.msg(sender, "arena.set", "setting", "team2Rot", "arena", name, "value", DF.format(arena.getTeam2Rot()));
                        }
                    }
                }
                saveArenas();
                return;
            }
        }
        msg.msg(sender, "arena.wrongsub");
    }

    private boolean arenaUseCheck(CommandSender sender, String sub, String name, Arena arena) {
        if (!sub.equals("create")) {
            if (arena == null) {
                msg.msg(sender, "arena.notexists", "arena", name);
                return true;
            }
            if (!sub.equals("info")) {
                for (Game game : games) {
                    if (game.getArena() == arena) {
                        msg.msg(sender, "arena.inuse", "arena", name);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<String> filterStart(Collection<String> list, String prefix) {
        String prefixLower = prefix.toLowerCase();
        return list.stream().filter(el -> el.toLowerCase().startsWith(prefixLower)).sorted().toList();
    }

    private Collection<String> numberRange(int size) {
        List<String> out = new ArrayList<>();

        for (int i = 0; i < size; ++i)
            out.add(String.valueOf(i));

        return out;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (!sender.hasPermission("lsdc.admin")) {
            sender.sendMessage("§cYou don't have permission for using this command");
            return true;
        }
        String sub = args.length == 0 ? "help" : args[0].toLowerCase();
        switch (sub) {
            case "help" -> {
                msg.msg(sender, "help");
                return true;
            }
            case "arena" -> {
                if (args.length == 1) {
                    msg.msg(sender, "arena.help",
                            "settings", StringUtils.join(settings, ", "),
                            "arenas", StringUtils.join(arenas.keySet(), ", "));
                    return true;
                }
                if (args.length == 2) {
                    msg.msg(sender, "arena.none",
                            "arenas", StringUtils.join(arenas.keySet(), ", "));
                    return true;
                }
                arenaCmd(sender, args);
                return true;
            }
            case "start" -> {
                withGame(sender, args, game -> game.forceStart(sender));
                return true;
            }
            case "stop" -> {
                withGame(sender, args, game -> {
                    game.forceStop();
                    msg.msg(sender, "game.stop");
                });
                return true;
            }
            case "queue" -> {
                withPlayer(sender, args, (target) -> {
                    String type = args.length < 3 ? "1v1" : args[2].toLowerCase();
                    if (!conf.getGameTypes().containsKey(type)) {
                        msg.msg(sender, "game.wrongtype", "type", type, "types", StringUtils.join(conf.getGameTypes().keySet(), ", "));
                        return;
                    }
                    msg.msg(sender, CGAPI.queue(target, type) ? "game.queue" : "game.queuefail", "player", target.getName());
                });
                return true;
            }
        }
        msg.msg(sender, "wrongsub");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (!sender.hasPermission("lsdc.admin"))
            return List.of();

        if (args.length == 1)
            return filterStart(mainCommands, args[0]);

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "arena" -> {
                if (args.length == 2)
                    return filterStart(arenaCommands, args[1]);
                String arenaCmd = args[1].toLowerCase();
                if (args.length == 3) {
                    if (!arenaCmd.equals("create") && arenaCommands.contains(arenaCmd))
                        return filterStart(arenas.keySet(), args[2]);
                    return List.of();
                } else if (arenaCmd.equals("set")) {
                    if (args.length == 4)
                        return filterStart(settings, args[3]);
                    if (args.length == 5 && args[3].equalsIgnoreCase("type"))
                        return filterStart(conf.getGameTypes().keySet(), args[4]);
                }
                return List.of();
            }
            case "start", "stop" -> {
                if (args.length == 2)
                    return filterStart(playerGames.keySet(), args[1]);

                return List.of();
            }
            case "queue" -> {
                if (args.length == 2)
                    return filterStart(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> !playerGames.containsKey(name))
                            .toList(), args[1]);

                if (args.length == 3)
                    return filterStart(conf.getGameTypes().keySet(), args[2]);

                return List.of();
            }
        }
        return List.of();
    }

    private String toStr(Object obj) {
        return obj == null ? msg.get("arena.notset") : obj.toString();
    }

    private void withGame(CommandSender sender, String[] args, Consumer<Game> con) {
        if (args.length == 1 && !(sender instanceof Player)) {
            msg.msg(sender, "player.none");
            return;
        }
        String pln = args.length == 1 ? sender.getName() : args[1];
        Player target = Bukkit.getPlayer(pln);
        if (target == null) {
            msg.msg(sender, "player.wrong", "player", args[0]);
            return;
        }
        Game game = playerGames.get(pln);
        if (game == null) {
            msg.msg(sender, target == sender ? "game.notin" : "game.notinothers");
            return;
        }
        con.accept(game);
    }

    private void withPlayer(CommandSender sender, String[] args, Consumer<Player> con) {
        if (args.length == 1 && !(sender instanceof Player)) {
            msg.msg(sender, "player.none");
            return;
        }
        Player target = args.length == 1 ? (Player) sender : Bukkit.getPlayer(args[1]);
        if (target == null) {
            msg.msg(sender, "player.wrong", "player", args[1]);
            return;
        }
        Game game = playerGames.get(target.getName());
        if (game != null) {
            msg.msg(sender, "game.inothers", "player", target.getName());
            return;
        }
        con.accept(target);
    }
}
