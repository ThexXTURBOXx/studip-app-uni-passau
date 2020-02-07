package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import oauth.signpost.exception.OAuthException;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.CustomTabHelper;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.SentryUtil;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class LoginActivity extends AppCompatActivity {

    private CustomTabHelper tabHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
        setContentView(R.layout.content_login);
        OAuthTask oAuthTask = new OAuthTask();
        oAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void finish() {
        super.finish();
        if (this.tabHelper != null)
            this.tabHelper.unbindCustomTabsService();
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
                    tabHelper = StudIPHelper.authenticate(LoginActivity.this, authUrl);
                }
            }
        }

    }

}
