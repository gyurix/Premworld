package gyurix.timedtrials.conf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gyurix.timedtrials.conf.adapters.ItemStackAdapter;
import gyurix.timedtrials.conf.adapters.PostProcessableAdapter;
import gyurix.timedtrials.conf.adapters.StringSerializableAdapter;
import gyurix.timedtrials.data.Arena;
import gyurix.timedtrials.data.Config;
import gyurix.timedtrials.TTPlugin;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.TreeMap;

public class ConfigManager {
    public static File arenaFile = new File(TTPlugin.pl.getDataFolder() + File.separator + "arenas.json"),
            confFile = new File(TTPlugin.pl.getDataFolder() + File.separator + "config.json"),
            msgFile = new File(TTPlugin.pl.getDataFolder() + File.separator + "messages.yml");
    public static TreeMap<String, Arena> arenas = new TreeMap<>();
    public static Config conf;
    public static Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new StringSerializableAdapter())
            .registerTypeAdapterFactory(new PostProcessableAdapter())
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter().nullSafe())
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    public static Messages msg;

    @SneakyThrows
    public static void reload() {
        if (!confFile.exists())
            TTPlugin.pl.saveResource("config.json", false);

        if (!arenaFile.exists())
            TTPlugin.pl.saveResource("arenas.json", false);

        if (!msgFile.exists())
            TTPlugin.pl.saveResource("messages.yml", false);

        try (FileReader confFileReader = new FileReader(confFile); FileReader arenaFileReader = new FileReader(arenaFile)) {
            conf = gson.fromJson(confFileReader, Config.class);
            arenas = gson.fromJson(arenaFileReader, TypeToken.getParameterized(TreeMap.class, String.class, Arena.class).getType());
            msg = new Messages(YamlConfiguration.loadConfiguration(msgFile));
        }
    }

    @SneakyThrows
    public static void saveArenas() {
        try (FileWriter arenaWriter = new FileWriter(arenaFile)) {
            arenaWriter.write(gson.toJson(arenas));
        }
    }
}
