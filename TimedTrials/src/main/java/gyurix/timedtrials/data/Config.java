package gyurix.timedtrials.data;

import lombok.Getter;

@SuppressWarnings("unused")
@Getter
public class Config {
    private Counter counters;
    private int drawExp, winExp, loseExp;
    private int minPlayers, maxPlayers;
    private int titleFadeIn, titleShowTime, titleFadeOut;
}
