package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        ((StudIPApp) getApplicationContext()).setCurrentTopActivity(this);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(2).getSubMenu().getItem(3).setChecked(true);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nameofcurrentuser)).setText(StudIPHelper.current_user.getName().getFormatted());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.usernameel)).setText(StudIPHelper.current_user.getUsername());
        if (StudIPHelper.profile_pic != null)
            setProfilePic();
        CircleImageView civ = findViewById(R.id.nico_image);
        civ.setImageResource(R.drawable.nico);
        civ.setBorderColor(0xffffffff);
        civ.setBorderWidth(15);
        ((TextView) findViewById(R.id.version_text)).setText(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        ((TextView) findViewById(R.id.about_text)).setText(getString(R.string.about_string));

        findViewById(R.id.button_email_me).setOnClickListener((v) -> {
            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "nico.mexis@kabelmail.de", null));
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Stud.IP App Bug");
            getApplicationContext().startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
        });

        findViewById(R.id.button_source).setOnClickListener((v) -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/ThexXTURBOXx/studip-app-uni-passau"));
            startActivity(intent);
        });

        findViewById(R.id.button_credits).setOnClickListener((v) -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/ThexXTURBOXx/studip-app-uni-passau/blob/master/README.md#credits"));
            startActivity(intent);
        });

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
            Intent intent = new Intent(AboutActivity.this, ScheduleActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_mensa) {
            Intent intent = new Intent(AboutActivity.this, MensaActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(AboutActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bugreport) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ThexXTURBOXx/studip-app-uni-passau/issues/new"));
            startActivity(intent);
        } else if (id == R.id.open_in_browser) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://studip.uni-passau.de/studip/index.php"));
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
