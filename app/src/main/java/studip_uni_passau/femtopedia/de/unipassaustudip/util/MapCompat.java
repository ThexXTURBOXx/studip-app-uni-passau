package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.os.Build;

import androidx.annotation.NonNull;

import java.util.Map;

public class MapCompat {

    public static <K, V> V getOrDefault(@NonNull Map<K, V> map, K key, V defaultValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return map.getOrDefault(key, defaultValue);
        } else {
            V v;
            return (((v = map.get(key)) != null) || map.containsKey(key))
                    ? v
                    : defaultValue;
        }
    }

}
