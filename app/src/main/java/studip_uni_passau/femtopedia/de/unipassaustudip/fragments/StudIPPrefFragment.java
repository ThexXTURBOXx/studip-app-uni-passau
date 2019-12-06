package studip_uni_passau.femtopedia.de.unipassaustudip.fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.rarepebble.colorpicker.ColorPreference;
import com.rarepebble.colorpicker.ColorPreferenceFragment;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.preference.time.TimePreference;
import studip_uni_passau.femtopedia.de.unipassaustudip.preference.time.TimePreferenceDialogFragmentCompat;

public class StudIPPrefFragment extends PreferenceFragmentCompat implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    public static final String TAG = "STUDIP_PREF_FRAGMENT";
    private int mFragmentContainerId;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        this.mFragmentContainerId = R.id.fragment_container;
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    private StudIPPrefFragment newInstance() {
        try {
            return this.getClass().newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException ie) {
            throw new RuntimeException(ie);
        }
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        StudIPPrefFragment fragment = newInstance();

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);

        showFragment(fragment);
        return true;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ColorPreference) {
            ColorPreferenceFragment f = ColorPreferenceFragment.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            if (getFragmentManager() != null) {
                f.show(getFragmentManager(), TAG);
            }
        } else if (preference instanceof TimePreference) {
            DialogFragment dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            if (this.getFragmentManager() != null) {
                dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference" +
                        ".PreferenceFragment.DIALOG");
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    private void showFragment(Fragment fragment) {
        if (mFragmentContainerId == 0)
            throw new Error("You must call setFragmentContainerId(int) in onCreatePreferences()!");

        FragmentActivity activity = getActivity();
        if (activity != null) {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(mFragmentContainerId, fragment, TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

}
