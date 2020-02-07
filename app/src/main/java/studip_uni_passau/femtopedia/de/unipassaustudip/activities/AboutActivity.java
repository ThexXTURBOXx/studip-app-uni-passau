package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;
import studip_uni_passau.femtopedia.de.unipassaustudip.BuildConfig;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class AboutActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StudIPHelper.ProfilePicHolder,
        StudIPHelper.NavigationDrawerActivity {

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        ((StudIPApp) getApplicationContext()).setCurrentTopActivity(this);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setActive();
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nameofcurrentuser)).setText(StudIPHelper.getCurrentUser().getName().getFormatted());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.usernameel)).setText(StudIPHelper.getCurrentUser().getUsername());
        if (StudIPHelper.getProfilePic() != null) {
            setProfilePic();
        }
        ((TextView) findViewById(R.id.version_text)).setText(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((AppCompatTextView) findViewById(R.id.about_text)).setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        findViewById(R.id.button_email_me).setOnClickListener((v) -> {
            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "nico.mexis@kabelmail.de", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Stud.IP App Feedback");
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
        });

        findViewById(R.id.button_source).setOnClickListener((v) -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/ThexXTURBOXx/studip-app-uni-passau"));
            startActivity(intent);
        });

        findViewById(R.id.button_credits).setOnClickListener((v) -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/ThexXTURBOXx/studip-app-uni-passau/blob/master/README.md#credits"));
            startActivity(intent);
        });

        findViewById(R.id.button_translate).setOnClickListener((v) -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://poeditor.com/join/project/KUzUA6JkXe"));
            startActivity(intent);
        });

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

    @Override
    public void setActive() {
        if (navigationView != null)
            navigationView.getMenu().findItem(R.id.nav_about).setChecked(true);
    }

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(StudIPHelper.getProfilePic());
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        StudIPHelper.updateNavigation(item.getItemId(), R.id.nav_about, this);
        return true;
    }

}
