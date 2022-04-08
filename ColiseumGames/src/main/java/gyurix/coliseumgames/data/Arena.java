package gyurix.coliseumgames.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class Arena {
    private Area area, queue, team1, team2, spec;
    private String name;
    @Setter
    private String type;

    public Arena(String name) {
        this.name = name;
    }

    public boolean isConfigured() {
        return name != null && type != null && area != null && queue != null && team1 != null && team2 != null && spec != null;
    }
}
