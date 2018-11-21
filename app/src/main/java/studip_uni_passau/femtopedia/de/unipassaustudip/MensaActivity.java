package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
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

import de.femtopedia.studip.shib.ShibbolethClient;
import de.hdodenhof.circleimageview.CircleImageView;

public class MensaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String mensaUrl = "https://www.stwno.de/infomax/daten-extern/csv/UNI-P/";
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    List<List<Object>> listDataChild;
    List<Integer> listDataColorsBg, listDataColorsText;
    DateTime dateTime;
    private TextView dateView;
    private NavigationView navigationView;
    private SwipeRefreshLayout swiperefresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mense);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);

        swiperefresher = findViewById(R.id.swiperefresh_mensa);
        swiperefresher.setOnRefreshListener(this::updateData);

        expListView = findViewById(R.id.mensacontent);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this,
                listDataHeader, listDataChild, listDataColorsBg, listDataColorsText);
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);

        swiperefresher.setRefreshing(true);
        updateData();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nameofcurrentuser)).setText(ActivityHolder.current_user.getName().getFormatted());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.usernameel)).setText(ActivityHolder.current_user.getUsername());
        if (ActivityHolder.profile_pic != null)
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

        dateView = findViewById(R.id.dateView);
        setDate(new DateTime().withTime(0, 0, 0, 0));
    }

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(ActivityHolder.profile_pic);
    }

    private void updateData() {
        CacheMensaPlan data = new CacheMensaPlan();
        data.execute();
    }

    private void setDate(DateTime dt) {
        int days = Days.daysBetween(new DateTime().withDayOfWeek(DateTimeConstants.MONDAY).toLocalDate(), dt.toLocalDate()).getDays();
        if (days < 14 && days >= 0) {
            this.dateTime = dt;
            dateView.setText(getDateString(dt));
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
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_schedule) {
            Intent intent = new Intent(MensaActivity.this, ScheduleActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MensaActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bugreport) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ThexXTURBOXx/studip-app-uni-passau/issues/new"));
            startActivity(intent);
        } else if (id == R.id.nav_about) {
        } else if (id == R.id.open_in_browser) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://studip.uni-passau.de/studip/index.php"));
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setToView(DateTime dt) {
        MensaPlan.DayMenu menu = ActivityHolder.mensaPlan.menu.get(dt.getMillis());
        if (menu != null) {
            addListItem(getString(R.string.soup), new ArrayList<>(), Color.BLACK, Color.WHITE);
            for (MensaPlan.Food f : menu.soups) {
                addFood(f, "7bad41");
            }
            addListItem(getString(R.string.mains), new ArrayList<>(), Color.BLACK, Color.WHITE);
            for (MensaPlan.Food f : menu.mains) {
                addFood(f, "ea3838");
            }
            addListItem(getString(R.string.garnishes), new ArrayList<>(), Color.BLACK, Color.WHITE);
            for (MensaPlan.Food f : menu.garnishes) {
                addFood(f, "61dfed");
            }
            addListItem(getString(R.string.desserts), new ArrayList<>(), Color.BLACK, Color.WHITE);
            for (MensaPlan.Food f : menu.desserts) {
                addFood(f, "baac18");
            }
        }
    }

    public void addFood(MensaPlan.Food f, String colorBg) {
        List<Object> info = new ArrayList<>();
        List<Integer> images = new ArrayList<>();
        for (MensaPlan.FoodProperty fp : f.properties)
            images.add(fp.drawable);
        info.add(images);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        info.add(getString(R.string.students) + ": " + formatter.format(f.price_stud));
        info.add(getString(R.string.servants) + ": " + formatter.format(f.price_bed));
        info.add(getString(R.string.guests) + ": " + formatter.format(f.price_guest));
        int color = Color.parseColor("#" + colorBg);
        double lum = 0.299d * (double) Color.red(color) + 0.587d * (double) Color.green(color) + 0.114d * (double) Color.blue(color);
        addListItem(f.name, info, color, lum > 128 ? Color.BLACK : Color.WHITE);
    }

    private String getDateString(DateTime time) {
        int day = time.getDayOfWeek();
        int rr = Days.daysBetween(new DateTime().toLocalDate(), time.toLocalDate()).getDays();
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

    @SuppressWarnings("useSparseArrays")
    public Map<Long, MensaPlan.DayMenu> parseMensaPlan(HttpResponse csv) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy");
        Map<Long, MensaPlan.DayMenu> dayMenus = new HashMap<>();
        try {
            InputStream content = csv.getEntity().getContent();
            MensaPlan.Food food = null;
            MensaPlan.DayMenu menu = new MensaPlan.DayMenu();
            String time = "";
            DateTime dt;
            for (String s : ShibbolethClient.readLines(content, "Cp1252")) {
                if (food == null) {
                    food = new MensaPlan.Food();
                    continue;
                }
                food = new MensaPlan.Food();
                String[] cols = s.split(";");
                food.name = cols[3];
                food.properties = new ArrayList<>();
                for (String c : cols[4].split(",")) {
                    MensaPlan.FoodProperty fp = MensaPlan.FoodProperty.getProperty(c);
                    if (fp != null)
                        food.properties.add(fp);
                }
                food.price_stud = Double.parseDouble(cols[6].replace(",", "."));
                food.price_bed = Double.parseDouble(cols[7].replace(",", "."));
                food.price_guest = Double.parseDouble(cols[8].replace(",", "."));
                if (!cols[0].equals(time)) {
                    if (!time.equals("")) {
                        dt = formatter.parseDateTime(time);
                        dayMenus.put(dt.withTime(0, 0, 0, 0).getMillis(), menu);
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
                dt = formatter.parseDateTime(time);
                dayMenus.put(dt.withTime(0, 0, 0, 0).getMillis(), menu);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dayMenus;
    }

    @SuppressWarnings("StaticFieldLeak")
    public class CacheMensaPlan extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... url) {
            try {
                int week = new DateTime().getWeekOfWeekyear();
                int next_week = new DateTime().plusDays(7).getWeekOfWeekyear();
                ActivityHolder.mensaPlan.menu.putAll(parseMensaPlan(ActivityHolder.api.getShibbolethClient().getIfValid(mensaUrl + week + ".csv")));
                ActivityHolder.mensaPlan.menu.putAll(parseMensaPlan(ActivityHolder.api.getShibbolethClient().getIfValid(mensaUrl + (next_week) + ".csv")));
            } catch (IllegalAccessException e) {
                Intent intent = new Intent(MensaActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return 2;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer success) {
            if (success == 0) {
                clearListItems();
                setToView(dateTime.withTime(0, 0, 0, 0));
                swiperefresher.setRefreshing(false);
            } else if (success == 1) {
                updateData();
            }
            super.onPostExecute(success);
        }
    }

}
