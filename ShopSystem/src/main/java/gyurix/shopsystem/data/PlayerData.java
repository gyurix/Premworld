package gyurix.shopsystem.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;

import static gyurix.shopsystem.PlayerManager.save;

@Getter
@NoArgsConstructor
public class PlayerData {
    private HashMap<String, Long> boughtItems = new HashMap<>();
    private UUID uuid;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        save(this);
    }

    public String getName() {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public void addBoughtItem(String item, long expiration){
        boughtItems.put(item, expiration);
        save(this);
    }
}
