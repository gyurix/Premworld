package gyurix.coliseumgames;

import com.nftworlds.wallet.event.PlayerTransactEvent;
import gyurix.coliseumgames.data.Arena;
import gyurix.coliseumgames.data.Game;
import gyurix.coliseumgames.enums.GameState;
import gyurix.coliseumgames.gui.UpgradeRunnable;
import gyurix.coliseumgames.gui.UpgradesGUI;
import gyurix.coliseumgames.util.LocUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.spigotmc.event.entity.EntityDismountEvent;

import static gyurix.coliseumgames.CGPlugin.pl;
import static gyurix.coliseumgames.conf.ConfigManager.arenas;
import static gyurix.coliseumgames.conf.ConfigManager.conf;
import static gyurix.coliseumgames.conf.ConfigManager.msg;

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
    public void onClick(PlayerInteractEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        ItemStack is = e.getItem();
        if (is != null && is.hasItemMeta() && conf.getUpgradeItem().getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName())) {
            e.setCancelled(true);
            Game game = CGAPI.playerGames.get(pln);
            if (game == null)
                return;
            new UpgradesGUI(plr, conf.getGuis().get(game.getType().getUpgradesGUI()));
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = CGAPI.playerGames.get(pln);
        if (game != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = CGAPI.playerGames.get(pln);
        if (game != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
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
        Game game1 = CGAPI.playerGames.get(dmgr.getName());
        if (!(ent instanceof Player victim)) {
            System.out.println("Damage - " + ent.getUniqueId() + " - F1: " + game1.getTeam1Flag().getUniqueId() + ", F2: " + game1.getTeam2Flag().getUniqueId());
            if (game1.getTeam1().containsKey(dmgr.getName()) && game1.getTeam1Carrier() == null
                    && ent.getUniqueId().equals(game1.getTeam2Flag().getUniqueId()))
                game1.pickupFlag(dmgr, false);
            else if (game1.getTeam2().containsKey(dmgr.getName()) && game1.getTeam2Carrier() == null
                    && ent.getUniqueId().equals(game1.getTeam1Flag().getUniqueId()))
                game1.pickupFlag(dmgr, true);
            return;
        }
        Game game2 = CGAPI.playerGames.get(ent.getName());
        if (game1 == null && game2 == null)
            return;
        if (game1 != game2) {
            msg.msg(dmgr, "game.nodamage", "player", victim.getName());
            e.setCancelled(true);
            return;
        }
        if (game1.getState() != GameState.INGAME ||
                game1.getTeam2().containsKey(dmgr.getName()) == game1.getTeam2().containsKey(victim.getName())) {
            msg.msg(dmgr, "game.nodamage", "player", victim.getName());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player plr = e.getEntity();
        String pln = plr.getName();
        Game game = CGAPI.playerGames.get(pln);
        if (game == null)
            return;
        e.setCancelled(true);
        if (game.getType().getFlagCount() > 0) {
            if (pln.equals(game.getTeam1Carrier()))
                game.resetFlag(true);
            else if (pln.equals(game.getTeam2Carrier()))
                game.resetFlag(false);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> {
            if (game.getType().getFlagCount() > 0) {
                Arena arena = game.getArena();
                plr.teleport(LocUtils.fixLoc((game.getTeam2().containsKey(pln) ? arena.getTeam2() : arena.getTeam1()).randomLoc()));
            } else
                game.spectate(plr);
        }, 2);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player plr = e.getPlayer();
        Game game = CGAPI.playerGames.get(plr.getName());
        if (game != null) {
            msg.msg(plr, "game.nodrop");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTransact(PlayerTransactEvent<?> e) {
        if (e.getPayload() instanceof UpgradeRunnable)
            ((UpgradeRunnable) e.getPayload()).run();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = CGAPI.playerGames.get(pln);
        if (game != null)
            game.quit(plr);
    }
}
