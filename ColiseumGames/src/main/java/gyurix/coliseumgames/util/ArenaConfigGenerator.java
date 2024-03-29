package gyurix.coliseumgames.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gyurix.coliseumgames.conf.adapters.StringSerializableAdapter;
import gyurix.coliseumgames.data.Arena;
import org.apache.commons.lang.StringUtils;

import java.io.StringReader;
import java.util.TreeMap;
import java.util.function.Function;

public class ArenaConfigGenerator {
    private static final int offsetZ = 1000;
    public static Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new StringSerializableAdapter())
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static void generateArenas(TreeMap<String, Arena> arenas, String arenaJson, Function<Integer, String> typeGetter) {
        Arena arena = gson.fromJson(new StringReader(arenaJson), Arena.class);
        for (int i = 0; i < 80; ++i) {
            Arena shifted = new Arena();
            shifted.setName(arena.getName() + StringUtils.leftPad(String.valueOf(i + 1), 2, '0'));
            shifted.setArea(arena.getArea().shift(0, 0, offsetZ * i));
            shifted.setQueue(arena.getQueue().shift(0, 0, offsetZ * i));
            shifted.setSpec(arena.getSpec().shift(0, 0, offsetZ * i));
            shifted.setTeam1(arena.getTeam1().shift(0, 0, offsetZ * i));
            shifted.setTeam2(arena.getTeam2().shift(0, 0, offsetZ * i));
            shifted.setTeam1Rot(arena.getTeam1Rot());
            shifted.setTeam2Rot(arena.getTeam2Rot());
            shifted.setType(typeGetter.apply(i));
            arenas.put(shifted.getName(), shifted);
        }
    }

    public static String getNetherArenaType(int id) {
        if (id < 30)
            return "1v1";
        else if (id < 60)
            return "2v2";
        else if (id < 70)
            return "3v3";
        return "4v4";
    }

    public static String getBoatArenaType(int id) {
        if (id < 20)
            return "1v1";
        else if (id < 40)
            return "2v2";
        if (id < 50)
            return "3v3";
        else if (id < 60)
            return "4v4";
        else if (id < 70)
            return "ctf1";
        return "ctf2";
    }

    public static String getTowerArenaType(int id) {
        if (id < 20)
            return "1v1";
        else if (id < 40)
            return "2v2";
        if (id < 50)
            return "3v3";
        else if (id < 60)
            return "4v4";
        else if (id < 70)
            return "ctf1";
        return "ctf2";
    }

    public static String getHiddenArenaType(int id) {
        if (id < 20)
            return "1v1";
        else if (id < 40)
            return "2v2";
        if (id < 50)
            return "3v3";
        else if (id < 60)
            return "4v4";
        else if (id < 70)
            return "ctf1";
        return "ctf2";
    }

    public static void main(String[] args) {
        TreeMap<String, Arena> arenas = new TreeMap<>();

        String boatArenaJson = "{\"area\":\"arenas 8872 46 -90 9056 179 108\",\"queue\":\"arenas 8944 64 -17 8981 74 19\",\"team1\":\"arenas 8918 64 -2 8922 68 2\",\"team2\":\"arenas 8998 64 -2 9002 69 2\",\"spec\":\"arenas 8872 46 -90 9056 179 108\",\"name\":\"boat\",\"type\":\"ctf1\",\"team1Rot\":-90,\"team2Rot\":90}";
        generateArenas(arenas, boatArenaJson, ArenaConfigGenerator::getBoatArenaType);

        String netherArenaJson = "{\"area\":\"arenas 9923 54 -29 10016 135 29\",\"queue\":\"arenas 9947 63 -15 10001 68 14\",\"team1\":\"arenas 9937 64 -1 9939 64 1\",\"team2\":\"arenas 9999 64 -1 10001 64 1\",\"spec\":\"arenas 9947 63 -15 10001 79 14\",\"name\":\"nether\",\"type\":\"1v1\",\"team1Rot\":-90,\"team2Rot\":90}";
        generateArenas(arenas, netherArenaJson, ArenaConfigGenerator::getNetherArenaType);

        String towerArenaJson = "{\"area\":\"arenas 7975 53 -33 8077 134 34\",\"queue\":\"arenas 8017 61 -9 8033 64 6\",\"team1\":\"arenas 8062 64 2 8066 64 6\",\"team2\":\"arenas 7998 64 -2 8002 64 2\",\"spec\":\"arenas 7975 53 -33 8077 134 34\",\"name\":\"tower\",\"type\":\"1v1\",\"team1Rot\":90,\"team2Rot\":-90}";
        generateArenas(arenas, towerArenaJson, ArenaConfigGenerator::getTowerArenaType);

        String hiddenArenaJson = "{\"area\":\"arenas 6966 47 -67 7146 147 59\",\"queue\":\"arenas 7051 61 -14 7070 61 4\",\"team1\":\"arenas 7001 64 -3 7010 64 3\",\"team2\":\"arenas 7112 64 -3 7121 64 3\",\"spec\":\"arenas 6966 47 -67 7146 147 59\",\"name\":\"hidden\",\"type\":\"1v1\",\"team1Rot\":-90,\"team2Rot\":90}";
        generateArenas(arenas, hiddenArenaJson, ArenaConfigGenerator::getHiddenArenaType);

        System.out.println(gson.toJson(arenas));
    }
}
