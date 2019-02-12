package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import oauth.signpost.exception.OAuthException;

public class LoginActivity extends AppCompatActivity {

    private CustomTabHelper tabHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
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

        OAuthTask() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return StudIPHelper.api.getAuthorizationUrl("studipassau://oauth_callback");
            } catch (OAuthException e) {
                e.printStackTrace();
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
