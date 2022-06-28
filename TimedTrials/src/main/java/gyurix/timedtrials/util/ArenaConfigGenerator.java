package gyurix.timedtrials.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gyurix.timedtrials.conf.adapters.StringSerializableAdapter;
import gyurix.timedtrials.data.Arena;
import gyurix.timedtrials.data.Loc;
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
            shifted.setName(arena.getName() + StringUtils.leftPad(String.valueOf(i + 1), 2, '0'));
            shifted.setArea(arena.getArea().shift(0, 0, -20000 + offsetZ * i));
            shifted.setQueue(arena.getQueue().shift(0, 0, -20000 + offsetZ * i));
            shifted.setWall(arena.getWall().shift(0, 0, -20000 + offsetZ * i));
            shifted.setFinish(arena.getFinish().shift(0, 0, -20000 + offsetZ * i));
            for (Loc loc : arena.getSpawns()) {
                shifted.getSpawns().add(loc.add(0, 0, -20000 + offsetZ * i));
            }
            shifted.setQueueRot(arena.getQueueRot());
            arenas.put(shifted.getName(), shifted);
        }
    }

    public static void main(String[] args) {
        TreeMap<String, Arena> arenas = new TreeMap<>();

        String arenaJson = "{\"area\":\"arenas 5802 59 18976 6125 89 19280\",\"queue\":\"arenas 5859 65 19215 5876 76 19230\",\"wall\":\"arenas 5808 64 19202 5833 69 19202\",\"finish\":\"arenas 5998 63 18980 6004 68 19018\",\"name\":\"jetski\",\"spawns\":[\"arenas 5832 64 19200 0\",\"arenas 5829 64 19200 0\",\"arenas 5826 64 19200 0\",\"arenas 5823 64 19200 0\",\"arenas 5820 64 19200 0\",\"arenas 5817 64 19200 0\",\"arenas 5814 64 19200 0\",\"arenas 5811 64 19200 0\",\"arenas 5808 64 19200 0\"],\"queueRot\":45}";
        generateArenas(arenas, arenaJson);

        System.out.println(gson.toJson(arenas));
    }
}
