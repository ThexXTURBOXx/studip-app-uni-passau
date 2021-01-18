package studip_uni_passau.femtopedia.de.unipassaustudip.util.mensa;

import android.text.TextUtils;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import studip_uni_passau.femtopedia.de.unipassaustudip.api.MensaPlan;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.MapCompat;

public final class MensaPlanParser {

    private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.GERMAN);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Pattern PATTERN = Pattern.compile("\\s*\\(([^)]+)\\)\\s*");

    public static MensaPlan parsePlan(List<String> lines) {
        MensaPlan plan = new MensaPlan();

        for (int i = 1; i < lines.size(); i++) {
            String[] entries = lines.get(i).split(";");
            try {
                addToMenu(plan, entries);
            } catch (Throwable t) {
                t.addSuppressed(new Throwable(Arrays.toString(entries)));
                t.printStackTrace();
            }
        }

        return plan;
    }

    private static void addToMenu(MensaPlan plan, String[] entries) throws CSVParserException {
        CalendarDay day = CalendarDay.from(parseDate(entries[0]));
        long key = day.getDate().toEpochDay();
        MensaPlan.DayMenu menu = MapCompat.getOrDefault(plan.menu, key, new MensaPlan.DayMenu());
        MensaPlan.Food food = parseFood(entries);

        String type = entries[2];
        if (type.startsWith("HG")) {
            menu.mains.add(food);
        } else if (type.startsWith("B")) {
            menu.garnishes.add(food);
        } else if (type.startsWith("N")) {
            menu.desserts.add(food);
        } else if (type.startsWith("Suppe")) {
            menu.soups.add(food);
        } else {
            throw new CSVParserException("Invalid type!");
        }

        plan.menu.put(key, menu);
    }

    private static MensaPlan.Food parseFood(String[] entries) throws CSVParserException {
        if (entries.length != 9) {
            throw new CSVParserException("Entries length is wrong!");
        }

        Set<String> set = parseAllergensAndAdditives(entries[3]);

        MensaPlan.Food food = new MensaPlan.Food();
        food.name = parseName(entries[3]);
        food.properties = parseFoodProperties(entries[4]);
        food.price_stud = parsePrice(entries[6]);
        food.price_bed = parsePrice(entries[7]);
        food.price_guest = parsePrice(entries[8]);

        if (!set.isEmpty()) {
            food.name += " (" + TextUtils.join(",", set) + ')';
        }

        return food;
    }

    private static LocalDate parseDate(String date) throws CSVParserException {
        try {
            return LocalDate.parse(date, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new CSVParserException("Date could not be parsed");
        }
    }

    private static String parseName(String name) {
        String[] split = PATTERN.split(name);

        if (split.length == 1) {
            return split[0];
        } else {
            String parts = TextUtils.join(", ", Arrays.copyOfRange(split, 1, split.length));
            return split[0] + " (" + parts + ")";
        }
    }

    private static Set<MensaPlan.FoodProperty> parseFoodProperties(String tags) {
        Set<MensaPlan.FoodProperty> set = new HashSet<>();
        for (String tagStr : tags.split(",\\s*")) {
            MensaPlan.FoodProperty fp = MensaPlan.FoodProperty.getProperty(tagStr);
            if (fp != null)
                set.add(fp);
        }
        return set;
    }

    private static Set<String> parseAllergensAndAdditives(String name) {
        Set<String> set = new HashSet<>();

        Matcher m = PATTERN.matcher(name);
        while (m.find()) {
            String[] arr = m.group(1).split(",\\s*");
            set.addAll(Arrays.asList(arr));
        }

        return set;
    }

    private static double parsePrice(String number) throws CSVParserException {
        try {
            return FORMAT.parse(number).doubleValue();
        } catch (ParseException e) {
            throw new CSVParserException("Invalid price!");
        }
    }

    private static class CSVParserException extends Exception {

        private CSVParserException(String msg) {
            super(msg);
        }

    }

}
