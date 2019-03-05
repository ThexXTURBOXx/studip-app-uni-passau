package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import org.apache.http.HttpEntity;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import de.femtopedia.studip.json.User;
import de.femtopedia.studip.shib.CustomAccessHttpResponse;
import oauth.signpost.exception.OAuthException;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.api.OAuthData;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;

public class LoadActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private VerifyTask verifyTask = null;
    private View mProgressView;
    private TextView loadingStatus;
    private boolean loggedIn = false, checkedForUpdates = false;
    private String oAuthVerifier = null;
    private File oAuthDataFile;

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.load_activity_name);

        setContentView(R.layout.activity_load);

        AppUpdater updater = new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("http://femtopedia.de/studip/version.json")
                .setOnFinish((v, i) -> {
                    checkedForUpdates = true;
                    tryIntentChange();
                });
        updater.start();

        //Delete Legacy Data
        File cookieDir = new File(getApplicationContext().getFilesDir(), "/cookies");
        if (cookieDir.exists()) {
            for (File f : cookieDir.listFiles()) {
                f.delete();
            }
            cookieDir.delete();
        }

        mProgressView = findViewById(R.id.load_progress);
        loadingStatus = findViewById(R.id.load_progress_status);

        startLoading();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    private void startLoading() {
        if (verifyTask != null) {
            return;
        }

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mProgressView.animate().setDuration(shortAnimTime).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(View.VISIBLE);
                    }
                });
        loadingStatus.animate().setDuration(shortAnimTime).alpha(1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(View.VISIBLE);
                    }
                });

        verifyTask = new VerifyTask();
        verifyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private void tryIntentChange() {
        if (loggedIn && checkedForUpdates) {
            Intent intent;
            if (StudIPHelper.target != null) {
                switch (StudIPHelper.target) {
                    case "mensa":
                        intent = new Intent(LoadActivity.this, MensaActivity.class);
                        break;
                    default:
                        intent = new Intent(LoadActivity.this, ScheduleActivity.class);
                        break;
                }
            } else {
                intent = new Intent(LoadActivity.this, ScheduleActivity.class);
            }
            startActivity(intent);
        }
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Nickname.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Nickname.IS_PRIMARY,
        };
    }

    /**
     * Represents an asynchronous data loading task used to verify API access.
     */
    @SuppressWarnings({"StaticFieldLeak"})
    public class VerifyTask extends AsyncTask<Void, Void, Integer> {

        VerifyTask() {
            loadingStatus.setText(getString(R.string.verifying_api));
            oAuthDataFile = new File(getApplicationContext().getFilesDir(), "/oauth.ser");
            StudIPHelper.initFile(oAuthDataFile);
            if (getIntent() != null) {
                Intent intent = getIntent();
                if (intent.getExtras() != null) {
                    StudIPHelper.target = intent.getExtras().getString("target");
                }
                String action = intent.getAction();
                String data = intent.getDataString();
                if (Intent.ACTION_VIEW.equals(action) && data != null) {
                    oAuthVerifier = Uri.parse(data).getQueryParameter("oauth_verifier");
                }
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                boolean internetAvailable = StudIPHelper.isNetworkAvailable(LoadActivity.this);
                StudIPHelper.constructAPI();
                if (internetAvailable)
                    StudIPHelper.verifyAPI(LoadActivity.this);
                if (oAuthVerifier != null && internetAvailable) {
                    StudIPHelper.api.verifyAccess(oAuthVerifier);
                } else {
                    OAuthData saveData = StudIPHelper.loadFromFile(oAuthDataFile, OAuthData.class);
                    if (saveData != null) {
                        StudIPHelper.api.getOAuthClient().setToken(saveData.accessToken, saveData.accessTokenSecret);
                    } else {
                        return 2;
                    }
                }
                if (!internetAvailable) {
                    return 1;
                }
                if (!StudIPHelper.api.getOAuthClient().isSessionValid()) {
                    return 2;
                }
            } catch (IllegalAccessException | OAuthException e) {
                return 2;
            } catch (IOException | IllegalStateException | IllegalArgumentException e) {
                e.printStackTrace();
                return 3;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            verifyTask = null;

            switch (success) {
                case 0:
                    //Success
                    String[] token = StudIPHelper.api.getOAuthClient().getToken();
                    StudIPHelper.saveToFile(oAuthDataFile, new OAuthData(token[0], token[1]));
                    //No break here
                case 1:
                    //Offline
                    CacheCurrentUserData data = new CacheCurrentUserData();
                    data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case 2:
                    //Wrong credentials
                    Intent intent = new Intent(LoadActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
                default:
                    //IO and various Error
                    verifyTask = new VerifyTask();
                    verifyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            verifyTask = null;
        }

    }

    @SuppressWarnings({"StaticFieldLeak"})
    public class CacheCurrentUserData extends AsyncTask<Void, Void, User> {

        CacheCurrentUserData() {
            loadingStatus.setText(getString(R.string.loading_user_data));
            StudIPHelper.current_user = StudIPHelper.loadFromFile(new File(getApplicationContext().getFilesDir(), "user.json"), User.class);
        }

        @Override
        protected User doInBackground(Void... voids) {
            if (!StudIPHelper.isNetworkAvailable(LoadActivity.this))
                return null;
            try {
                return StudIPHelper.api.getCurrentUserData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException | OAuthException e) {
                Intent intent = new Intent(((StudIPApp) getApplication()).getCurrentActivity(),
                        LoadActivity.class);
                startActivity(intent);
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user == null && StudIPHelper.current_user == null) {
                CacheCurrentUserData data = new CacheCurrentUserData();
                data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                if (user != null) {
                    StudIPHelper.current_user = user;
                    StudIPHelper.saveToFile(new File(getApplicationContext().getFilesDir(), "user.json"), StudIPHelper.current_user);
                }
                CacheCurrentUserPic pic = new CacheCurrentUserPic(false);
                pic.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, StudIPHelper.current_user.getAvatar_original());
                loggedIn = true;
                tryIntentChange();
            }
            super.onPostExecute(user);
        }
    }

    @SuppressWarnings({"StaticFieldLeak"})
    public class CacheCurrentUserPic extends AsyncTask<String, Void, Bitmap> {

        CacheCurrentUserPic(boolean innerCall) {
            if (!innerCall)
                loadingStatus.setText(getString(R.string.loading_user_pic));
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            if (!StudIPHelper.isNetworkAvailable(LoadActivity.this))
                return null;
            CustomAccessHttpResponse response = null;
            InputStream instream = null;
            try {
                response = StudIPHelper.api.getOAuthClient().get(url[0]);
                HttpEntity entity = response.getResponse().getEntity();
                BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
                instream = bufHttpEntity.getContent();
                return BitmapFactory.decodeStream(instream);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException | OAuthException e) {
                Intent intent = new Intent(((StudIPApp) getApplication()).getCurrentActivity(),
                        LoadActivity.class);
                startActivity(intent);
            } finally {
                try {
                    if (response != null)
                        response.close();
                    if (instream != null)
                        instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                CacheCurrentUserPic data = new CacheCurrentUserPic(true);
                data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, StudIPHelper.current_user.getAvatar_original());
            } else {
                StudIPHelper.updatePic(bitmap, (StudIPApp) getApplication());
            }
            super.onPostExecute(bitmap);
        }
    }

}

