package gyurix.partysystem.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StrUtils {
    public static Random rand = new Random();

    public static List<String> colorize(List<String> list) {
        ArrayList<String> out = new ArrayList<>();
        list.forEach(s -> out.add(colorize(s)));
        return out;
    }

    public static String colorize(String s) {
        return s.replaceAll("&([0-9a-fk-or])", "§$1");
    }
}
