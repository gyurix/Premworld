package gyurix.partysystem.data;

import com.google.common.collect.Lists;
import gyurix.partysystem.PartyAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import static gyurix.partysystem.conf.ConfigManager.conf;
import static gyurix.partysystem.conf.ConfigManager.msg;
import static gyurix.partysystem.util.StrUtils.rand;

@Getter
@Setter
public class Party {
    private final TreeSet<String> members = new TreeSet<>();
    private final TreeSet<String> mods = new TreeSet<>();
    private final TreeSet<String> owners = new TreeSet<>();
    private final TreeMap<String, PartyInvite> invites = new TreeMap<>();
    private boolean open;

    public Party(String owner) {
        this.owners.add(owner);
        PartyAPI.partiesByPlayer.put(owner, this);
    }

    public int countPlayers() {
        return members.size() + mods.size() + owners.size();
    }

    public void disband() {
        msg("disband");
        for (String pln : members)
            PartyAPI.partiesByPlayer.remove(pln);
        for (String pln : mods)
            PartyAPI.partiesByPlayer.remove(pln);
        for (String pln : owners)
            PartyAPI.partiesByPlayer.remove(pln);

        for (PartyInvite pi : invites.values()) {
            Bukkit.getScheduler().cancelTask(pi.getExpirationTaskId());
            pi.expire();
        }
    }

    public void findNextOwner() {
        if (!mods.isEmpty())
            selectNextOwner(Lists.newArrayList(mods));
        else if (!members.isEmpty())
            selectNextOwner(Lists.newArrayList(members));
        else
            disband();
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        members.forEach(pln -> players.add(Bukkit.getPlayer(pln)));
        mods.forEach(pln -> players.add(Bukkit.getPlayer(pln)));
        owners.forEach(pln -> players.add(Bukkit.getPlayer(pln)));
        return players;
    }

    public void invite(Player plr, String pln) {
        Player p = Bukkit.getPlayer(pln);
        if (p == null) {
            msg.msg(plr, "wrongplayer", "player", pln);
            return;
        }
        pln = p.getName();
        if (PartyAPI.partiesByPlayer.get(pln) != null) {
            msg.msg(plr, "in.others", "player", pln);
            return;
        }
        if (invites.containsKey(pln)) {
            msg.msg(plr, "invite.already", "player", pln);
            return;
        }
        if (countPlayers() >= conf.getPlayerLimit()) {
            msg.msg(plr, "full.you");
            return;
        }
        invites.put(pln, new PartyInvite(this, plr.getName(), pln));
        msg("invite.sent", "player", pln);
        msg.msg(plr, "invite.receive", "player", plr.getName());
    }

    public void join(Player plr) {
        String pln = plr.getName();
        msg("join.party", "player", pln);
        members.add(pln);
    }

    public void kick(Player plr, String pln, boolean memberOnly) {
        Party party = PartyAPI.partiesByPlayer.get(pln);
        if (party != this) {
            msg.msg(plr, "notin.others", "player", pln);
            return;
        }
        if (memberOnly && (mods.contains(pln) || owners.contains(pln))) {
            msg.msg(plr, "kick.noperm", "player", pln);
            return;
        }
        msg.msg(Bukkit.getPlayer(pln), "kick.you", "player", plr.getName());
        msgOthers(pln, "kick.party", "player", pln, "kicker", plr.getName());
        leave(pln, true);
    }

    public void leave(String pln, boolean silent) {
        PartyAPI.partiesByPlayer.remove(pln);
        members.remove(pln);
        mods.remove(pln);
        owners.remove(pln);
        if (!silent)
            msg("leave.party", "player", pln);
        if (owners.isEmpty()) {
            findNextOwner();
        }
    }

    public void msg(String key, Object... args) {
        getAllPlayers().forEach(p -> msg.msg(p, key, args));
    }

    public void msgOthers(String skip, String key, Object... args) {
        getAllPlayers().forEach(p -> {
            if (!p.getName().equals(skip))
                msg.msg(p, key, args);
        });
    }

    private void selectNextOwner(List<String> modNames) {
        String mod = modNames.get(rand.nextInt(modNames.size()));
        members.remove(mod);
        mods.remove(mod);
        owners.add(mod);
        msgOthers(mod, "ranks.owner.party", "player", mod);
        msg.msg(Bukkit.getPlayer(mod), "ranks.owner.you");
    }
}
