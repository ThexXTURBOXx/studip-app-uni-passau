package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.WeekFields;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.femtopedia.studip.shib.CustomAccessHttpResponse;
import de.femtopedia.studip.shib.OAuthClient;
import de.hdodenhof.circleimageview.CircleImageView;
import oauth.signpost.exception.OAuthException;
import okhttp3.ResponseBody;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.api.MensaPlan;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.AnimatingRefreshButtonManager;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.ExpandableListAdapter;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class MensaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StudIPHelper.ProfilePicHolder,
        StudIPHelper.NavigationDrawerActivity, OnDateSelectedListener {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String MENSA_URL = "https://www.stwno.de/infomax/daten-extern/csv/UNI-P/";
    private ExpandableListAdapter listAdapter;
    private List<Object> listDataHeader;
    private List<List<Object>> listDataChild;
    private List<Integer> listDataColorsBg, listDataColorsText;
    private MaterialCalendarView dateView;
    private NavigationView navigationView;
    private SwipeRefreshLayout swipeRefresher;
    private AnimatingRefreshButtonManager refreshManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (StudIPHelper.current_user == null) {
            Intent intent = new Intent(MensaActivity.this, LoadActivity.class);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.mense);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
        StudIPHelper.target = "mensa";

        swipeRefresher = findViewById(R.id.swiperefresh_mensa);
        swipeRefresher.setOnRefreshListener(this::updateData);

        ExpandableListView expListView = findViewById(R.id.mensacontent);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this,
                listDataHeader, listDataChild, listDataColorsBg, listDataColorsText);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

        dateView = findViewById(R.id.dateView);
        dateView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return day.equals(CalendarDay.today());
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(ContextCompat.getColor(MensaActivity.this, R.color.colorDark)));
            }
        });
        dateView.setOnDateChangedListener(this);
        dateView.state().edit()
                .setMinimumDate(LocalDate.now().with(DayOfWeek.MONDAY))
                .setMaximumDate(LocalDate.now().plusDays(7).with(DayOfWeek.SUNDAY))
                .commit();
        LocalDateTime now = LocalDateTime.now();
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("mensa_closing_time_active", true)) {
            if (getMinuteOfDay(now) >= PreferenceManager.getDefaultSharedPreferences(this)
                    .getInt("mensa_closing_time", 900)) {
                now = now.plusDays(1);
            }
        }
        setDate(CalendarDay.from(now.toLocalDate()));

        updateDataFirst();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setActive();

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

    private int getMinuteOfDay(LocalDateTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    @Override
    public void setActive() {
        if (navigationView != null)
            navigationView.getMenu().findItem(R.id.nav_mensa).setChecked(true);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if (selected) {
            setDate(date);
        }
    }

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(StudIPHelper.profile_pic);
    }

    private void updateDataFirst() {
        StudIPHelper.loadMensaPlan(this.getApplicationContext());
        updateMensaPlan();
        if (StudIPHelper.isNetworkAvailable(this) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto_sync", true)) {
            startUpdateAnimation();
            CacheMensaPlan data = new CacheMensaPlan();
            data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateData() {
        if (StudIPHelper.isNetworkAvailable(this)) {
            startUpdateAnimation();
            CacheMensaPlan data = new CacheMensaPlan();
            data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            stopUpdateAnimation();
        }
    }

    private void startUpdateAnimation() {
        if (swipeRefresher != null)
            swipeRefresher.setRefreshing(true);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (refreshManager != null)
                refreshManager.onRefreshBeginning();
        });
    }

    private void stopUpdateAnimation() {
        if (swipeRefresher != null)
            swipeRefresher.setRefreshing(false);
        if (refreshManager != null)
            refreshManager.onRefreshComplete();
    }

    private void setDate(CalendarDay calendarDay) {
        dateView.setSelectedDate(calendarDay);
        clearListItems();
        setToView(calendarDay);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataColorsBg = new ArrayList<>();
        listDataColorsText = new ArrayList<>();
        listDataChild = new ArrayList<>();
    }

    private void clearListItems() {
        listDataHeader.clear();
        listDataColorsBg.clear();
        listDataColorsText.clear();
        listDataChild.clear();

        listAdapter.notifyDataSetChanged();
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
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_refresh, menu);
        refreshManager = new AnimatingRefreshButtonManager(this, menu.findItem(R.id.action_refresh_bar));
        if (swipeRefresher != null && swipeRefresher.isRefreshing())
            refreshManager.onRefreshBeginning();
        else
            refreshManager.onRefreshComplete();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        StudIPHelper.updateNavigation(item.getItemId(), R.id.nav_mensa, this);
        return true;
    }

    private void setToView(CalendarDay dt) {
        if (StudIPHelper.mensaPlan == null)
            return;
        MensaPlan.DayMenu menu = StudIPHelper.mensaPlan.menu.get(dt.getDate().toEpochDay());
        if (menu != null) {
            int separatorColor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("separator_mensa_color", 0xFF000000);
            int separatorColorCon = StudIPHelper.contraColor(separatorColor);
            if (!menu.soups.isEmpty()) {
                int soupColor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("soup_color", 0xFF7bad41);
                int soupColorCon = StudIPHelper.contraColor(soupColor);
                addListItem(getString(R.string.soup), new ArrayList<>(), separatorColor, separatorColorCon);
                for (MensaPlan.Food f : menu.soups) {
                    addFood(f, soupColor, soupColorCon);
                }
            }
            if (!menu.mains.isEmpty()) {
                int mainColor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("main_color", 0xFFea3838);
                int mainColorCon = StudIPHelper.contraColor(mainColor);
                addListItem(getString(R.string.mains), new ArrayList<>(), separatorColor, separatorColorCon);
                for (MensaPlan.Food f : menu.mains) {
                    addFood(f, mainColor, mainColorCon);
                }
            }
            if (!menu.garnishes.isEmpty()) {
                int garnishColor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("garnish_color", 0xFF61dfed);
                int garnishColorCon = StudIPHelper.contraColor(garnishColor);
                addListItem(getString(R.string.garnishes), new ArrayList<>(), separatorColor, separatorColorCon);
                for (MensaPlan.Food f : menu.garnishes) {
                    addFood(f, garnishColor, garnishColorCon);
                }
            }
            if (!menu.desserts.isEmpty()) {
                int dessertColor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("desserts_color", 0xFFbaac18);
                int dessertColorCon = StudIPHelper.contraColor(dessertColor);
                addListItem(getString(R.string.desserts), new ArrayList<>(), separatorColor, separatorColorCon);
                for (MensaPlan.Food f : menu.desserts) {
                    addFood(f, dessertColor, dessertColorCon);
                }
            }
        }
    }

    public void addFood(MensaPlan.Food f, int colorBg, int colorText) {
        List<Object> info = new ArrayList<>();
        List<Integer> images = new ArrayList<>();
        for (MensaPlan.FoodProperty fp : f.properties)
            images.add(fp.drawable);
        info.add(images);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        info.add(getString(R.string.students) + ": " + formatter.format(f.price_stud));
        info.add(getString(R.string.servants) + ": " + formatter.format(f.price_bed));
        info.add(getString(R.string.guests) + ": " + formatter.format(f.price_guest));
        addListItem(f.name, info, colorBg, colorText);
    }

    @SuppressWarnings({"useSparseArrays", "StringContatenationInLoop"})
    public Map<Long, MensaPlan.DayMenu> parseMensaPlan(CustomAccessHttpResponse csv) {
        Map<Long, MensaPlan.DayMenu> dayMenus = new HashMap<>();
        InputStream content = null;
        try {
            ResponseBody body = csv.getResponse().body();
            if (body == null) {
                return new HashMap<>();
            }
            content = body.byteStream();
            MensaPlan.Food food = null;
            MensaPlan.DayMenu menu = new MensaPlan.DayMenu();
            String time = "";
            CalendarDay dt;
            for (String s : OAuthClient.readLines(content, "Cp1252")) {
                if (food == null) {
                    food = new MensaPlan.Food();
                    continue;
                }
                try {
                    food = new MensaPlan.Food();
                    String[] cols = s.split(";");
                    food.name = cols[3];
                    food.properties = new ArrayList<>();
                    parseProperties(food, cols[4]);
                    try {
                        food.price_stud = Double.parseDouble(cols[6].replace(",", "."));
                        food.price_bed = Double.parseDouble(cols[7].replace(",", "."));
                        food.price_guest = Double.parseDouble(cols[8].replace(",", "."));
                    } catch (NumberFormatException e) {
                        food.name = food.name + "," + cols[4];
                        food.properties.clear();
                        parseProperties(food, cols[5]);
                        try {
                            food.price_stud = Double.parseDouble(cols[7].replace(",", "."));
                            food.price_bed = Double.parseDouble(cols[8].replace(",", "."));
                            food.price_guest = Double.parseDouble(cols[9].replace(",", "."));
                        } catch (NumberFormatException e1) {
                            e1.printStackTrace();
                            food.price_stud = 0;
                            food.price_bed = 0;
                            food.price_guest = 0;
                        }
                    }
                    if (!cols[0].equals(time)) {
                        if (!time.equals("")) {
                            dt = CalendarDay.from(LocalDate.parse(time, DATE_PATTERN));
                            dayMenus.put(dt.getDate().toEpochDay(), menu);
                            menu = new MensaPlan.DayMenu();
                        }
                        time = cols[0];
                    }
                    if (cols[2].startsWith("Suppe"))
                        menu.soups.add(food);
                    else if (cols[2].startsWith("HG"))
                        menu.mains.add(food);
                    else if (cols[2].startsWith("B"))
                        menu.garnishes.add(food);
                    else if (cols[2].startsWith("N"))
                        menu.desserts.add(food);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (!time.equals("")) {
                dt = CalendarDay.from(LocalDate.parse(time, DATE_PATTERN));
                dayMenus.put(dt.getDate().toEpochDay(), menu);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                csv.close();
                if (content != null)
                    content.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dayMenus;
    }

    private void parseProperties(MensaPlan.Food food, String col) {
        for (String c : col.split(",")) {
            MensaPlan.FoodProperty fp = MensaPlan.FoodProperty.getProperty(c);
            if (fp != null)
                food.properties.add(fp);
        }
    }

    public void updateMensaPlan() {
        if (StudIPHelper.mensaPlan == null)
            return;
        clearListItems();
        setToView(dateView.getSelectedDate());
    }

    @SuppressWarnings("StaticFieldLeak")
    public class CacheMensaPlan extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... url) {
            try {
                LocalDate date = LocalDate.now();
                int week = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
                int next_week = date.plusDays(7).get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
                if (StudIPHelper.mensaPlan == null)
                    StudIPHelper.mensaPlan = new MensaPlan();
                StudIPHelper.mensaPlan.menu.putAll(parseMensaPlan(StudIPHelper.api.getOAuthClient().get(MENSA_URL + week + ".csv")));
                StudIPHelper.mensaPlan.menu.putAll(parseMensaPlan(StudIPHelper.api.getOAuthClient().get(MENSA_URL + (next_week) + ".csv")));
            } catch (IllegalAccessException | OAuthException e) {
                Intent intent = new Intent(MensaActivity.this, LoadActivity.class);
                startActivity(intent);
                return 2;
            } catch (IOException e) {
                return 1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer success) {
            if (success == 0) {
                StudIPHelper.updateMensaPlan(MensaActivity.this, StudIPHelper.mensaPlan);
                updateMensaPlan();
                stopUpdateAnimation();
            } else if (success == 1) {
                updateData();
            }
            super.onPostExecute(success);
        }
    }

}
