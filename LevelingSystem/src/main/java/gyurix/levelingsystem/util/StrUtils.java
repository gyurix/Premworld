package gyurix.levelingsystem.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StrUtils {
    public static DecimalFormat DF = new DecimalFormat("###,###,###,###,##0.##");

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

    public static String toCamelCase(String name) {
        StringBuilder sb = new StringBuilder();
        for (String s : name.split("[ _]")) {
            if (s.isEmpty())
                continue;
            sb.append(' ').append(Character.toUpperCase(s.charAt(0))).append(s.substring(1).toLowerCase());
        }
        return sb.length() == 0 ? sb.toString() : sb.substring(1);
    }
}
