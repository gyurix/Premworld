package gyurix.coliseumgames;

import gyurix.coliseumgames.data.Arena;
import gyurix.coliseumgames.data.Game;
import gyurix.coliseumgames.enums.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.spigotmc.event.entity.EntityDismountEvent;

import static gyurix.coliseumgames.CGPlugin.pl;
import static gyurix.coliseumgames.conf.ConfigManager.arenas;

public class CGListener implements Listener {
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Arena arena : arenas.values()) {
            for (Entity ent : e.getChunk().getEntities()) {
                if (!(ent instanceof Player) && arena.getArea() != null && arena.getArea().contains(ent.getLocation())) {
                    ent.remove();
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        if (ent instanceof Player) {
            Game game = CGAPI.playerGames.get(ent.getName());
            if (game != null)
                e.setCancelled(true);
            return;
        }
        Entity damager = e.getDamager();
        Player dmgr = null;
        if (damager instanceof Player)
            dmgr = (Player) damager;
        else if (damager instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) damager).getShooter();
            if (projectileSource instanceof Player)
                dmgr = (Player) projectileSource;
        }
        if (dmgr == null)
            return;
        Game game = CGAPI.playerGames.get(dmgr.getName());
        if (game == null)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player plr = e.getEntity();
        Game game = CGAPI.playerGames.get(plr.getName());
        if (game == null)
            return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> plr.spigot().respawn(), 2);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = CGAPI.playerGames.get(pln);
        if (game != null)
            game.quit(plr);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player plr = e.getPlayer();
        Game game = CGAPI.playerGames.get(plr.getName());
        if (game == null)
            return;
        e.setRespawnLocation(game.getArena().getSpec().randomLoc());
    }

    @EventHandler
    public void onVehicleDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player plr))
            return;
        Game game = CGAPI.playerGames.get(plr.getName());
        if (game == null || game.getState() != GameState.INGAME)
            return;
        e.setCancelled(true);
    }
}
