package studip_uni_passau.femtopedia.de.unipassaustudip.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.rarepebble.colorpicker.ColorPreference;
import com.rarepebble.colorpicker.ColorPreferenceFragment;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class StudIPPrefFragment extends PreferenceFragmentCompat implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    public static final String TAG = "STUDIP_PREF_FRAGMENT";
    private int mFragmentContainerId;

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setFragmentContainerId(R.id.fragment_container);
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    public StudIPPrefFragment newInstance() {
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

        showFragment(fragment, TAG, true);
        return true;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ColorPreference) {
            ColorPreferenceFragment f = ColorPreferenceFragment.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    public void setFragmentContainerId(int fragmentContainerId) {
        mFragmentContainerId = fragmentContainerId;
    }

    public void showFragment(Fragment fragment, String tag, boolean addToBackStack) {
        if (mFragmentContainerId == 0)
            throw new Error("You must call setFragmentContainerId(int) in onCreatePreferences()!");

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(mFragmentContainerId, fragment, tag);
        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

}
