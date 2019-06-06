package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
import studip_uni_passau.femtopedia.de.unipassaustudip.util.ExpandableListAdapter;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class MensaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StudIPHelper.ProfilePicHolder {

    private static final LocalDateTime JAN_1_1970 = new LocalDateTime(1970, 1, 1, 0, 0);
    public static String mensaUrl = "https://www.stwno.de/infomax/daten-extern/csv/UNI-P/";
    private ExpandableListAdapter listAdapter;
    private List<Object> listDataHeader;
    private List<List<Object>> listDataChild;
    private List<Integer> listDataColorsBg, listDataColorsText;
    private DateTime dateTime;
    private TextView dateView;
    private NavigationView navigationView;
    private SwipeRefreshLayout swiperefresher;

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

        swiperefresher = findViewById(R.id.swiperefresh_mensa);
        swiperefresher.setOnRefreshListener(this::updateData);

        ExpandableListView expListView = findViewById(R.id.mensacontent);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this,
                listDataHeader, listDataChild, listDataColorsBg, listDataColorsText);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

        dateView = findViewById(R.id.dateView);
        setDate(new DateTime().withTime(0, 0, 0, 0).withZone(StudIPHelper.ZONE));

        updateDataFirst();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

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

    private long getMillis(DateTime dt) {
        return new Duration(JAN_1_1970.toDateTime(StudIPHelper.ZONE), dt.withZone(StudIPHelper.ZONE)).getMillis();
    }

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(StudIPHelper.profile_pic);
    }

    private void updateDataFirst() {
        StudIPHelper.loadMensaPlan(this.getApplicationContext());
        updateMensaPlan();
        if (StudIPHelper.isNetworkAvailable(this) && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto_sync", true)) {
            swiperefresher.setRefreshing(true);
            CacheMensaPlan data = new CacheMensaPlan();
            data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateData() {
        if (StudIPHelper.isNetworkAvailable(this)) {
            swiperefresher.setRefreshing(true);
            CacheMensaPlan data = new CacheMensaPlan();
            data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            swiperefresher.setRefreshing(false);
        }
    }

    private void setDate(DateTime dt) {
        int days = Days.daysBetween(new DateTime().withDayOfWeek(DateTimeConstants.MONDAY)
                .withZone(StudIPHelper.ZONE).toLocalDate(), dt.withZone(StudIPHelper.ZONE).toLocalDate()).getDays();
        if (days < 14 && days >= 0) {
            this.dateTime = dt.withTime(0, 0, 0, 0).withZone(StudIPHelper.ZONE);
            dateView.setText(getDateString(this.dateTime));
            clearListItems();
            setToView(dateTime);
        }
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void onClickButtonDatePrev(View v) {
        setDate(dateTime.minusDays(1));
    }

    public void onClickButtonDateNext(View v) {
        setDate(dateTime.plusDays(1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_refresh, menu);
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

    private void setToView(DateTime dt) {
        if (StudIPHelper.mensaPlan == null)
            return;
        MensaPlan.DayMenu menu = StudIPHelper.mensaPlan.menu.get(getMillis(dt));
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

    private String getDateString(DateTime time) {
        int day = time.getDayOfWeek();
        int rr = Days.daysBetween(new DateTime().withZone(StudIPHelper.ZONE).toLocalDate(),
                time.toLocalDate()).getDays();
        StringBuilder sb = new StringBuilder();
        if (rr == 0)
            sb = sb.append(getString(R.string.today)).append(", ");
        else if (rr == 1)
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

    @SuppressWarnings({"useSparseArrays", "StringContatenationInLoop"})
    public Map<Long, MensaPlan.DayMenu> parseMensaPlan(CustomAccessHttpResponse csv) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy");
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
            DateTime dt;
            for (String s : OAuthClient.readLines(content, "Cp1252")) {
                if (food == null) {
                    food = new MensaPlan.Food();
                    continue;
                }
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
                        dt = formatter.parseDateTime(time).withZone(StudIPHelper.ZONE);
                        dayMenus.put(getMillis(dt.withTime(0, 0, 0, 0)), menu);
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
            }
            if (!time.equals("")) {
                dt = formatter.parseDateTime(time).withZone(StudIPHelper.ZONE);
                dayMenus.put(getMillis(dt.withTime(0, 0, 0, 0)), menu);
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
            if (fp != null && fp != MensaPlan.FoodProperty.NONE)
                food.properties.add(fp);
        }
    }

    public void updateMensaPlan() {
        if (StudIPHelper.mensaPlan == null)
            return;
        clearListItems();
        setToView(dateTime.withTime(0, 0, 0, 0).withZone(StudIPHelper.ZONE));
    }

    @SuppressWarnings("StaticFieldLeak")
    public class CacheMensaPlan extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... url) {
            try {
                int week = new DateTime().withZone(StudIPHelper.ZONE).getWeekOfWeekyear();
                int next_week = new DateTime().withZone(StudIPHelper.ZONE).plusDays(7).getWeekOfWeekyear();
                if (StudIPHelper.mensaPlan == null)
                    StudIPHelper.mensaPlan = new MensaPlan();
                StudIPHelper.mensaPlan.menu.putAll(parseMensaPlan(StudIPHelper.api.getOAuthClient().get(mensaUrl + week + ".csv")));
                StudIPHelper.mensaPlan.menu.putAll(parseMensaPlan(StudIPHelper.api.getOAuthClient().get(mensaUrl + (next_week) + ".csv")));
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
                swiperefresher.setRefreshing(false);
            } else if (success == 1) {
                updateData();
            }
            super.onPostExecute(success);
        }
    }

}
