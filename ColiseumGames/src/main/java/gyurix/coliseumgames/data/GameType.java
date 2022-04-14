package gyurix.coliseumgames.data;

import lombok.Data;

import java.util.List;

@Data
public class GameType {
    private String name;
    private int minPlayersPerTeam;
    private int maxPlayersPerTeam;
    private int winExp;
    private int loseExp;
    private int drawExp;
    private Counter counters;
    private List<String> defaultUpgrades;
    private int flagCount;
    private String upgradesGUI;

    @Override
    public String toString() {
        return name;
    }
}
