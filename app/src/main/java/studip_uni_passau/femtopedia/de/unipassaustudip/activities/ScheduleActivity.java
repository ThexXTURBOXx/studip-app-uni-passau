package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.threeten.bp.LocalDate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.femtopedia.studip.json.Course;
import de.femtopedia.studip.json.Event;
import de.femtopedia.studip.json.Events;
import de.femtopedia.studip.json.Schedule;
import de.femtopedia.studip.json.ScheduledCourse;
import de.hdodenhof.circleimageview.CircleImageView;
import oauth.signpost.exception.OAuthException;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.api.ScheduledEvent;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.ListScheduleAdapter;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class ScheduleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StudIPHelper.ProfilePicHolder,
        OnDateSelectedListener {

    private ListScheduleAdapter listAdapter;
    private List<Object> listDataHeader;
    private List<List<Object>> listDataChild;
    private List<Integer> listDataColorsBg, listDataColorsText;
    private NavigationView navigationView;
    private SwipeRefreshLayout swipeRefresher;
    private MaterialCalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (StudIPHelper.current_user == null) {
            Intent intent = new Intent(ScheduleActivity.this, LoadActivity.class);
            startActivity(intent);
            return;
        }

        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
        StudIPHelper.target = "schedule";

        setContentView(R.layout.schedule);

        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangedListener(this);
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return day.equals(CalendarDay.today());
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(Color.LTGRAY));
            }
        });

        swipeRefresher = findViewById(R.id.swiperefresh_schedule);
        swipeRefresher.setOnRefreshListener(this::updateData);

        ExpandableListView expListView = findViewById(R.id.schedulecontent);
        prepareListData();
        listAdapter = new ListScheduleAdapter(this,
                listDataHeader, listDataChild, listDataColorsBg, listDataColorsText);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);
        selectDate(CalendarDay.today());

        updateDataFirst();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nameofcurrentuser)).setText(StudIPHelper.current_user.getName().getFormatted());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.usernameel)).setText(StudIPHelper.current_user.getUsername());

        if (StudIPHelper.profile_pic != null)
            setProfilePic();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer, (Toolbar) actionbar.getCustomView(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(drawerToggle);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
            drawerToggle.syncState();
        }
    }

    public void selectDate(CalendarDay day) {
        calendarView.setSelectedDate(day);
        clearListItems();
        LocalDate date = day.getDate();
        int dow = date.getDayOfWeek().getValue();
        int wk = (int) Math.floor(CalendarDay.today().getDate().until(date).getDays() / 7d);
        int days = dow + wk * 7;
        if (StudIPHelper.schedule != null) {
            List<ScheduledEvent> list = StudIPHelper.schedule.get(days);
            if (list != null) {
                addToView(list);
            }
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if (selected) {
            selectDate(date);
        }
    }

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(StudIPHelper.profile_pic);
    }

    private void updateDataFirst() {
        if (swipeRefresher.isRefreshing())
            return;
        StudIPHelper.loadSchedule(this.getApplicationContext());
        selectDate(CalendarDay.today());
        if (StudIPHelper.isNetworkAvailable(this) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto_sync", true)) {
            swipeRefresher.setRefreshing(true);
            CacheSchedule schedule = new CacheSchedule();
            schedule.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateData() {
        if (StudIPHelper.isNetworkAvailable(this)) {
            swipeRefresher.setRefreshing(true);
            CacheSchedule schedule = new CacheSchedule();
            schedule.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            swipeRefresher.setRefreshing(false);
            selectDate(calendarView.getSelectedDate());
        }
    }

    private void clearListItems() {
        listDataHeader.clear();
        listDataColorsBg.clear();
        listDataColorsText.clear();
        listDataChild.clear();

        listAdapter.notifyDataSetChanged();
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataColorsBg = new ArrayList<>();
        listDataColorsText = new ArrayList<>();
        listDataChild = new ArrayList<>();
    }

    private void addScheduleItem(String startStr, String endStr, String title, String room, String categories,
                                 String description, int colorText, int colorBg) {
        listDataHeader.add(new ListScheduleAdapter.ScheduleItem(startStr, room, title));
        listDataColorsBg.add(colorBg);
        listDataColorsText.add(colorText);
        if (categories == null) {
            listDataChild.add(Arrays.asList(description, room, "Start: " + startStr, "End: " + endStr));
        } else {
            listDataChild.add(Arrays.asList(description, room, categories, "Start: " + startStr, "End: " + endStr));
        }

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (!drawer.isDrawerOpen(GravityCompat.START))
                drawer.openDrawer(GravityCompat.START);
            else
                drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.action_refresh_bar) {
            this.updateData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        StudIPHelper.updateNavigation(item.getItemId(), R.id.nav_schedule, this);
        return true;
    }

    private void addToView(@NonNull List<ScheduledEvent> list) {
        for (ScheduledEvent se : list) {
            DateTime start = new DateTime(se.start).withZone(StudIPHelper.ZONE);
            DateTime end = new DateTime(se.end).withZone(StudIPHelper.ZONE);
            String startStr = String.format(Locale.GERMANY, "%02d", start.getHourOfDay()) + ":" + String.format(Locale.GERMANY, "%02d", start.getMinuteOfHour());
            String endStr = String.format(Locale.GERMANY, "%02d", end.getHourOfDay()) + ":" + String.format(Locale.GERMANY, "%02d", end.getMinuteOfHour());
            addScheduleItem(startStr, endStr, se.title, se.room, se.categories, se.description, StudIPHelper.contraColor(se.color), se.color);
        }
    }

    @SuppressWarnings("UseSparseArrays")
    private Map<Integer, List<ScheduledEvent>> compareSchedule(Schedule schedule) throws IllegalAccessException, IOException, OAuthException {
        Events events = StudIPHelper.api.getData("user/" + StudIPHelper.current_user.getUser_id() + "/events?limit=10000", Events.class);
        Map<Integer, List<ScheduledEvent>> sched = new HashMap<>();
        for (int i = 0; i <= 4; i++) {
            for (int d = 0; d < 7; d++) {
                int day = d + i * 7;
                sched.put(day + 1, compareDay(schedule.getDay(day % 7), events.getCollection(), day + 1, i));
            }
        }
        return sched;
    }

    private List<ScheduledEvent> compareDay(Map<String, ScheduledCourse> courses, List<Event> events, int day, int week) {
        List<ScheduledEvent> eventss = new ArrayList<>();
        DateTime now = new DateTime().withZone(StudIPHelper.ZONE).plusDays(1 + week * 7).withTime(0, 0, 0, 0);
        for (Event event : events) {
            boolean flag = false;
            DateTime time = new DateTime(event.getStart() * 1000).withZone(StudIPHelper.ZONE);
            if (time.getDayOfWeek() != (day - 1) % 7 + 1 || time.isBefore(now.minusDays(1).minusSeconds(1)) ||
                    Days.daysBetween(now, new DateTime(time).withTime(1, 0, 0, 0).withZone(StudIPHelper.ZONE)).getDays() >= 6)
                continue;
            DateTime dd = new DateTime(event.getEnd() * 1000).withZone(StudIPHelper.ZONE);
            ScheduledEvent se = new ScheduledEvent();
            se.start = time.getMillis();
            se.end = dd.getMillis();
            se.title = event.getTitle();
            se.canceled = event.getCanceled();
            se.room = event.getRoom();
            se.course = event.getCourse();
            if (!event.getCategories().equals("Sitzung")) {
                se.categories = event.getCategories();
                se.color = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("non_lecture_color", 0xFF339966);
            }
            if (courses != null) {
                for (Map.Entry<String, ScheduledCourse> s : courses.entrySet()) {
                    if (event.getCourse().replaceFirst("/studip/api.php/course/", "").equals(s.getKey().split("-")[0])) {
                        String time1 = String.format(Locale.GERMANY, "%02d", time.getHourOfDay()) + String.format(Locale.GERMANY, "%02d", time.getMinuteOfHour());
                        if (Integer.parseInt(time1) == s.getValue().getStart()) {
                            String time2 = String.format(Locale.GERMANY, "%02d", dd.getHourOfDay()) + String.format(Locale.GERMANY, "%02d", dd.getMinuteOfHour());
                            if (Integer.parseInt(time2) == s.getValue().getEnd()) {
                                flag = true;
                                se.description = s.getValue().getContent();
                                se.color = (int) Long.parseLong("ff" + s.getValue().getColor().replaceFirst("#", ""), 16);
                            }
                        }
                    }
                }
            }
            if (!flag) {
                try {
                    Course c = StudIPHelper.api.getCourse(event.getCourse().replaceFirst("/studip/api.php/course/", ""));
                    se.description = c.getNumber() + " " + c.getTitle();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException | OAuthException e) {
                    Intent intent = new Intent(ScheduleActivity.this, LoadActivity.class);
                    startActivity(intent);
                }
                if (se.color == -1)
                    se.color = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("not_found_lecture_color", 0xFFea3838);
            }
            eventss.add(se);
        }
        return eventss;
    }

    @SuppressWarnings({"staticFieldLeak"})
    public class CacheSchedule extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... url) {
            try {
                StudIPHelper.updateSchedule(getApplicationContext(), compareSchedule(StudIPHelper.api.getSchedule(StudIPHelper.current_user.getUser_id())));
            } catch (IllegalAccessException | OAuthException e) {
                Intent intent = new Intent(ScheduleActivity.this, LoadActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            selectDate(calendarView.getSelectedDate());
            swipeRefresher.setRefreshing(false);
            super.onPostExecute(aVoid);
        }
    }

}
