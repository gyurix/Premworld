package gyurix.timedtrials.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static gyurix.timedtrials.conf.ConfigManager.conf;

@Getter
@Setter
@NoArgsConstructor
public class Arena {
    private Area area, queue, wall, finish;
    private String name;

    private List<Loc> spawns = new ArrayList<>();
    @Setter
    private float queueRot;

    public Arena(String name) {
        this.name = name;
    }

    public boolean isConfigured() {
        return name != null && area != null && queue != null && finish != null && wall != null && !spawns.contains(null) && spawns.size() >= conf.getMaxPlayers();
    }

    public void setSpawn(int spawnId, Loc loc) {
        while (spawns.size() < spawnId)
            spawns.add(null);
        spawns.set(spawnId - 1, loc);
    }
}
