package gyurix.huntinggames.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;

@Getter
@NoArgsConstructor
public class Arena {
    private Area area, queue, spawn;
    private String name;
    @Setter
    private float spawnRot;

    public Arena(String name) {
        this.name = name;
    }

    public boolean isConfigured() {
        return name != null && area != null && queue != null;
    }
}
