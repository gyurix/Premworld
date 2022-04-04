package gyurix.coliseumgames.conf;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

import static gyurix.coliseumgames.util.StrUtils.colorize;

public class Messages {
    private final YamlConfiguration msgYaml;

    public Messages(YamlConfiguration msgYaml) {
        this.msgYaml = msgYaml;
    }

    public String get(String key, Object... args) {
        String msg = msgYaml.isList(key) ? StringUtils.join(msgYaml.getStringList(key), "\n") : msgYaml.getString(key, "Missing(" + key + ")");
        String curKey = null;
        for (Object o : args) {
            if (curKey == null) {
                curKey = "<" + o + ">";
                continue;
            }
            msg = msg.replace(curKey, String.valueOf(o));
            curKey = null;
        }
        return colorize(msg);
    }

    public List<String> getList(String key) {
        return colorize(msgYaml.getStringList(key));
    }

    public void msg(CommandSender target, String key, Object... args) {
        String msg = get(key, args);
        if (msg.contains("\n")) {
            target.sendMessage(msg);
            return;
        }
        target.sendMessage(get("prefix") + msg);
    }
}
