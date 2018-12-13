package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import de.femtopedia.studip.StudIPAPI;
import de.femtopedia.studip.json.User;

/**
 * Created by Nico Mexis on 22.10.2018.
 */

class StudIPHelper {

    static StudIPAPI api;

    static User current_user;
    static Bitmap profile_pic = null;
    static EventSchedule schedule;
    static MensaPlan mensaPlan = new MensaPlan();

    static void updatePic(Bitmap profile_pic, StudIPApp application) {
        StudIPHelper.profile_pic = profile_pic;
        Activity a = application.getCurrentActivity();
        if (a instanceof ScheduleActivity)
            ((ScheduleActivity) a).setProfilePic();
        else if (a instanceof MensaActivity)
            ((MensaActivity) a).setProfilePic();
        else if (a instanceof AboutActivity)
            ((AboutActivity) a).setProfilePic();
    }

    static void loadMensaPlan(Context context) {
        StudIPHelper.mensaPlan = StudIPHelper.loadFromFile(new File(context.getFilesDir(), "mensa-plan.json"), MensaPlan.class);
    }

    static void updateMensaPlan(Context context, MensaPlan mensaPlan) {
        StudIPHelper.mensaPlan = mensaPlan;
        StudIPHelper.saveToFile(new File(context.getFilesDir(), "mensa-plan.json"), StudIPHelper.mensaPlan);
    }

    static void loadSchedule(Context context) {
        StudIPHelper.schedule = StudIPHelper.loadFromFile(new File(context.getFilesDir(), "schedule.json"), EventSchedule.class);
    }

    static void updateSchedule(Context context, EventSchedule schedule) {
        StudIPHelper.schedule = schedule;
        StudIPHelper.saveToFile(new File(context.getFilesDir(), "schedule.json"), StudIPHelper.schedule);
    }

    @SuppressWarnings("EmptyCatchBlock")
    static <T> T loadFromFile(File file, Class<T> clazz) {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            BufferedReader in = new BufferedReader(new FileReader(file));
            return gson.fromJson(in, clazz);
        } catch (IOException e) {
        }
        return null;
    }

    static void saveToFile(File file, Object obj) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String ser = gson.toJson(obj);
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            PrintWriter out = new PrintWriter(fileOut);
            out.write(ser);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

}
