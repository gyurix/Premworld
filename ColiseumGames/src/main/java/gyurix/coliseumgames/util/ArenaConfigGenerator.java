package gyurix.coliseumgames.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gyurix.coliseumgames.conf.adapters.StringSerializableAdapter;
import gyurix.coliseumgames.data.Arena;
import org.apache.commons.lang.StringUtils;

import java.io.StringReader;
import java.util.TreeMap;

public class ArenaConfigGenerator {
    private static final int offsetZ = 1000;
    public static Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new StringSerializableAdapter())
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static String getType(int id) {
        if (id < 25)
            return "1v1";
        else if (id < 50)
            return "2v2";
        else if (id < 75)
            return "3v3";
        return "4v4";
    }

    public static void main(String[] args) {
        String json = "{\"area\":\"arenas 9923 54 -29 10016 135 29\",\"queue\":\"arenas 9947 63 -15 10001 68 14\",\"team1\":\"arenas 9937 64 -1 9939 64 1\",\"team2\":\"arenas 9999 64 -1 10001 64 1\",\"spec\":\"arenas 9947 63 -15 10001 79 14\",\"name\":\"nether\",\"type\":\"1v1\",\"team1Rot\":-90,\"team2Rot\":90}";
        Arena arena = gson.fromJson(new StringReader(json), Arena.class);
        TreeMap<String, Arena> arenas = new TreeMap<>();
        for (int i = 0; i < 80; ++i) {
            Arena shifted = new Arena();
            shifted.setName(arena.getName() + StringUtils.leftPad(String.valueOf(i + 1), 2, '0'));
            shifted.setArea(arena.getArea().shift(0, 0, offsetZ));
            shifted.setQueue(arena.getQueue().shift(0, 0, offsetZ));
            shifted.setSpec(arena.getSpec().shift(0, 0, offsetZ));
            shifted.setTeam1(arena.getTeam1().shift(0, 0, offsetZ));
            shifted.setTeam2(arena.getTeam2().shift(0, 0, offsetZ));
            shifted.setTeam1Rot(arena.getTeam1Rot());
            shifted.setTeam2Rot(arena.getTeam2Rot());
            shifted.setType(getType(i));
            arenas.put(shifted.getName(), shifted);
        }
        System.out.println(gson.toJson(arenas));
    }
}
