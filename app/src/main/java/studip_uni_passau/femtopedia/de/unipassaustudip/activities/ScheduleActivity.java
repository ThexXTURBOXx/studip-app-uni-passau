package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.femtopedia.studip.json.Course;
import de.femtopedia.studip.json.Event;
import de.femtopedia.studip.json.Events;
import de.femtopedia.studip.util.Schedule;
import de.femtopedia.studip.util.ScheduledCourse;
import de.hdodenhof.circleimageview.CircleImageView;
import oauth.signpost.exception.OAuthException;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.api.ScheduledEvent;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.ExpandableListAdapter;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class ScheduleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StudIPHelper.ProfilePicHolder {

    private static int weeks = 0;
    private ExpandableListAdapter listAdapter;
    private List<Object> listDataHeader;
    private List<List<Object>> listDataChild;
    private List<Integer> listDataColorsBg, listDataColorsText;
    private NavigationView navigationView;
    private SwipeRefreshLayout swiperefresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
        StudIPHelper.target = "schedule";

        setContentView(R.layout.schedule);

        swiperefresher = findViewById(R.id.swiperefresh_schedule);
        swiperefresher.setOnRefreshListener(this::updateData);

        ExpandableListView expListView = findViewById(R.id.schedulecontent);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this,
                listDataHeader, listDataChild, listDataColorsBg, listDataColorsText);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

        updateDataFirst();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        if (StudIPHelper.current_user == null) {
            Intent intent = new Intent(ScheduleActivity.this, LoadActivity.class);
            startActivity(intent);
        }
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

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(StudIPHelper.profile_pic);
    }

    private void updateDataFirst() {
        if (swiperefresher.isRefreshing())
            return;
        StudIPHelper.loadSchedule(this.getApplicationContext());
        updateSchedule();
        if (StudIPHelper.isNetworkAvailable(this) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto_sync", true)) {
            swiperefresher.setRefreshing(true);
            CacheSchedule schedule = new CacheSchedule();
            schedule.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateData() {
        if (StudIPHelper.isNetworkAvailable(this)) {
            swiperefresher.setRefreshing(true);
            CacheSchedule schedule = new CacheSchedule();
            schedule.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            swiperefresher.setRefreshing(false);
            updateSchedule();
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

    private void addListItem(Object title, List<Object> info, int colorBg, int colorText) {
        listDataHeader.add(title);
        listDataColorsBg.add(colorBg);
        listDataColorsText.add(colorText);
        listDataChild.add(info);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        StudIPHelper.updateNavigation(item.getItemId(), R.id.nav_schedule, this);
        return true;
    }

    private void addToView(String day, @NonNull List<ScheduledEvent> list) {
        if (!list.isEmpty()) {
            int colorBg = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("separator_schedule_color", 0xFF000000);
            addListItem(day, new ArrayList<>(), colorBg, StudIPHelper.contraColor(colorBg));
        }
        for (ScheduledEvent se : list) {
            List<Object> info = new ArrayList<>();
            info.add(se.title);
            info.add(se.room);
            if (se.categories != null)
                info.add(se.categories);
            DateTime start = new DateTime(se.start).withZone(StudIPHelper.ZONE);
            DateTime end = new DateTime(se.end).withZone(StudIPHelper.ZONE);
            info.add(getString(R.string.start) + ": " + String.format(Locale.GERMANY, "%02d", start.getHourOfDay()) + ":" + String.format(Locale.GERMANY, "%02d", start.getMinuteOfHour()));
            info.add(getString(R.string.end) + ": " + String.format(Locale.GERMANY, "%02d", end.getHourOfDay()) + ":" + String.format(Locale.GERMANY, "%02d", end.getMinuteOfHour()));
            addListItem(se.description, info, se.color, StudIPHelper.contraColor(se.color));
        }
    }

    @SuppressWarnings("UseSparseArrays")
    private Map<Integer, List<ScheduledEvent>> compareSchedule(Schedule schedule) throws IllegalAccessException, IOException, OAuthException {
        Events events = StudIPHelper.api.getData("user/" + StudIPHelper.current_user.getUser_id() + "/events?limit=10000", Events.class);
        Map<Integer, List<ScheduledEvent>> sched = new HashMap<>();
        for (int i = 0; i <= 4; i++) {
            for (int d = 0; d < 7; d++) {
                int day = d + i * 7;
                sched.put(day + 1, compareDay(getDayOfSchedule(schedule, day % 7 + 1), events.getCollection(), day + 1, i));
            }
        }
        return sched;
    }

    private List<ScheduledCourse> getDayOfSchedule(Schedule schedule, int day) {
        switch (day) {
            case 1:
                return schedule.getMonday();
            case 2:
                return schedule.getTuesday();
            case 3:
                return schedule.getWednesday();
            case 4:
                return schedule.getThursday();
            case 5:
                return schedule.getFriday();
            case 6:
                return schedule.getSaturday();
            default:
                return schedule.getSunday();
        }
    }

    private List<ScheduledEvent> compareDay(List<ScheduledCourse> courses, List<Event> events, int day, int week) {
        List<ScheduledEvent> eventss = new ArrayList<>();
        DateTime now = new DateTime().plusDays(1 + week * 7).withTime(0, 0, 0, 0).withZone(StudIPHelper.ZONE);
        for (Event event : events) {
            boolean flag = false;
            DateTime time = new DateTime(event.getStart() * 1000).withZone(StudIPHelper.ZONE);
            if (time.getDayOfWeek() != (day - 1) % 7 + 1 || time.isBefore(now.minusDays(1).minusSeconds(1)) ||
                    Days.daysBetween(now, new DateTime(time).withTime(1, 0, 0, 0)).getDays() >= 6)
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
                for (ScheduledCourse s : courses) {
                    if (event.getCourse().replaceFirst("/studip/api.php/course/", "").equals(s.getEvent_id())) {
                        String time1 = String.format(Locale.GERMANY, "%02d", time.getHourOfDay()) + String.format(Locale.GERMANY, "%02d", time.getMinuteOfHour());
                        if (Integer.parseInt(time1) == s.getStart()) {
                            String time2 = String.format(Locale.GERMANY, "%02d", dd.getHourOfDay()) + String.format(Locale.GERMANY, "%02d", dd.getMinuteOfHour());
                            if (Integer.parseInt(time2) == s.getEnd()) {
                                flag = true;
                                se.description = s.getContent();
                                se.color = Integer.parseInt("ff" + s.getColor(), 16);
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

    private String getDateString(int day, int today, int week) {
        return getDateString(day, today, week, -1);
    }

    private String getDateString(int day, int today, int week, int isToday) {
        DateTime time = new DateTime().plusDays((day < today ? day + 7 : day) - today).plusDays(7 * week).withZone(StudIPHelper.ZONE);
        StringBuilder sb = new StringBuilder();
        if (isToday == 0)
            sb = sb.append(getString(R.string.today)).append(", ");
        else if (isToday == 1)
            sb = sb.append(getString(R.string.tomorrow)).append(", ");
        switch (day) {
            case 1:
                sb = sb.append(getString(R.string.monday));
                break;
            case 2:
                sb = sb.append(getString(R.string.tuesday));
                break;
            case 3:
                sb = sb.append(getString(R.string.wednesday));
                break;
            case 4:
                sb = sb.append(getString(R.string.thursday));
                break;
            case 5:
                sb = sb.append(getString(R.string.friday));
                break;
            case 6:
                sb = sb.append(getString(R.string.saturday));
                break;
            case 7:
                sb = sb.append(getString(R.string.sunday));
                break;
        }
        return sb.append(", ").append(time.getDayOfMonth()).append(".").append(time.getMonthOfYear()).append(".").append(time.getYear()).toString();
    }

    void updateSchedule() {
        if (StudIPHelper.schedule == null)
            return;
        clearListItems();
        DateTime dt = new DateTime().withZone(StudIPHelper.ZONE);
        int dow = dt.getDayOfWeek();
        for (int wk = 0; wk <= weeks; wk++) {
            for (int i = 0; i < 7; i++) {
                int dows = (i + dow - 1) % 7 + 1;
                int days = dows + wk * 7;
                List<ScheduledEvent> list = StudIPHelper.schedule.get(days);
                if (list != null) {
                    if (days == dow)
                        addToView(getDateString(dows, dow, wk, 0), list);
                    else if (days == dow % 7 + 1)
                        addToView(getDateString(dows, dow, wk, 1), list);
                    else
                        addToView(getDateString(dows, dow, wk), list);
                }
            }
        }
        int colorButton = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("load_more_color", 0xFF000000);
        int colorButtonCon = StudIPHelper.contraColor(colorButton);
        addListItem(new ExpandableListAdapter.ButtonPreset(
                        getString(R.string.load_more), colorButtonCon, colorButton,
                        (view) -> {
                            if (!swiperefresher.isRefreshing()) {
                                weeks++;
                                if (weeks >= 4)
                                    updateData();
                                else
                                    updateSchedule();
                            }
                        }),
                new ArrayList<>(), colorButton, colorButtonCon);
    }

    @SuppressWarnings({"staticFieldLeak"})
    public class CacheSchedule extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... url) {
            try {
                StudIPHelper.updateSchedule(getApplicationContext(), compareSchedule(StudIPHelper.api.getSchedule()));
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
            updateSchedule();
            swiperefresher.setRefreshing(false);
            super.onPostExecute(aVoid);
        }
    }

}
