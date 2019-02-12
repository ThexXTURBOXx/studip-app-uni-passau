package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import studip_uni_passau.femtopedia.de.unipassaustudip.activities.SettingsActivity;

public abstract class StudIPPreferenceFragment extends PreferenceFragment {

    public abstract int getPrefResource();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(getPrefResource());
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
