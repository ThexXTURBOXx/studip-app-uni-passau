package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.app.Application;

public class StudIPApp extends Application {

    //TODO Maybe later this needs optimization
    public static StudIPApp app;
    private Activity mCurrentActivity = null;

    public void onCreate() {
        app = this;
        super.onCreate();
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

}