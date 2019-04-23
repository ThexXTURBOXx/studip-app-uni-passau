package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.content.Context;

import androidx.multidex.MultiDexApplication;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "studipapp@femtopedia.de",
        resSubject = R.string.crash_subject,
        reportAsFile = false)
@AcraDialog(resText = R.string.crash_dialog_text,
        resCommentPrompt = R.string.crash_dialog_comment)
public class StudIPApp extends MultiDexApplication {

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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }

}
