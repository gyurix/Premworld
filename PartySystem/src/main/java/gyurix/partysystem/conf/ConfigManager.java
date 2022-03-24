package gyurix.partysystem.conf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;

import static gyurix.partysystem.PartySystem.pl;

public class ConfigManager {
    public static Config conf;
    public static File confFile = new File(pl.getDataFolder() + File.separator + "config.json"),
        msgFile = new File(pl.getDataFolder() + File.separator + "messages.yml");
    public static Gson gson = new GsonBuilder()
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
