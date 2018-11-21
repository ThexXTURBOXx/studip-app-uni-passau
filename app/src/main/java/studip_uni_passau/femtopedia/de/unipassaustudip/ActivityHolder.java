package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.graphics.Bitmap;

import de.femtopedia.studip.StudIPAPI;
import de.femtopedia.studip.json.User;

/**
 * Created by Nico Mexis on 22.10.2018.
 */

class ActivityHolder {

    static StudIPAPI api;

    static User current_user;
    static Bitmap profile_pic = null;
    static EventSchedule schedule;
    static MensaPlan mensaPlan = new MensaPlan();

    static void updatePic(Bitmap profile_pic, StudIPApp application) {
        ActivityHolder.profile_pic = profile_pic;
        Activity a = application.getCurrentActivity();
        if (a instanceof ScheduleActivity)
            ((ScheduleActivity) a).setProfilePic();
        else if (a instanceof MensaActivity)
            ((MensaActivity) a).setProfilePic();
    }

}
