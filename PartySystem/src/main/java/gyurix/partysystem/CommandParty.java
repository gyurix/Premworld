package gyurix.partysystem;

import gyurix.partysystem.data.Party;
import gyurix.partysystem.data.PartyInvite;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static gyurix.partysystem.conf.ConfigManager.conf;
import static gyurix.partysystem.conf.ConfigManager.msg;

@SuppressWarnings("NullableProblems")
public class CommandParty implements CommandExecutor {
    public CommandParty() {
        PluginCommand cmd = PartySystem.pl.getCommand("party");
        cmd.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player plr)) {
            msg.msg(sender, "noconsole");
            return true;
        }
        String sub = args.length == 0 ? "help" : args[0].toLowerCase();
        Party party = PartyAPI.partiesByPlayer.get(plr.getName());
        switch (sub) {
            case "help" -> {
                cmdHelp(party, plr, alias);
                return true;
            }
            case "create", "invite" -> {
                if (party == null) {
                    cmdCreate(plr, Arrays.copyOfRange(args, 1, args.length));
                    return true;
                }
                cmdInvite(party, plr, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            case "join" -> {
                if (args.length == 1) {
                    msg.msg(plr, "noplayer");
                    return true;
                }
                cmdJoin(plr, args[1]);
                return true;
            }
            case "info" -> {
                cmdInfo(party, plr);
                return true;
            }
            case "leave" -> {
                cmdLeave(party, plr);
                return true;
            }
            case "kick" -> {
                cmdKick(party, plr, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            case "open" -> {
                cmdOpen(party, plr);
                return true;
            }
            case "close" -> {
                cmdClose(party, plr);
                return true;
            }
            case "disband" -> {
                cmdDisband(party, plr);
                return true;
            }
            case "demote" -> {
                cmdDemote(party, plr, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            case "promote" -> {
                cmdPromote(party, plr, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            case "owner", "setowner" -> {
                cmdOwner(party, plr, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
        }
        if (party == null) {
            cmdCreate(plr, args);
            return true;
        }
        cmdInvite(party, plr, args);
        return true;
    }

    private void cmdClose(Party party, Player plr) {
        if (party == null) {
            msg.msg(plr, "notin");
            return;
        }
        if (!party.isOpen()) {
            msg.msg(plr, "close.already");
            return;
        }
        if (!party.getOwners().contains(plr.getName())) {
            msg.msg(plr, "close.noperm");
            return;
        }
        party.setOpen(false);
        msg.msg(plr, "close.done");
    }

    private void cmdCreate(Player plr, String[] playerNames) {
        if (playerNames.length == 0) {
            msg.msg(plr, "noplayer");
            return;
        }
        if (playerNames.length == 1) {
            Player p = Bukkit.getPlayer(playerNames[0]);
            if (p == null) {
                msg.msg(plr, "wrongplayer", "player", playerNames[0]);
                return;
            }
            Party party = PartyAPI.partiesByPlayer.get(p.getName());
            if (party != null) {
                cmdJoin(plr, p.getName());
                return;
            }
        }
        Party party = new Party(plr.getName());
        msg.msg(plr, "create");
        for (String pln : playerNames) {
            party.invite(plr, pln);
        }
    }

    private void cmdDemote(Party party, Player plr, String[] playerNames) {
        if (isRankChangeBlocked(party, plr, playerNames))
            return;
        for (String pln : playerNames) {
            Player p = Bukkit.getPlayer(pln);
            if (p == null) {
                msg.msg(plr, "wrongplayer", "player", pln);
                continue;
            }
            pln = p.getName();
            if (PartyAPI.partiesByPlayer.get(pln) != party) {
                msg.msg(plr, "notin.others", "player", pln);
                continue;
            }
            if (party.getMembers().contains(pln)) {
                msg.msg(plr, "ranks.member.already", "player", pln);
                continue;
            }
            if (party.getOwners().remove(pln)) {
                party.getMods().add(pln);
                msg.msg(p, "ranks.moddemote.you");
                party.msgOthers(pln, "ranks.moddemote.party", "player", pln);
                continue;
            }
            party.getMods().remove(pln);
            party.getMembers().add(pln);
            msg.msg(p, "ranks.member.you");
            party.msgOthers(pln, "ranks.member.party", "player", pln);
        }
    }

    private void cmdDisband(Party party, Player plr) {
        if (party == null) {
            msg.msg(plr, "notin");
            return;
        }
        if (!party.getOwners().contains(plr.getName())) {
            msg.msg(plr, "disband.noperm");
            return;
        }
        party.disband();
    }

    private void cmdHelp(Party party, Player plr, String alias) {
        msg.msg(plr, "help.header");
        if (party == null) {
            msg.msg(plr, "help.noparty", "cmd", alias);
            return;
        }
        msg.msg(plr, "help.everyone", "cmd", alias);
        if (party.getOwners().contains(plr.getName())) {
            msg.msg(plr, "help.mods", "cmd", alias);
            msg.msg(plr, "help.owners", "cmd", alias);
        } else if (party.getMods().contains(plr.getName())) {
            msg.msg(plr, "help.mods", "cmd", alias);
        }
    }

    private void cmdInfo(Party party, Player plr) {
        if (party == null) {
            msg.msg(plr, "notin.you");
            return;
        }
        msg.msg(plr, "info",
            "count", party.countPlayers(),
            "limit", conf.getPlayerLimit(),
            "members", StringUtils.join(party.getMembers(), ", "),
            "memberCount", party.getMembers().size(),
            "mods", StringUtils.join(party.getMods(), ", "),
            "modCount", party.getMods().size(),
            "owners", StringUtils.join(party.getOwners(), ", "),
            "ownerCount", party.getOwners().size());
    }

    private void cmdInvite(Party party, Player plr, String[] playerNames) {
        if (playerNames.length == 0) {
            msg.msg(plr, "noplayer");
            return;
        }
        if (party.getMembers().contains(plr.getName())) {
            msg.msg(plr, "invite.nomod");
            return;
        }
        for (String pln : playerNames) {
            party.invite(plr, pln);
        }
    }

    private void cmdJoin(Player plr, String target) {
        Player p = Bukkit.getPlayer(target);
        if (p == null) {
            msg.msg(plr, "wrongplayer", "player", target);
            return;
        }
        target = p.getName();
        Party party = PartyAPI.partiesByPlayer.get(target);
        if (party == null) {
            msg.msg(plr, "notin.any", "player", target);
            return;
        }
        PartyInvite invite = party.getInvites().get(plr.getName());
        if (invite != null) {
            invite.accept(plr);
            return;
        }
        if (!party.isOpen()) {
            msg.msg(plr, "invite.no", "player", target);
            return;
        }
        if (party.countPlayers() >= conf.getPlayerLimit()) {
            msg.msg(plr, "full.others", "player", target);
            return;
        }
        msg.msg(plr, "join.you", "player", target);
        party.join(plr);
    }

    private void cmdKick(Party party, Player plr, String[] playerNames) {
        if (party == null) {
            msg.msg(plr, "notin.you");
            return;
        }
        if (playerNames.length == 0) {
            msg.msg(plr, "noplayer");
            return;
        }
        if (party.getMembers().contains(plr.getName())) {
            msg.msg(plr, "kick.nomod");
            return;
        }
        boolean memberOnly = party.getMods().contains(plr.getName());
        for (String pln : playerNames) {
            Player p = Bukkit.getPlayer(pln);
            if (p == null) {
                msg.msg(plr, "wrongplayer", "player", pln);
                continue;
            }
            pln = p.getName();
            party.kick(plr, pln, memberOnly);
        }
    }

    private void cmdLeave(Party party, Player plr) {
        if (party == null) {
            msg.msg(plr, "notin");
            return;
        }
        msg.msg(plr, "leave.you");
        party.leave(plr.getName(), false);
    }

    private void cmdOpen(Party party, Player plr) {
        if (party == null) {
            msg.msg(plr, "notin");
            return;
        }
        if (party.isOpen()) {
            msg.msg(plr, "open.already");
            return;
        }
        if (!party.getOwners().contains(plr.getName())) {
            msg.msg(plr, "open.noperm");
            return;
        }
        party.setOpen(true);
        msg.msg(plr, "open.done");
    }

    private void cmdOwner(Party party, Player plr, String[] playerNames) {
        if (isRankChangeBlocked(party, plr, playerNames))
            return;
        for (String pln : playerNames) {
            Player p = Bukkit.getPlayer(pln);
            if (p == null) {
                msg.msg(plr, "wrongplayer", "player", pln);
                continue;
            }
            pln = p.getName();
            if (PartyAPI.partiesByPlayer.get(pln) != party) {
                msg.msg(plr, "notin.others", "player", pln);
                continue;
            }
            if (party.getOwners().contains(pln)) {
                msg.msg(plr, "ranks.owner.already", "player", pln);
                continue;
            }
            party.getMembers().remove(pln);
            party.getMods().remove(pln);
            party.getOwners().add(pln);
            msg.msg(p, "ranks.owner.you");
            party.msgOthers(pln, "ranks.owner.party", "player", pln);
        }
    }

    private void cmdPromote(Party party, Player plr, String[] playerNames) {
        if (isRankChangeBlocked(party, plr, playerNames))
            return;
        for (String pln : playerNames) {
            Player p = Bukkit.getPlayer(pln);
            if (p == null) {
                msg.msg(plr, "wrongplayer", "player", pln);
                continue;
            }
            pln = p.getName();
            if (PartyAPI.partiesByPlayer.get(pln) != party) {
                msg.msg(plr, "notin.others", "player", pln);
                continue;
            }
            if (party.getOwners().contains(pln)) {
                msg.msg(plr, "ranks.owner.already", "player", pln);
                continue;
            }
            if (party.getMembers().remove(pln)) {
                party.getMods().add(pln);
                msg.msg(p, "ranks.modpromote.you");
                party.msgOthers(pln, "ranks.modpromote.party", "player", pln);
                continue;
            }
            party.getMods().remove(pln);
            party.getOwners().add(pln);
            msg.msg(p, "ranks.owner.you");
            party.msgOthers(pln, "ranks.owner.party", "player", pln);
        }
    }

    private boolean isRankChangeBlocked(Party party, Player plr, String[] playerNames) {
        if (party == null) {
            msg.msg(plr, "notin");
            return true;
        }
        if (playerNames.length == 0) {
            msg.msg(plr, "noplayer");
            return true;
        }
        if (!party.getOwners().contains(plr.getName())) {
            msg.msg(plr, "ranks.noperm");
            return true;
        }
        return false;
    }
}
