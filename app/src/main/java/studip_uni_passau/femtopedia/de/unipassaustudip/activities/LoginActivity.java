package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import liou.rayyuan.chromecustomtabhelper.Browsers;
import liou.rayyuan.chromecustomtabhelper.ChromeCustomTabsHelper;
import oauth.signpost.exception.OAuthException;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.SentryUtil;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class LoginActivity extends AppCompatActivity {

    private final ChromeCustomTabsHelper tabHelper = new ChromeCustomTabsHelper();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
        setContentView(R.layout.content_login);
        OAuthTask oAuthTask = new OAuthTask();
        oAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tabHelper.bindCustomTabsServices(this, Browsers.CHROME, "https://studip.uni-passau.de/");
    }

    @Override
    protected void onStop() {
        super.onStop();
        tabHelper.unbindCustomTabsServices(this);
    }

    @SuppressWarnings({"StaticFieldLeak"})
    public class OAuthTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            if (StudIPHelper.isNetworkAvailable(LoginActivity.this)) {
                try {
                    return StudIPHelper.getApi().getAuthorizationUrl("studipassau://oauth_callback");
                } catch (OAuthException e) {
                    e.printStackTrace();
                    SentryUtil.logError(e);
                }
            } else {
                return "";
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String authUrl) {
            if (authUrl != null) {
                if (authUrl.equals("")) {
                    ((TextView) findViewById(R.id.login_warning)).setText(R.string.login_warning_no_internet);
                } else {
                    CustomTabsIntent intent = new CustomTabsIntent.Builder()
                            .build();
                    intent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    ChromeCustomTabsHelper.openCustomTab(LoginActivity.this, Browsers.CHROME,
                            intent, Uri.parse(authUrl), (activity, uri) -> {
                                Intent fallback = new Intent("android.intent.action.VIEW", uri);
                                try {
                                    fallback.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    activity.startActivity(fallback);
                                } catch (ActivityNotFoundException e) {
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setTitle(R.string.browser_not_found)
                                            .setPositiveButton(R.string.button_okay, (dialog, id) -> {
                                            })
                                            .setMessage(R.string.browser_not_found_desc)
                                            .show();
                                }
                            });
                }
            }
        }

    }

}
