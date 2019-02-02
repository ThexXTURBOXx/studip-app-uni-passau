package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.cookie.Cookie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;

import de.femtopedia.studip.StudIPAPI;
import de.femtopedia.studip.json.User;

/**
 * Created by Nico Mexis on 22.10.2018.
 */

class StudIPHelper {

    static StudIPAPI api = null;

    static User current_user;
    static Bitmap profile_pic = null;
    static Map<Integer, List<ScheduledEvent>> schedule = null;
    static MensaPlan mensaPlan = new MensaPlan();

    private static Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static Type scheduleType = new TypeToken<Map<Integer, List<ScheduledEvent>>>() {
    }.getType();

    static void constructAPI(List<Cookie> cookies) {
        if (StudIPHelper.api != null)
            StudIPHelper.api.shutdown();
        StudIPHelper.api = new StudIPAPI(cookies, null, "");
    }

    static void logIntoAPI(Context context, List<Cookie> cookies, String username, String password, boolean authenticate)
            throws IllegalArgumentException, IllegalAccessException, IllegalStateException, IOException {
        try {
            if (authenticate)
                StudIPHelper.api.authenticate(username, password);
            StudIPHelper.api.getShibbolethClient()
                    .get("https://studip.uni-passau.de/studip/index.php").getResponse().getEntity().getContent();
            StudIPHelper.api.getShibbolethClient()
                    .get("https://www.stwno.de/infomax/daten-extern/csv/UNI-P/1.csv").getResponse().getEntity().getContent();
        } catch (SSLPeerUnverifiedException e) {
            System.out.println("SSLPeerUnverifiedException thrown, using KeyStore.");
            StudIPHelper.api.shutdown();
            StudIPHelper.api = new StudIPAPI(cookies,
                    context.getResources().openRawResource(R.raw.newtruststore), "012345");
            if (authenticate)
                StudIPHelper.api.authenticate(username, password);
        }
    }

    static void updatePic(Bitmap profile_pic, StudIPApp application) {
        StudIPHelper.profile_pic = profile_pic;
        Activity a = application.getCurrentActivity();
        if (a instanceof ProfilePicHolder)
            ((ProfilePicHolder) a).setProfilePic();
    }

    static void loadMensaPlan(Context context) {
        StudIPHelper.mensaPlan = StudIPHelper.loadFromFile(new File(context.getFilesDir(), "mensa-plan.json"), MensaPlan.class);
    }

    static void updateMensaPlan(Context context, MensaPlan mensaPlan) {
        StudIPHelper.mensaPlan = mensaPlan;
        StudIPHelper.saveToFile(new File(context.getFilesDir(), "mensa-plan.json"), StudIPHelper.mensaPlan);
    }

    static void loadSchedule(Context context) {
        StudIPHelper.schedule = StudIPHelper.loadFromFile(new File(context.getFilesDir(), "schedule.json"), scheduleType);
    }

    static void updateSchedule(Context context, Map<Integer, List<ScheduledEvent>> schedule) {
        if (schedule == null)
            return;
        StudIPHelper.schedule = schedule;
        StudIPHelper.saveToFile(new File(context.getFilesDir(), "schedule.json"), StudIPHelper.schedule, scheduleType);
    }

    static <T> T loadFromFile(File file, Type type) {
        initFile(file);
        try {
            return gson.fromJson(new BufferedReader(new FileReader(file)), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static <T> T loadFromFile(File file, Class<T> clazz) {
        initFile(file);
        try {
            return gson.fromJson(new BufferedReader(new FileReader(file)), clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static <T> void saveToFile(File file, T obj, Type t) {
        initFile(file);
        String ser = gson.toJson(obj, t);
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

    static void saveToFile(File file, Object obj) {
        initFile(file);
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

    static boolean initFile(File file) {
        boolean flag = false;
        File parent = file.getParentFile();
        if (!parent.exists())
            flag = parent.mkdirs();
        try {
            if (!file.exists())
                flag = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    interface ProfilePicHolder {
        void setProfilePic();
    }

}
