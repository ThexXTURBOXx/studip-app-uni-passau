package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class StudIPApp extends MultiDexApplication {

    private Activity currentActivity = null;
    private Activity currentTopActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(
                Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM + "")));
    }

    public Activity getCurrentActivity() {
        return this.currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        if (this.currentActivity != null) {
            this.currentActivity.finish();
        }
        if (this.currentTopActivity != null) {
            this.currentTopActivity.finish();
            this.currentTopActivity = null;
        }
        this.currentActivity = currentActivity;
        if (this.currentActivity instanceof StudIPHelper.NavigationDrawerActivity) {
            ((StudIPHelper.NavigationDrawerActivity) this.currentActivity).setActive();
        }
    }

    public void setCurrentTopActivity(Activity currentTopActivity) {
        if (this.currentTopActivity != null) {
            this.currentTopActivity.finish();
        }
        this.currentTopActivity = currentTopActivity;
        if (this.currentActivity instanceof StudIPHelper.NavigationDrawerActivity) {
            ((StudIPHelper.NavigationDrawerActivity) this.currentActivity).setActive();
        }
        if (this.currentTopActivity instanceof StudIPHelper.NavigationDrawerActivity) {
            ((StudIPHelper.NavigationDrawerActivity) this.currentTopActivity).setActive();
        }
    }

}
