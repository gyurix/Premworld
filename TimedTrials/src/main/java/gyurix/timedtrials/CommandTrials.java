package gyurix.timedtrials;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import gyurix.timedtrials.conf.ConfigManager;
import gyurix.timedtrials.data.Area;
import gyurix.timedtrials.data.Arena;
import gyurix.timedtrials.data.Game;
import gyurix.timedtrials.data.Loc;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static gyurix.timedtrials.TTPlugin.pl;
import static gyurix.timedtrials.conf.ConfigManager.conf;
import static gyurix.timedtrials.util.StrUtils.DF;

public class CommandTrials implements CommandExecutor, TabCompleter {
    public static List<String> arenaCommands = List.of("create", "remove", "info", "set");
    public static List<String> mainCommands = List.of("help", "arena", "start", "stop", "queue");
    public static List<String> settings = Lists.newArrayList("area", "finish", "queue", "wall");

    public CommandTrials() {
        PluginCommand cmd = pl.getCommand("trials");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
        for (int i = 1; i <= conf.getMaxPlayers(); ++i) {
            settings.add("spawn" + i);
        }
    }

    @SneakyThrows
    private void arenaCmd(CommandSender sender, String[] args) {
        String sub = args[1].toLowerCase();
        String name = args[2].toLowerCase();
        Arena arena = ConfigManager.arenas.get(name);
        if (arenaUseCheck(sender, sub, name, arena))
            return;

        switch (sub) {
            case "create" -> {
                if (arena != null) {
                    ConfigManager.msg.msg(sender, "arena.exists", "arena", name);
                    return;
                }
                ConfigManager.arenas.put(name, new Arena(name));
                ConfigManager.msg.msg(sender, "arena.create", "arena", name);
                ConfigManager.saveArenas();
                return;
            }
            case "remove" -> {
                ConfigManager.arenas.remove(name);
                ConfigManager.saveArenas();
                return;
            }
            case "info" -> {
                List<String> variables = Lists.newArrayList("arena", name,
                        "area", toStr(arena.getArea()),
                        "wall", toStr(arena.getWall()),
                        "queue", toStr(arena.getQueue()),
                        "queueRot", DF.format(arena.getQueueRot()),
                        "finish", toStr(arena.getFinish()),
                        "configured", arena.isConfigured() ? "§ayes" : "§cno");
                for (int i = 1; i <= conf.getMaxPlayers(); ++i) {
                    variables.add("spawn" + i);
                    variables.add(toStr(arena.getSpawns().size() < i ? null : arena.getSpawns().get(i - 1)));
                }
                ConfigManager.msg.msg(sender, "arena.info", variables.toArray());
                return;
            }
            case "set" -> {
                if (args.length == 3) {
                    ConfigManager.msg.msg(sender, "arena.nosetting", "settings", StringUtils.join(settings, ", "));
                    return;
                }
                String setting = args[3].toLowerCase();
                if (!settings.contains(setting)) {
                    ConfigManager.msg.msg(sender, "arena.wrongsetting", "settings", StringUtils.join(settings, ", "));
                    return;
                }
                if (setting.startsWith("spawn")) {
                    int spawnId = Integer.parseInt(setting.substring(5));
                    arena.setSpawn(spawnId, new Loc(((Player) sender).getLocation()));
                    ConfigManager.msg.msg(sender, "arena.set", "setting", setting, "arena", name, "value", arena.getSpawns().get(spawnId - 1));
                    ConfigManager.saveArenas();
                    return;
                }
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
                        ConfigManager.msg.msg(sender, "arena.nosel");
                        return;
                    }
                    Area area = new Area(bPlayer.getWorld().getName(), region);
                    f.set(arena, area);
                    ConfigManager.msg.msg(sender, "arena.set", "setting", setting, "arena", name, "value", area);
                    if (setting.equals("queue")) {
                        arena.setQueueRot(bPlayer.getLocation().getYaw());
                        ConfigManager.msg.msg(sender, "arena.set", "setting", "queueRot", "arena", name, "value", DF.format(arena.getQueueRot()));
                    }
                }
                ConfigManager.saveArenas();
                return;
            }
        }
        ConfigManager.msg.msg(sender, "arena.wrongsub");
    }

    private boolean arenaUseCheck(CommandSender sender, String sub, String name, Arena arena) {
        if (!sub.equals("create")) {
            if (arena == null) {
                ConfigManager.msg.msg(sender, "arena.notexists", "arena", name);
                return true;
            }
            if (!sub.equals("info")) {
                for (Game game : TTAPI.games) {
                    if (game.getArena() == arena) {
                        ConfigManager.msg.msg(sender, "arena.inuse", "arena", name);
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (!sender.hasPermission("hunt.admin")) {
            sender.sendMessage("§cYou don't have permission for using this command");
            return true;
        }
        String sub = args.length == 0 ? "help" : args[0].toLowerCase();
        switch (sub) {
            case "help" -> {
                ConfigManager.msg.msg(sender, "help");
                return true;
            }
            case "arena" -> {
                if (args.length == 1) {
                    ConfigManager.msg.msg(sender, "arena.help",
                            "settings", StringUtils.join(settings, ", "),
                            "arenas", StringUtils.join(ConfigManager.arenas.keySet(), ", "));
                    return true;
                }
                if (args.length == 2) {
                    ConfigManager.msg.msg(sender, "arena.none",
                            "arenas", StringUtils.join(ConfigManager.arenas.keySet(), ", "));
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
                    ConfigManager.msg.msg(sender, "game.stop");
                });
                return true;
            }
            case "queue" -> {
                withPlayer(sender, args,
                        target -> ConfigManager.msg.msg(sender, TTAPI.queue(target) ? "game.queue" : "game.queuefail", "player", target.getName()));
                return true;
            }
        }
        ConfigManager.msg.msg(sender, "wrongsub");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (!sender.hasPermission("hunt.admin"))
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
                        return filterStart(ConfigManager.arenas.keySet(), args[2]);
                    return List.of();
                } else if (arenaCmd.equals("set")) {
                    if (args.length == 4)
                        return filterStart(settings, args[3]);
                }
                return List.of();
            }
            case "start", "stop" -> {
                if (args.length == 2)
                    return filterStart(TTAPI.playerGames.keySet(), args[1]);

                return List.of();
            }
            case "queue" -> {
                if (args.length == 2)
                    return filterStart(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> !TTAPI.playerGames.containsKey(name))
                            .toList(), args[1]);

                return List.of();
            }
        }
        return List.of();
    }

    private String toStr(Object obj) {
        return obj == null ? ConfigManager.msg.get("arena.notset") : obj.toString();
    }

    private void withGame(CommandSender sender, String[] args, Consumer<Game> con) {
        if (args.length == 1 && !(sender instanceof Player)) {
            ConfigManager.msg.msg(sender, "player.none");
            return;
        }
        String pln = args.length == 1 ? sender.getName() : args[1];
        Player target = Bukkit.getPlayer(pln);
        if (target == null) {
            ConfigManager.msg.msg(sender, "player.wrong", "player", pln);
            return;
        }
        Game game = TTAPI.playerGames.get(pln);
        if (game == null) {
            ConfigManager.msg.msg(sender, target == sender ? "game.notin" : "game.notinothers", "player", target.getName());
            return;
        }
        con.accept(game);
    }

    private void withPlayer(CommandSender sender, String[] args, Consumer<Player> con) {
        if (args.length == 1 && !(sender instanceof Player)) {
            ConfigManager.msg.msg(sender, "player.none");
            return;
        }
        Player target = args.length == 1 ? (Player) sender : Bukkit.getPlayer(args[1]);
        if (target == null) {
            ConfigManager.msg.msg(sender, "player.wrong", "player", args[1]);
            return;
        }
        Game game = TTAPI.playerGames.get(target.getName());
        if (game != null) {
            ConfigManager.msg.msg(sender, "game.inothers", "player", target.getName());
            return;
        }
        con.accept(target);
    }
}
