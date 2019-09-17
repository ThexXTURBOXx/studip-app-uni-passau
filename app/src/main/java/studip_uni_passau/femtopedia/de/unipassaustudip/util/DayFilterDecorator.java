package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.content.Context;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class DayFilterDecorator implements DayViewDecorator {

    private HashSet<CalendarDay> dates;
    private Context context;

    public DayFilterDecorator(Context context, Collection<CalendarDay> whitelist) {
        this.dates = new HashSet<>(whitelist);
        this.context = context;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return !dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorGrey)));
    }

}
