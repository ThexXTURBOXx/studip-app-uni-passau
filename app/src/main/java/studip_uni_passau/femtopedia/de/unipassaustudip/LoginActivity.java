package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.femtopedia.studip.StudIPAPI;
import de.femtopedia.studip.json.User;
import de.femtopedia.studip.shib.ShibHttpResponse;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView authenticationStatus;
    private List<Cookie> cookies;
    private boolean loggedIn = false, checkedForUpdates = false;

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);

        AppUpdater updater = new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("http://femtopedia.de/studip/version.json")
                .setOnFinish((v, i) -> {
                    if (i == 0) {
                        checkedForUpdates = true;
                        tryIntentChange();
                    } else {
                        finish();
                    }
                });
        updater.start();

        cookies = new ArrayList<>();
        if (PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean("cookie_saving", true)) {
            //Load cookies from file
            File dir = new File(getApplicationContext().getFilesDir(), "/cookies");
            if (!dir.exists())
                dir.mkdirs();
            for (File f : dir.listFiles()) {
                cookies.add(StudIPHelper.loadFromFile(f, BasicClientCookie.class));
            }
        } else {
            File dir = new File(getApplicationContext().getFilesDir(), "/cookies");
            dir.deleteOnExit();
        }

        // Set up the login form.
        mUsernameView = findViewById(R.id.username);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("credentials", MODE_PRIVATE);
        mUsernameView.setText(pref.getString("username", ""));

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin(false);
                return true;
            }
            return false;
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener((view) -> attemptLogin(false));

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        authenticationStatus = findViewById(R.id.login_progress_status);

        attemptLogin(true);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(boolean ignoreEmpty) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!ignoreEmpty) {
            // Check for a valid password, if the user entered one.
            if (TextUtils.isEmpty(password)) {
                mPasswordView.setError(getString(R.string.error_field_required));
                focusView = mPasswordView;
                cancel = true;
            }

            // Check for a valid username.
            if (TextUtils.isEmpty(username)) {
                mUsernameView.setError(getString(R.string.error_field_required));
                focusView = mUsernameView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, cookies);
            mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ignoreEmpty);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        authenticationStatus.setVisibility(show ? View.VISIBLE : View.GONE);
        authenticationStatus.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
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
            Intent intent = new Intent(LoginActivity.this, ScheduleActivity.class);
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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressWarnings({"StaticFieldLeak"})
    public class UserLoginTask extends AsyncTask<Boolean, Void, Integer> {

        private final String mUsername;
        private final String mPassword;
        private final List<Cookie> mCookies;

        UserLoginTask(String username, String password, List<Cookie> cookies) {
            mUsername = username;
            mPassword = password;
            mCookies = cookies;
            authenticationStatus.setText(getString(R.string.authenticating));
        }

        @Override
        protected Integer doInBackground(Boolean... params) {
            if (!StudIPHelper.isNetworkAvailable(LoginActivity.this)) {
                StudIPHelper.api = new StudIPAPI(mCookies);
                return 4;
            }
            try {
                StudIPHelper.api = new StudIPAPI(mCookies);
                if (!params[0])
                    StudIPHelper.api.authenticate(mUsername, mPassword);
                if (!StudIPHelper.api.getShibbolethClient().isSessionValid()) {
                    return 3;
                }
                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("cookie_saving", true)) {
                    int i = 0;
                    for (Cookie c : StudIPHelper.api.getShibbolethClient().getCookieStore().getCookies()) {
                        StudIPHelper.saveToFile(new File(getApplicationContext().getFilesDir(), "cookies/cookie_" + (i++) + ".ser"), c);
                    }
                }
            } catch (IllegalAccessException e) {
                return 1;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException | IllegalArgumentException e) {
                return 2;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mAuthTask = null;

            switch (success) {
                case 0:
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("credentials", MODE_PRIVATE);
                    SharedPreferences.Editor e = pref.edit();
                    e.putString("username", mUsername);
                    e.apply();
                    loggedIn = true;
                    CacheCurrentUserData data = new CacheCurrentUserData();
                    data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case 1:
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
                case 2:
                    mPasswordView.setError(getString(R.string.error_random_error));
                    mPasswordView.requestFocus();
                    break;
                case 3:
                    mPasswordView.setError(getString(R.string.error_login_again));
                    mPasswordView.requestFocus();
                    break;
                case 4:
                    loggedIn = true;
                    CacheCurrentUserData data1 = new CacheCurrentUserData();
                    data1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                default:
                    mPasswordView.setError(getString(R.string.error_sum_sh));
                    mPasswordView.requestFocus();
                    break;
            }

            if (success != 0 && success != 4)
                showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }

    @SuppressWarnings({"StaticFieldLeak"})
    public class CacheCurrentUserData extends AsyncTask<Void, Void, User> {

        CacheCurrentUserData() {
            authenticationStatus.setText(getString(R.string.loading_user_data));
            StudIPHelper.current_user = StudIPHelper.loadFromFile(new File(getApplicationContext().getFilesDir(), "user.json"), User.class);
        }

        @Override
        protected User doInBackground(Void... voids) {
            if (!StudIPHelper.isNetworkAvailable(LoginActivity.this))
                return null;
            try {
                return StudIPHelper.api.getCurrentUserData();
            } catch (IOException | IllegalAccessException e) {
                e.printStackTrace();
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
                CacheCurrentUserPic pic = new CacheCurrentUserPic();
                pic.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, StudIPHelper.current_user.getAvatar_original());
                tryIntentChange();
            }
            super.onPostExecute(user);
        }
    }

    @SuppressWarnings({"StaticFieldLeak"})
    public class CacheCurrentUserPic extends AsyncTask<String, Void, Bitmap> {

        CacheCurrentUserPic() {
            this(false);
        }

        CacheCurrentUserPic(boolean innerCall) {
            if (!innerCall)
                authenticationStatus.setText(getString(R.string.loading_user_pic));
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            ShibHttpResponse response = null;
            InputStream instream = null;
            try {
                response = StudIPHelper.api.getShibbolethClient().getIfValid(url[0]);
                HttpEntity entity = response.getResponse().getEntity();
                BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
                instream = bufHttpEntity.getContent();
                return BitmapFactory.decodeStream(instream);
            } catch (IOException | IllegalAccessException e) {
                e.printStackTrace();
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

