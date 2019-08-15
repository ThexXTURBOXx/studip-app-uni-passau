package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

public class DayFilterDecorator implements DayViewDecorator {

    private HashSet<CalendarDay> dates;

    public DayFilterDecorator(Collection<CalendarDay> whitelist) {
        this.dates = new HashSet<>(whitelist);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return !dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setDaysDisabled(true);
    }

}
