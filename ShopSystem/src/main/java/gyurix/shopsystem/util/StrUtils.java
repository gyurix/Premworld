package gyurix.shopsystem.util;

import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StrUtils {
    public static final DecimalFormat DF = new DecimalFormat("###,###,###,###,##0.##");

    public static List<String> colorize(List<String> list) {
        ArrayList<String> out = new ArrayList<>();
        list.forEach(s -> out.add(colorize(s)));
        return out;
    }

    public static String colorize(String s) {
        return s.replaceAll("&([0-9a-fk-or])", "§$1");
    }

    public static String fillVariables(String s, Object... vars) {
        if (s == null || s.isEmpty())
            return s;
        String key = null;
        for (Object o : vars) {
            if (key == null)
                key = String.valueOf(o);
            else {
                s = s.replace("<" + key + ">", String.valueOf(o));
                key = null;
            }
        }
        return s;
    }

    public static List<String> fillVariables(List<String> list, Object... vars) {
        if (list == null || list.isEmpty())
            return list;
        int len = list.size();
        for (int i = 0; i < len; ++i)
            list.set(i, fillVariables(list.get(i), vars));
        return list;
    }

    public static String formatTime(long time) {
        time /= 1000;
        if (time < 0)
            time = 0L;
        int w = (int) (time / 604800);
        int d = (int) (time % 604800 / 86400);
        int h = (int) (time % 86400 / 3600);
        int m = (int) (time % 3600 / 60);
        int s = (int) (time % 60);
        StringBuilder sb = new StringBuilder();
        String sep = ", ";
        if (w > 0)
            sb.append(w).append('w').append(sep);
        if (d > 0)
            sb.append(d).append('d').append(sep);
        if (h > 0)
            sb.append(h).append('h').append(sep);
        if (m > 0)
            sb.append(m).append('m').append(sep);
        if (sb.length() == 0 || s > 0)
            sb.append(s).append('s').append(sep);
        return sb.substring(0, sb.length() - sep.length());
    }
}
