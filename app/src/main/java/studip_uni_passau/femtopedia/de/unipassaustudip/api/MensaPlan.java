package studip_uni_passau.femtopedia.de.unipassaustudip.api;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class MensaPlan {

    public Map<Long, DayMenu> menu = new HashMap<>();

    @NonNull
    @Override
    public String toString() {
        return "MensaPlan{" +
                "menu=" + menu +
                '}';
    }

    public enum FoodProperty {

        CHICKEN("G", R.string.chicken, R.drawable.chicken),
        PIG("S", R.string.pig, R.drawable.pig),
        COW("R", R.string.cow, R.drawable.cow),
        LAMB("L", R.string.lamb, R.drawable.lamb),
        WILD("W", R.string.wild, R.drawable.wild),
        FISH("F", R.string.fish, R.drawable.fish),
        ALCOHOL("A", R.string.alcohol, R.drawable.alcohol),
        VEGETARIAN("V", R.string.vegetarian, R.drawable.vegetarian),
        VEGAN("VG", R.string.vegan, R.drawable.vegan),
        VITAL("MV", R.string.vital, R.drawable.vital),
        OEKO("B", R.string.oeko, R.drawable.oeko),
        JURADISTL("J", R.string.juradistl, R.drawable.juradistl),
        BIOLAND("BL", R.string.bioland, R.drawable.bioland),
        UNKNOWN("", R.string.unknown, R.drawable.unknown),
        NONE("noneunknownkey", 0, 0);

        public String abbrev;
        public int meaning, drawable;

        FoodProperty(String abbrev, int meaning, int drawable) {
            this.abbrev = abbrev;
            this.meaning = meaning;
            this.drawable = drawable;
        }

        public static FoodProperty getProperty(String key) {
            if (key.equals(""))
                return NONE;
            for (FoodProperty prop : values()) {
                if (prop.abbrev.equals(key))
                    return prop;
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return "FoodProperty{" +
                    "abbrev='" + abbrev + '\'' +
                    '}';
        }

    }

    public static class DayMenu {

        public List<Food> soups = new ArrayList<>();
        public List<Food> mains = new ArrayList<>();
        public List<Food> garnishes = new ArrayList<>();
        public List<Food> desserts = new ArrayList<>();

        @NonNull
        @Override
        public String toString() {
            return "DayMenu{" +
                    "soups=" + soups +
                    ", mains=" + mains +
                    ", garnishes=" + garnishes +
                    ", desserts=" + desserts +
                    '}';
        }

    }

    public static class Food {

        public String name;
        public Set<FoodProperty> properties;
        public double price_stud, price_bed, price_guest;

        @NonNull
        @Override
        public String toString() {
            return "Food{" +
                    "name='" + name + '\'' +
                    ", properties=" + properties +
                    ", price_stud=" + price_stud +
                    ", price_bed=" + price_bed +
                    ", price_guest=" + price_guest +
                    '}';
        }

    }

}
