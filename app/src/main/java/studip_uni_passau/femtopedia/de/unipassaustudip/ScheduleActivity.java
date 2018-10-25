package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.femtopedia.studip.json.Event;
import de.femtopedia.studip.json.Events;
import de.femtopedia.studip.json.User;
import de.femtopedia.studip.util.Schedule;
import de.femtopedia.studip.util.ScheduledCourse;
import de.hdodenhof.circleimageview.CircleImageView;

public class ScheduleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    List<List<String>> listDataChild;
    List<Integer> listDataColorsBg, listDataColorsText;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);
        CacheCurrentUserData data = new CacheCurrentUserData();
        data.execute();
        CacheSchedule sched = new CacheSchedule();
        sched.execute();

        expListView = findViewById(R.id.schedulecontent);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this,
                listDataHeader, listDataChild, listDataColorsBg, listDataColorsText);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataColorsBg = new ArrayList<>();
        listDataColorsText = new ArrayList<>();
        listDataChild = new ArrayList<>();
    }

    private void addListItem(String title, List<String> info, int colorBg, int colorText) {
        listDataHeader.add(title);
        listDataColorsBg.add(colorBg);
        listDataColorsText.add(colorText);
        listDataChild.add(info);

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_opendrawer) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (!drawer.isDrawerOpen(GravityCompat.START))
                drawer.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_todo) {

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(ScheduleActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bugreport) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addToView(String day, List<ScheduledEvent> list) {
        if (list != null && !list.isEmpty())
            addListItem(day, new ArrayList<>(), 0x00000000, 0xffffffff);
        for (ScheduledEvent se : list) {
            List<String> info = new ArrayList<>();
            info.add(se.title);
            info.add(se.room);
            info.add("Start: " + String.format("%02d", se.start.getHourOfDay()) + ":" + String.format("%02d", se.start.getMinuteOfHour()));
            info.add("Ende: " + String.format("%02d", se.end.getHourOfDay()) + ":" + String.format("%02d", se.end.getMinuteOfHour()));
            addListItem(se.description, info, Color.parseColor("#" + se.color), (0xffffff - Color.parseColor("#" + se.color)) | 0xFF000000);
        }
    }

    private EventSchedule compareSchedule(Schedule schedule) throws IllegalAccessException, IOException {
        Events events = ActivityHolder.api.getData("user/" + ActivityHolder.current_user.getUser_id() + "/events?limit=10000", Events.class);
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
            for (ScheduledCourse s : courses) {
                for (Event event : events) {
                    if (event.getCourse().replaceFirst("/studip/api.php/course/", "").equals(s.getEvent_id())) {
                        DateTime time = new DateTime(event.getStart() * 1000);
                        if (Days.daysBetween(now, new DateTime(time).withTime(1, 0, 0, 0)).getDays() >= 6)
                            continue;
                        int day1 = time.getDayOfWeek();
                        if (day == day1) {
                            String time1 = String.format("%02d", time.getHourOfDay()) + String.format("%02d", time.getMinuteOfHour());
                            if (Integer.parseInt(time1) == s.getStart()) {
                                DateTime dd = new DateTime(event.getEnd() * 1000);
                                String time2 = String.format("%02d", dd.getHourOfDay()) + String.format("%02d", dd.getMinuteOfHour());
                                if (Integer.parseInt(time2) == s.getEnd()) {
                                    ScheduledEvent se = new ScheduledEvent();
                                    se.start = time;
                                    se.end = dd;
                                    se.title = event.getTitle();
                                    se.description = s.getContent();
                                    se.canceled = event.getCanceled();
                                    se.room = event.getRoom();
                                    se.color = s.getColor();
                                    se.course = event.getCourse();
                                    eventss.add(se);
                                }
                            }
                        }
                    }
                }
            }
        }
        return eventss;
    }

    private String getDateString(int day, int today) {
        return getDateString(day, today, false);
    }

    private String getDateString(int day, int today, boolean isToday) {
        DateTime time = new DateTime().plusDays((day < today ? day + 7 : day) - today);
        StringBuilder sb = new StringBuilder();
        if (isToday)
            sb = sb.append(getString(R.string.today)).append(", ");
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

    public class CacheCurrentUserData extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... voids) {
            try {
                return ActivityHolder.api.getCurrentUserData();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Intent intent = new Intent(ScheduleActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user == null) {
                CacheCurrentUserData data = new CacheCurrentUserData();
                data.execute();
            } else {
                ActivityHolder.current_user = user;
                ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nameofcurrentuser)).setText(user.getName().getFormatted());
                ((TextView) navigationView.getHeaderView(0).findViewById(R.id.usernameel)).setText(user.getUsername());
                CacheCurrentUserPic pic = new CacheCurrentUserPic();
                pic.execute(user.getAvatar_original());
            }
            super.onPostExecute(user);
        }
    }

    public class CacheCurrentUserPic extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... url) {
            try {
                HttpResponse response = ActivityHolder.api.getShibbolethClient().getIfValid(url[0]);
                HttpEntity entity = response.getEntity();
                BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
                InputStream instream = bufHttpEntity.getContent();
                return BitmapFactory.decodeStream(instream);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Intent intent = new Intent(ScheduleActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                CacheCurrentUserPic data = new CacheCurrentUserPic();
                data.execute(ActivityHolder.current_user.getAvatar_original());
            } else {
                ActivityHolder.profile_pic = bitmap;
                ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(bitmap);
            }
            super.onPostExecute(bitmap);
        }
    }

    public class CacheSchedule extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... url) {
            try {
                ActivityHolder.schedule = compareSchedule(ActivityHolder.api.getSchedule());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Intent intent = new Intent(ScheduleActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DateTime dt = new DateTime();
            int dow = dt.getDayOfWeek();
            switch (dow) {
                case 1:
                    addToView(getDateString(1, dow, true), ActivityHolder.schedule.monday);
                    addToView(getDateString(2, dow), ActivityHolder.schedule.tuesday);
                    addToView(getDateString(3, dow), ActivityHolder.schedule.wednesday);
                    addToView(getDateString(4, dow), ActivityHolder.schedule.thursday);
                    addToView(getDateString(5, dow), ActivityHolder.schedule.friday);
                    addToView(getDateString(6, dow), ActivityHolder.schedule.saturday);
                    addToView(getDateString(7, dow), ActivityHolder.schedule.sunday);
                    break;
                case 2:
                    addToView(getDateString(2, dow, true), ActivityHolder.schedule.tuesday);
                    addToView(getDateString(3, dow), ActivityHolder.schedule.wednesday);
                    addToView(getDateString(4, dow), ActivityHolder.schedule.thursday);
                    addToView(getDateString(5, dow), ActivityHolder.schedule.friday);
                    addToView(getDateString(6, dow), ActivityHolder.schedule.saturday);
                    addToView(getDateString(7, dow), ActivityHolder.schedule.sunday);
                    addToView(getDateString(1, dow), ActivityHolder.schedule.monday);
                    break;
                case 3:
                    addToView(getDateString(3, dow, true), ActivityHolder.schedule.wednesday);
                    addToView(getDateString(4, dow), ActivityHolder.schedule.thursday);
                    addToView(getDateString(5, dow), ActivityHolder.schedule.friday);
                    addToView(getDateString(6, dow), ActivityHolder.schedule.saturday);
                    addToView(getDateString(7, dow), ActivityHolder.schedule.sunday);
                    addToView(getDateString(1, dow), ActivityHolder.schedule.monday);
                    addToView(getDateString(2, dow), ActivityHolder.schedule.tuesday);
                    break;
                case 4:
                    addToView(getDateString(4, dow, true), ActivityHolder.schedule.thursday);
                    addToView(getDateString(5, dow), ActivityHolder.schedule.friday);
                    addToView(getDateString(6, dow), ActivityHolder.schedule.saturday);
                    addToView(getDateString(7, dow), ActivityHolder.schedule.sunday);
                    addToView(getDateString(1, dow), ActivityHolder.schedule.monday);
                    addToView(getDateString(2, dow), ActivityHolder.schedule.tuesday);
                    addToView(getDateString(3, dow), ActivityHolder.schedule.wednesday);
                    break;
                case 5:
                    addToView(getDateString(5, dow, true), ActivityHolder.schedule.friday);
                    addToView(getDateString(6, dow), ActivityHolder.schedule.saturday);
                    addToView(getDateString(7, dow), ActivityHolder.schedule.sunday);
                    addToView(getDateString(1, dow), ActivityHolder.schedule.monday);
                    addToView(getDateString(2, dow), ActivityHolder.schedule.tuesday);
                    addToView(getDateString(3, dow), ActivityHolder.schedule.wednesday);
                    addToView(getDateString(4, dow), ActivityHolder.schedule.thursday);
                    break;
                case 6:
                    addToView(getDateString(6, dow, true), ActivityHolder.schedule.saturday);
                    addToView(getDateString(7, dow), ActivityHolder.schedule.sunday);
                    addToView(getDateString(1, dow), ActivityHolder.schedule.monday);
                    addToView(getDateString(2, dow), ActivityHolder.schedule.tuesday);
                    addToView(getDateString(3, dow), ActivityHolder.schedule.wednesday);
                    addToView(getDateString(4, dow), ActivityHolder.schedule.thursday);
                    addToView(getDateString(5, dow), ActivityHolder.schedule.friday);
                    break;
                case 7:
                    addToView(getDateString(7, dow, true), ActivityHolder.schedule.sunday);
                    addToView(getDateString(1, dow), ActivityHolder.schedule.monday);
                    addToView(getDateString(2, dow), ActivityHolder.schedule.tuesday);
                    addToView(getDateString(3, dow), ActivityHolder.schedule.wednesday);
                    addToView(getDateString(4, dow), ActivityHolder.schedule.thursday);
                    addToView(getDateString(5, dow), ActivityHolder.schedule.friday);
                    addToView(getDateString(6, dow), ActivityHolder.schedule.saturday);
                    break;
            }
            super.onPostExecute(aVoid);
        }
    }

}
