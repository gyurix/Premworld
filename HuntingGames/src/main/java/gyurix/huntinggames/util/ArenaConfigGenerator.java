package gyurix.huntinggames.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gyurix.huntinggames.conf.adapters.StringSerializableAdapter;
import gyurix.huntinggames.data.Arena;
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

    public static void generateArenas(TreeMap<String, Arena> arenas, String arenaJson) {
        Arena arena = gson.fromJson(new StringReader(arenaJson), Arena.class);
        for (int i = 0; i < 20; ++i) {
            Arena shifted = new Arena();
            shifted.setArea(arena.getArea().shift(0, 0, offsetZ * i));
            shifted.setName(arena.getName() + StringUtils.leftPad(String.valueOf(i + 1), 2, '0'));
            shifted.setQueue(arena.getQueue().shift(0, 0, offsetZ * i));
            shifted.setSpawn(arena.getSpawn().shift(0, 0, offsetZ * i));
            shifted.setQueueRot(arena.getQueueRot());
            shifted.setSpawnRot(arena.getSpawnRot());
            arenas.put(shifted.getName(), shifted);
        }
    }

    public static void main(String[] args) {
        TreeMap<String, Arena> arenas = new TreeMap<>();

        String arenaJson = "{\"area\":\"arenas 4859 58 -87 5102 109 82\",\"queue\":\"arenas 4998 64 -4 5004 67 3\",\"spawn\":\"arenas 4998 64 -4 5004 67 3\",\"name\":\"hunt\",\"spawnRot\":-180,\"queueRot\":-180}";
        generateArenas(arenas, arenaJson);

        System.out.println(gson.toJson(arenas));
    }
}
