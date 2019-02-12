package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import oauth.signpost.exception.OAuthException;

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
                    return StudIPHelper.api.getAuthorizationUrl("studipassau://oauth_callback");
                } catch (OAuthException e) {
                    e.printStackTrace();
                }
            } else {
                ((TextView) findViewById(R.id.login_warning)).setText(R.string.login_warning_no_internet);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String authUrl) {
            if (authUrl != null) {
                tabHelper = StudIPHelper.authenticate(LoginActivity.this, authUrl);
            }
        }

    }

}
