package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.graphics.Bitmap;

import de.femtopedia.studip.StudIPAPI;
import de.femtopedia.studip.json.User;

/**
 * Created by Nico Mexis on 22.10.2018.
 */

public class ActivityHolder {

    public static StudIPAPI api;

    public static User current_user;
    public static Bitmap profile_pic = null;
    public static EventSchedule schedule;
    public static MensaPlan mensaPlan = new MensaPlan();

    public static void updatePic(Bitmap profile_pic) {
        ActivityHolder.profile_pic = profile_pic;
        Activity a = StudIPApp.app.getCurrentActivity();
        if (a instanceof ScheduleActivity)
            ((ScheduleActivity) a).setProfilePic();
        else if (a instanceof MensaActivity)
            ((MensaActivity) a).setProfilePic();
    }

}
