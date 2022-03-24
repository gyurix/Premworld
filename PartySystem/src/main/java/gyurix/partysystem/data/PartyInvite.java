package gyurix.partysystem.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static gyurix.partysystem.PartySystem.pl;
import static gyurix.partysystem.conf.ConfigManager.conf;
import static gyurix.partysystem.conf.ConfigManager.msg;

@Getter
public class PartyInvite {
    private int expirationTaskId;
    private Party party;
    private String receiver;
    private String sender;

    public PartyInvite(Party party, String sender, String receiver) {
        this.party = party;
        this.sender = sender;
        this.receiver = receiver;
        expirationTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(pl, this::expire);
    }

    public void accept(Player plr) {
        if (party.countPlayers() >= conf.getPlayerLimit()) {
            msg.msg(plr, "full.others", "player", sender);
            return;
        }
        Bukkit.getScheduler().cancelTask(expirationTaskId);
        party.getInvites().remove(receiver);
        msg.msg(plr, "join.you", "player", sender);
        party.join(plr);
    }

    public void expire() {
        party.msg("invite.expire.sender", "player", receiver);
        Player p = Bukkit.getPlayerExact(receiver);
        if (p != null)
            msg.msg(p, "invite.expire.receiver", "player", sender);
        party.getInvites().remove(receiver);
    }
}
