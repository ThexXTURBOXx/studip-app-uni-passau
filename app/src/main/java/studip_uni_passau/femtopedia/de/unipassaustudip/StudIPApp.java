package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.app.Application;

public class StudIPApp extends Application {

    private Activity currentActivity = null;
    private Activity currentTopActivity = null;

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
    }

    public void setCurrentTopActivity(Activity currentTopActivity) {
        if (this.currentTopActivity != null) {
            this.currentTopActivity.finish();
        }
        this.currentTopActivity = currentTopActivity;
    }

}