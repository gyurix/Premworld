package gyurix.levelingsystem.conf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gyurix.levelingsystem.conf.adapters.ItemStackAdapter;
import gyurix.levelingsystem.conf.adapters.StringSerializableAdapter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;

import static gyurix.levelingsystem.LevelingSystem.pl;


public class ConfigManager {
    public static Config conf;
    public static File confFile = new File(pl.getDataFolder() + File.separator + "config.json"),
        msgFile = new File(pl.getDataFolder() + File.separator + "messages.yml");
    public static Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new StringSerializableAdapter())
        .registerTypeAdapter(ItemStack.class, new ItemStackAdapter().nullSafe())
        .serializeNulls()
        .setPrettyPrinting()
        .create();
    public static Messages msg;

    @SneakyThrows
    public static void reload() {
        if (!confFile.exists())
            pl.saveResource("config.json", false);

        if (!msgFile.exists())
            pl.saveResource("messages.yml", false);

        try (FileReader confFileReader = new FileReader(confFile)) {
            conf = gson.fromJson(confFileReader, Config.class);
            msg = new Messages(YamlConfiguration.loadConfiguration(msgFile));
        }
    }
}
