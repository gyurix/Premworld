package gyurix.huntinggames;

import com.nftworlds.wallet.event.PlayerTransactEvent;
import gyurix.huntinggames.data.Arena;
import gyurix.huntinggames.data.Game;
import gyurix.huntinggames.data.Loc;
import gyurix.huntinggames.data.Upgrade;
import gyurix.huntinggames.gui.HuntingUpgradesGUI;
import gyurix.huntinggames.gui.UpgradeRunnable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import static gyurix.huntinggames.conf.ConfigManager.arenas;
import static gyurix.huntinggames.conf.ConfigManager.conf;
import static gyurix.huntinggames.conf.ConfigManager.msg;

public class HGListener implements Listener {
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
        if (is != null && is.hasItemMeta()) {
            ItemMeta meta = is.getItemMeta();
            if (!meta.hasDisplayName())
                return;
            String dn = meta.getDisplayName();
            Game game = HGAPI.playerGames.get(pln);
            if (game == null)
                return;
            e.setCancelled(true);
            if (dn.equals(conf.getUpgradeItem().getItemMeta().getDisplayName()))
                new HuntingUpgradesGUI(plr);
            else if (dn.equals(conf.getUpgrades().get("shotgun").getItem().getItemMeta().getDisplayName()))
                game.getPlayers().get(pln).useShotgun();
            else if (dn.equals(conf.getUpgrades().get("rifle").getItem().getItemMeta().getDisplayName()))
                game.getPlayers().get(pln).useRifle();
            else if (dn.equals(conf.getUpgrades().get("trap").getItem().getItemMeta().getDisplayName()))
                game.getPlayers().get(pln).useTrap();
            else if (dn.equals(conf.getUpgrades().get("lure").getItem().getItemMeta().getDisplayName()))
                game.getPlayers().get(pln).useLure();
            else
                e.setCancelled(false);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = HGAPI.playerGames.get(pln);
        if (game != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent e) {
        Player plr = e.getPlayer();
        String pln = plr.getName();
        Game game = HGAPI.playerGames.get(pln);
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
            for (Game game : HGAPI.games)
                if (game.getArena().getArea().contains(ent.getLocation()))
                    e.setCancelled(true);
            return;
        }
        Game game = HGAPI.playerGames.get(dmgr.getName());
        if (game == null)
            return;
        if (ent instanceof Player) {
            e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        double damage = e.getDamage();
        if (damager instanceof Arrow) {
            double multiplier = Math.max(0, Math.min((damage - 1) / 5, 1));
            Upgrade bowUpgrade = conf.getUpgrades().get("bow");
            damage = bowUpgrade.getMinDamage() + multiplier * (bowUpgrade.getDamage() - bowUpgrade.getMinDamage());
            damager.remove();
        } else if (damager instanceof Snowball) {
            Upgrade rifleUpgrade = conf.getUpgrades().get("rifle");
            damage = rifleUpgrade.getDamage();
            damager.remove();
        } else {
            ItemStack is = damager == dmgr ? dmgr.getInventory().getItemInMainHand() : null;
            if (is != null && is.getType() == Material.WOODEN_SWORD) {
                damage = conf.getUpgrades().get("wooden_sword").getDamage();
            }
        }
        game.damageMob(dmgr, (LivingEntity) ent, damage);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player plr = e.getEntity();
        String pln = plr.getName();
        Game game = HGAPI.playerGames.get(pln);
        if (game != null)
            game.quit(plr);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityPortalEnter(EntityPortalEvent e) {
        if (!(e.getEntity() instanceof LivingEntity))
            return;
        for (Game game : HGAPI.games) {
            Location from = e.getFrom();
            for (int xFix = -2; xFix <= 2; ++xFix) {
                for (int yFix = -1; yFix <= 1; ++yFix) {
                    for (int zFix = -2; zFix <= 2; ++zFix) {
                        Loc loc = new Loc(from.getBlock().getRelative(xFix, yFix, zFix));
                        if (game.getPortalOwners().containsKey(loc)) {
                            e.setCancelled(true);
                            game.damageMob(game.removePortalOwner(loc), (LivingEntity) e.getEntity(), 1000000);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player plr = e.getPlayer();
        Game game = HGAPI.playerGames.get(plr.getName());
        if (game != null) {
            msg.msg(plr, "game.nodrop");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPortalEnter(PlayerPortalEvent e) {
        Game game = HGAPI.playerGames.get(e.getPlayer().getName());
        if (game != null)
            e.setCancelled(true);
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
        Game game = HGAPI.playerGames.get(pln);
        if (game != null)
            game.quit(plr);
    }
}
