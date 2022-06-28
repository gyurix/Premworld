package gyurix.shopsystem;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import gyurix.shopsystem.data.PlayerData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static gyurix.shopsystem.ShopSystem.pl;
import static gyurix.shopsystem.conf.ConfigManager.conf;
import static gyurix.shopsystem.conf.ConfigManager.gson;

public class PlayerManager {
    private static final LoadingCache<UUID, PlayerData> players = CacheBuilder
            .newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public PlayerData load(@NotNull UUID uuid) {
                    AtomicReference<PlayerData> out = new AtomicReference<>();
                    conf.mySQL.query("SELECT `data` FROM `" + conf.mySQL.table + "` WHERE `uuid` = ? LIMIT 1", (rs) -> {
                        if (rs.next())
                            out.set(gson.fromJson(rs.getString(1), PlayerData.class));
                    }, uuid);
                    if (out.get() == null)
                        out.set(new PlayerData(uuid));
                    return out.get();
                }
            });

    public static void initTable() {
        Bukkit.getScheduler().runTaskAsynchronously(pl,
                () -> conf.mySQL.command("CREATE TABLE IF NOT EXISTS `" + conf.mySQL.table + "` (`uuid` CHAR(40) UNIQUE PRIMARY KEY, `data` MEDIUMTEXT)"));

    }

    public static void save(PlayerData playerData) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            int updated = conf.mySQL.update("UPDATE `" + conf.mySQL.table + "` SET `data` = ? WHERE `uuid` = ?",
                    gson.toJson(playerData), playerData.getUuid());
            if (updated == 0)
                conf.mySQL.update("INSERT INTO `" + conf.mySQL.table + "` VALUES ( ?, ? )", playerData.getUuid(), gson.toJson(playerData));
        });
    }

    public static void withPlayerData(UUID uuid, Consumer<PlayerData> con) {
        PlayerData cachedShop = players.getIfPresent(uuid);
        if (cachedShop != null) {
            con.accept(cachedShop);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            PlayerData playerData = players.getUnchecked(uuid);
            Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> con.accept(playerData));
        });
    }
}
