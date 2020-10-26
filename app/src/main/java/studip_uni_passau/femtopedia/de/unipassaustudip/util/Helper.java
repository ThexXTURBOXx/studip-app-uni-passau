package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import java.util.ArrayList;
import java.util.List;

public class Helper {

    @SafeVarargs
    public static <T> List<T> asNonNullList(T... args) {
        List<T> list = new ArrayList<>();
        for (T arg : args) {
            if (arg != null) {
                list.add(arg);
            }
        }
        return list;
    }

}
