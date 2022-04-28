package gyurix.timedtrials;

import gyurix.timedtrials.conf.ConfigManager;
import gyurix.timedtrials.data.Arena;
import gyurix.timedtrials.data.Game;
import gyurix.timedtrials.enums.GameState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.spigotmc.event.entity.EntityDismountEvent;

public class TTListener implements Listener {
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Arena arena : ConfigManager.arenas.values()) {
            for (Entity ent : e.getChunk().getEntities()) {
                if (!(ent instanceof Player) && arena.getArea() != null && arena.getArea().contains(ent.getLocation())) {
                    ent.remove();
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = TTAPI.playerGames.get(pln);
        if (game != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = TTAPI.playerGames.get(pln);
        if (game != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        if (!(ent instanceof LivingEntity))
            return;
        Entity damager = e.getDamager();
        Player dmgr = null;
        if (damager instanceof Player)
            dmgr = (Player) damager;
        else if (damager instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) damager).getShooter();
            if (projectileSource instanceof Player)
                dmgr = (Player) projectileSource;
        }
        if (dmgr == null) {
            for (Game game : TTAPI.games)
                if (game.getArena().getArea().contains(ent.getLocation()))
                    e.setCancelled(true);
            return;
        }
        Game game = TTAPI.playerGames.get(dmgr.getName());
        if (game == null)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player plr = e.getEntity();
        String pln = plr.getName();
        Game game = TTAPI.playerGames.get(pln);
        if (game != null)
            game.quit(plr);
    }

    @EventHandler
    public void onDismount(VehicleExitEvent e) {
        if (!(e.getExited() instanceof Player))
            return;
        String pln = e.getExited().getName();
        Game game = TTAPI.playerGames.get(pln);
        if (game != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent e) {
        if (TTAPI.playerGames.get(e.getAttacker().getName()) != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player plr = e.getPlayer();
        Game game = TTAPI.playerGames.get(plr.getName());
        if (game != null) {
            ConfigManager.msg.msg(plr, "game.nodrop");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = TTAPI.playerGames.get(pln);
        if (game != null)
            game.quit(plr);
    }
}
