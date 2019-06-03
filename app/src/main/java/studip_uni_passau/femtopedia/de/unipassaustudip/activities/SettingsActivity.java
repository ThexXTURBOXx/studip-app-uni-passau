package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onOptionsItemSelected(item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
