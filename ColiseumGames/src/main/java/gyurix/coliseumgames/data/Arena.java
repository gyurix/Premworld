package gyurix.coliseumgames.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Arena {
    private Area area, queue, team1, team2, spec;
    private String name;
    private String type;
    private float team1Rot, team2Rot;

    public Arena(String name) {
        this.name = name;
    }

    public boolean isConfigured() {
        return name != null && type != null && area != null && queue != null && team1 != null && team2 != null && spec != null;
    }
}
