package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Base64;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.threeten.bp.ZoneId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import de.femtopedia.studip.StudIPAPI;
import de.femtopedia.studip.json.User;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.activities.AboutActivity;
import studip_uni_passau.femtopedia.de.unipassaustudip.activities.FileListActivity;
import studip_uni_passau.femtopedia.de.unipassaustudip.activities.MensaActivity;
import studip_uni_passau.femtopedia.de.unipassaustudip.activities.ScheduleActivity;
import studip_uni_passau.femtopedia.de.unipassaustudip.activities.SettingsActivity;
import studip_uni_passau.femtopedia.de.unipassaustudip.api.MensaPlan;
import studip_uni_passau.femtopedia.de.unipassaustudip.api.ScheduledEvent;
import studip_uni_passau.femtopedia.de.unipassaustudip.natives.OAuthValues;

/**
 * Created by Nico Mexis on 22.10.2018.
 */

public class StudIPHelper {

    public static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final String CONSUMER_KEY = new String(Base64.decode(OAuthValues.getConsumerKey(), Base64.DEFAULT));
    private static final String CONSUMER_SECRET = new String(Base64.decode(OAuthValues.getConsumerSecret(), Base64.DEFAULT));
    private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final Type scheduleType = new TypeToken<Map<Integer, List<ScheduledEvent>>>() {
    }.getType();
    public static String target = null;
    public static Map<Integer, List<ScheduledEvent>> schedule = null;
    public static MensaPlan mensaPlan = new MensaPlan();
    private static StudIPAPI api = null;
    private static User currentUser = null;
    private static Bitmap profilePic = null;

    public static void constructAPI(boolean online, boolean auth) {
        if (auth || !online) {
            if (StudIPHelper.api == null)
                StudIPHelper.api = new StudIPAPI(CONSUMER_KEY, CONSUMER_SECRET);
        } else {
            StudIPHelper.api = new StudIPAPI(CONSUMER_KEY, CONSUMER_SECRET);
        }
    }

    public static void updatePic(Bitmap profile_pic, StudIPApp application) {
        StudIPHelper.profilePic = profile_pic;
        Activity a = application.getCurrentActivity();
        if (a instanceof ProfilePicHolder)
            ((ProfilePicHolder) a).setProfilePic();
    }

    public static void loadMensaPlan(Context context) {
        StudIPHelper.mensaPlan = StudIPHelper.loadFromFile(new File(context.getFilesDir(), "mensa-plan.json"), MensaPlan.class);
    }

    public static void updateMensaPlan(Context context, MensaPlan mensaPlan) {
        StudIPHelper.mensaPlan = mensaPlan;
        StudIPHelper.saveToFile(new File(context.getFilesDir(), "mensa-plan.json"), StudIPHelper.mensaPlan);
    }

    public static void loadSchedule(Context context) {
        StudIPHelper.schedule = StudIPHelper.loadFromFile(new File(context.getFilesDir(), "schedule.json"), scheduleType);
    }

    public static void updateSchedule(Context context, Map<Integer, List<ScheduledEvent>> schedule) {
        if (schedule == null)
            return;
        StudIPHelper.schedule = schedule;
        StudIPHelper.saveToFile(new File(context.getFilesDir(), "schedule.json"), StudIPHelper.schedule, scheduleType);
    }

    public static <T> T loadFromFile(File file, Type type) {
        initFile(file);
        try {
            return gson.fromJson(new BufferedReader(new FileReader(file)), type);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            SentryUtil.logError(e);
            if (!file.delete()) {
                file.deleteOnExit();
            } else {
                initFile(file);
            }
        }
        return null;
    }

    public static <T> T loadFromFile(File file, Class<T> clazz) {
        initFile(file);
        try {
            return gson.fromJson(new BufferedReader(new FileReader(file)), clazz);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            SentryUtil.logError(e);
            if (!file.delete()) {
                file.deleteOnExit();
            } else {
                initFile(file);
            }
        }
        return null;
    }

    public static <T> void saveToFile(File file, T obj, Type t) {
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

    public static void saveToFile(File file, Object obj) {
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

    public static void updateNavigation(int id, int currentActivity, Activity activity) {
        if (id != currentActivity) {
            if (id == R.id.nav_schedule) {
                Intent intent = new Intent(activity, ScheduleActivity.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_mensa) {
                Intent intent = new Intent(activity, MensaActivity.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_filelist) {
                Intent intent = new Intent(activity, FileListActivity.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_manage) {
                Intent intent = new Intent(activity, SettingsActivity.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_bugreport) {
                final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "studipapp@femtopedia.de", null));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.bug_subject));
                activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.send_mail)));
            } else if (id == R.id.nav_about) {
                Intent intent = new Intent(activity, AboutActivity.class);
                activity.startActivity(intent);
            } else if (id == R.id.open_in_browser) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://studip.uni-passau.de/studip/index.php"));
                activity.startActivity(intent);
            } else if (id == R.id.nav_telegram_bot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.dialog_telegram_desc)
                        .setTitle(R.string.dialog_telegram_title);
                builder.setPositiveButton(R.string.button_okay, (dialog, id1) -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://t.me/UniPassauBot"));
                    activity.startActivity(intent);
                }).setNegativeButton(R.string.button_cancel, (dialog, id1) -> {
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (id == R.id.nav_share) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.share_subject));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, activity.getString(R.string.share_body));
                activity.startActivity(Intent.createChooser(sharingIntent, activity.getString(R.string.share_via)));
            }
        }
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    public static boolean initFile(File file) {
        boolean flag = false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists())
            flag = parent.mkdirs();
        try {
            if (!file.exists())
                flag = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static int getComplementaryColor(int color) {
        return Color.rgb(255 - Color.red(color),
                255 - Color.green(color),
                255 - Color.blue(color));
    }

    public static double getLuminance(int color) {
        return getLuminance(color, 0.299d, 0.587d, 0.114d);
    }

    public static double getLuminance(int color, double r, double g, double b) {
        return r * (double) Color.red(color) +
                g * (double) Color.green(color) +
                b * (double) Color.blue(color);
    }

    public static int contraColor(int color) {
        return getLuminance(color) > 128 ? Color.BLACK : Color.WHITE;
    }

    public static int transpColor(int transparency, int color) {
        return Color.argb(transparency,
                Color.red(color),
                Color.green(color),
                Color.blue(color));
    }

    public static int resultingTranspColor(int background, int overlay, float alpha) {
        float opacity = alpha / 255f;
        float compOpacity = 1f - opacity;
        return Color.rgb((int) (Color.red(overlay) * opacity + Color.red(background) * compOpacity),
                (int) (Color.green(overlay) * opacity + Color.green(background) * compOpacity),
                (int) (Color.blue(overlay) * opacity + Color.blue(background) * compOpacity));
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        StudIPHelper.currentUser = currentUser;
        /*if (currentUser != null) {
            SentryUtil.setUser(currentUser.getName().getFormatted(), currentUser.getEmail());
        }*/
    }

    public static Bitmap getProfilePic() {
        return profilePic;
    }

    public static void setProfilePic(Bitmap profilePic) {
        StudIPHelper.profilePic = profilePic;
    }

    public static StudIPAPI getApi() {
        return api;
    }

    public interface ProfilePicHolder {
        void setProfilePic();
    }

    public interface NavigationDrawerActivity {
        void setActive();
    }

}
