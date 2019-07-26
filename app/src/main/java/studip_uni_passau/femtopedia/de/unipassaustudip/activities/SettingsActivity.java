package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.fragments.StudIPPrefFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((StudIPApp) getApplicationContext()).setCurrentTopActivity(this);
        setContentView(R.layout.settings);
        if (savedInstanceState == null) {
            StudIPPrefFragment fragment = new StudIPPrefFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, StudIPPrefFragment.TAG)
                    .commit();
        }
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener((prefs, key) -> {
                    if (key.equals("theme_mode")) {
                        AppCompatDelegate.setDefaultNightMode(Integer.parseInt(prefs.getString("theme_mode",
                                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM + "")));
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
