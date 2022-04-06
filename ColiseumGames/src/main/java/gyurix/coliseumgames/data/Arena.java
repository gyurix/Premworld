package gyurix.coliseumgames.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class Arena {
    private Area area, queue, start, finish, spec;
    private String name;
    @Setter
    private String type;

    public Arena(String name) {
        this.name = name;
    }

    public boolean isConfigured() {
        return name != null && type != null && area != null && queue != null && start != null && finish != null && spec != null;
    }
}
