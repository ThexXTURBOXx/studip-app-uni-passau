package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.femtopedia.studip.json.Course;
import de.femtopedia.studip.json.Event;
import de.femtopedia.studip.json.Events;
import de.femtopedia.studip.util.Schedule;
import de.femtopedia.studip.util.ScheduledCourse;
import de.hdodenhof.circleimageview.CircleImageView;

public class ScheduleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    List<List<Object>> listDataChild;
    List<Integer> listDataColorsBg, listDataColorsText;
    private NavigationView navigationView;
    private SwipeRefreshLayout swiperefresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);

        swiperefresher = findViewById(R.id.swiperefresh_schedule);
        swiperefresher.setOnRefreshListener(this::updateData);

        expListView = findViewById(R.id.schedulecontent);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this,
                listDataHeader, listDataChild, listDataColorsBg, listDataColorsText);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

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
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            drawerToggle.syncState();
        }
    }

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(StudIPHelper.profile_pic);
    }

    private void updateDataFirst() {
        StudIPHelper.loadSchedule(this.getApplicationContext());
        updateSchedule();
        if (StudIPHelper.isNetworkAvailable(this) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto_sync", true)) {
            swiperefresher.setRefreshing(true);
            CacheSchedule sched = new CacheSchedule();
            sched.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateData() {
        if (StudIPHelper.isNetworkAvailable(this)) {
            swiperefresher.setRefreshing(true);
            CacheSchedule sched = new CacheSchedule();
            sched.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            swiperefresher.setRefreshing(false);
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

    private void addListItem(String title, List<Object> info, int colorBg, int colorText) {
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
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mensa) {
            Intent intent = new Intent(ScheduleActivity.this, MensaActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(ScheduleActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bugreport) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ThexXTURBOXx/studip-app-uni-passau/issues/new"));
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(ScheduleActivity.this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.open_in_browser) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://studip.uni-passau.de/studip/index.php"));
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addToView(String day, @NonNull List<ScheduledEvent> list) {
        if (!list.isEmpty())
            addListItem(day, new ArrayList<>(), Color.BLACK, Color.WHITE);
        for (ScheduledEvent se : list) {
            List<Object> info = new ArrayList<>();
            info.add(se.title);
            info.add(se.room);
            DateTime start = new DateTime(se.start);
            DateTime end = new DateTime(se.end);
            info.add(getString(R.string.start) + ": " + String.format(Locale.GERMANY, "%02d", start.getHourOfDay()) + ":" + String.format(Locale.GERMANY, "%02d", start.getMinuteOfHour()));
            info.add(getString(R.string.end) + ": " + String.format(Locale.GERMANY, "%02d", end.getHourOfDay()) + ":" + String.format(Locale.GERMANY, "%02d", end.getMinuteOfHour()));
            int color = Color.parseColor("#" + se.color);
            double lum = 0.299d * (double) Color.red(color) + 0.587d * (double) Color.green(color) + 0.114d * (double) Color.blue(color);
            addListItem(se.description, info, color, lum > 128 ? Color.BLACK : Color.WHITE);
        }
    }

    private EventSchedule compareSchedule(Schedule schedule) throws IllegalAccessException, IOException {
        Events events = StudIPHelper.api.getData("user/" + StudIPHelper.current_user.getUser_id() + "/events?limit=10000", Events.class);
        EventSchedule sched = new EventSchedule();
        sched.monday = compareDay(schedule.getMonday(), events.getCollection(), 1);
        sched.tuesday = compareDay(schedule.getTuesday(), events.getCollection(), 2);
        sched.wednesday = compareDay(schedule.getWednesday(), events.getCollection(), 3);
        sched.thursday = compareDay(schedule.getThursday(), events.getCollection(), 4);
        sched.friday = compareDay(schedule.getFriday(), events.getCollection(), 5);
        sched.saturday = compareDay(schedule.getSaturday(), events.getCollection(), 6);
        sched.sunday = compareDay(schedule.getSunday(), events.getCollection(), 7);
        return sched;
    }

    private List<ScheduledEvent> compareDay(List<ScheduledCourse> courses, List<Event> events, int day) {
        List<ScheduledEvent> eventss = new ArrayList<>();
        if (courses != null) {
            DateTime now = new DateTime().plusDays(1).withTime(0, 0, 0, 0);
            for (Event event : events) {
                boolean flag = false;
                DateTime time = new DateTime(event.getStart() * 1000);
                if (time.getDayOfWeek() != day || Days.daysBetween(now, new DateTime(time).withTime(1, 0, 0, 0)).getDays() >= 6)
                    continue;
                DateTime dd = new DateTime(event.getEnd() * 1000);
                ScheduledEvent se = new ScheduledEvent();
                se.start = time.getMillis();
                se.end = dd.getMillis();
                se.title = event.getTitle();
                se.canceled = event.getCanceled();
                se.room = event.getRoom();
                se.course = event.getCourse();
                for (ScheduledCourse s : courses) {
                    if (event.getCourse().replaceFirst("/studip/api.php/course/", "").equals(s.getEvent_id())) {
                        String time1 = String.format(Locale.GERMANY, "%02d", time.getHourOfDay()) + String.format(Locale.GERMANY, "%02d", time.getMinuteOfHour());
                        if (Integer.parseInt(time1) == s.getStart()) {
                            String time2 = String.format(Locale.GERMANY, "%02d", dd.getHourOfDay()) + String.format(Locale.GERMANY, "%02d", dd.getMinuteOfHour());
                            if (Integer.parseInt(time2) == s.getEnd()) {
                                flag = true;
                                se.description = s.getContent();
                                se.color = s.getColor();
                            }
                        }
                    }
                }
                if (!flag) {
                    try {
                        Course c = StudIPHelper.api.getCourse(event.getCourse().replaceFirst("/studip/api.php/course/", ""));
                        se.description = c.getNumber() + " " + c.getTitle();
                    } catch (IOException | IllegalAccessException e) {
                    }
                    se.color = "ea3838";
                }
                if (se.title.endsWith(": Entfällt"))
                    se.color = "aaaaaa";
                eventss.add(se);
            }
        }
        return eventss;
    }

    private String getDateString(int day, int today) {
        return getDateString(day, today, -1);
    }

    private String getDateString(int day, int today, int isToday) {
        DateTime time = new DateTime().plusDays((day < today ? day + 7 : day) - today);
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
        DateTime dt = new DateTime();
        int dow = dt.getDayOfWeek();
        switch (dow) {
            case 1:
                addToView(getDateString(1, dow, 0), StudIPHelper.schedule.monday);
                addToView(getDateString(2, dow, 1), StudIPHelper.schedule.tuesday);
                addToView(getDateString(3, dow), StudIPHelper.schedule.wednesday);
                addToView(getDateString(4, dow), StudIPHelper.schedule.thursday);
                addToView(getDateString(5, dow), StudIPHelper.schedule.friday);
                addToView(getDateString(6, dow), StudIPHelper.schedule.saturday);
                addToView(getDateString(7, dow), StudIPHelper.schedule.sunday);
                break;
            case 2:
                addToView(getDateString(2, dow, 0), StudIPHelper.schedule.tuesday);
                addToView(getDateString(3, dow, 1), StudIPHelper.schedule.wednesday);
                addToView(getDateString(4, dow), StudIPHelper.schedule.thursday);
                addToView(getDateString(5, dow), StudIPHelper.schedule.friday);
                addToView(getDateString(6, dow), StudIPHelper.schedule.saturday);
                addToView(getDateString(7, dow), StudIPHelper.schedule.sunday);
                addToView(getDateString(1, dow), StudIPHelper.schedule.monday);
                break;
            case 3:
                addToView(getDateString(3, dow, 0), StudIPHelper.schedule.wednesday);
                addToView(getDateString(4, dow, 1), StudIPHelper.schedule.thursday);
                addToView(getDateString(5, dow), StudIPHelper.schedule.friday);
                addToView(getDateString(6, dow), StudIPHelper.schedule.saturday);
                addToView(getDateString(7, dow), StudIPHelper.schedule.sunday);
                addToView(getDateString(1, dow), StudIPHelper.schedule.monday);
                addToView(getDateString(2, dow), StudIPHelper.schedule.tuesday);
                break;
            case 4:
                addToView(getDateString(4, dow, 0), StudIPHelper.schedule.thursday);
                addToView(getDateString(5, dow, 1), StudIPHelper.schedule.friday);
                addToView(getDateString(6, dow), StudIPHelper.schedule.saturday);
                addToView(getDateString(7, dow), StudIPHelper.schedule.sunday);
                addToView(getDateString(1, dow), StudIPHelper.schedule.monday);
                addToView(getDateString(2, dow), StudIPHelper.schedule.tuesday);
                addToView(getDateString(3, dow), StudIPHelper.schedule.wednesday);
                break;
            case 5:
                addToView(getDateString(5, dow, 0), StudIPHelper.schedule.friday);
                addToView(getDateString(6, dow, 1), StudIPHelper.schedule.saturday);
                addToView(getDateString(7, dow), StudIPHelper.schedule.sunday);
                addToView(getDateString(1, dow), StudIPHelper.schedule.monday);
                addToView(getDateString(2, dow), StudIPHelper.schedule.tuesday);
                addToView(getDateString(3, dow), StudIPHelper.schedule.wednesday);
                addToView(getDateString(4, dow), StudIPHelper.schedule.thursday);
                break;
            case 6:
                addToView(getDateString(6, dow, 0), StudIPHelper.schedule.saturday);
                addToView(getDateString(7, dow, 1), StudIPHelper.schedule.sunday);
                addToView(getDateString(1, dow), StudIPHelper.schedule.monday);
                addToView(getDateString(2, dow), StudIPHelper.schedule.tuesday);
                addToView(getDateString(3, dow), StudIPHelper.schedule.wednesday);
                addToView(getDateString(4, dow), StudIPHelper.schedule.thursday);
                addToView(getDateString(5, dow), StudIPHelper.schedule.friday);
                break;
            case 7:
                addToView(getDateString(7, dow, 0), StudIPHelper.schedule.sunday);
                addToView(getDateString(1, dow, 1), StudIPHelper.schedule.monday);
                addToView(getDateString(2, dow), StudIPHelper.schedule.tuesday);
                addToView(getDateString(3, dow), StudIPHelper.schedule.wednesday);
                addToView(getDateString(4, dow), StudIPHelper.schedule.thursday);
                addToView(getDateString(5, dow), StudIPHelper.schedule.friday);
                addToView(getDateString(6, dow), StudIPHelper.schedule.saturday);
                break;
        }
    }

    @SuppressWarnings("staticFieldLeak")
    public class CacheSchedule extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... url) {
            try {
                StudIPHelper.updateSchedule(getApplicationContext(), compareSchedule(StudIPHelper.api.getSchedule()));
            } catch (IllegalAccessException e) {
                Intent intent = new Intent(ScheduleActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } catch (IOException e) {
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
