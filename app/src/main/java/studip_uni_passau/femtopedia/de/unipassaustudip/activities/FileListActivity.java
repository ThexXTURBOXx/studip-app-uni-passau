package studip_uni_passau.femtopedia.de.unipassaustudip.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import de.femtopedia.studip.json.Course;
import de.femtopedia.studip.json.Folder;
import de.femtopedia.studip.json.SubFile;
import de.femtopedia.studip.json.SubFolder;
import de.femtopedia.studip.shib.CustomAccessHttpResponse;
import de.hdodenhof.circleimageview.CircleImageView;
import oauth.signpost.exception.OAuthException;
import studip_uni_passau.femtopedia.de.unipassaustudip.R;
import studip_uni_passau.femtopedia.de.unipassaustudip.StudIPApp;
import studip_uni_passau.femtopedia.de.unipassaustudip.databinding.FilelistBinding;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.AnimatingRefreshButtonManager;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.FileAdapter;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.StudIPHelper;
import studip_uni_passau.femtopedia.de.unipassaustudip.util.SubContent;

public class FileListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StudIPHelper.ProfilePicHolder,
        StudIPHelper.NavigationDrawerActivity, AdapterView.OnItemClickListener {

    private static final int EXT_STORAGE_REQUEST_CODE = 0;
    private ProgressDialog mProgressDialog;
    private FileAdapter listAdapter;
    private List<SubContent> listDataHeader;
    private NavigationView navigationView;
    private SwipeRefreshLayout swipeRefresher;
    private AnimatingRefreshButtonManager refreshManager;
    private DrawerLayout drawer;
    private Deque<SubContent> currentContentCache = new ArrayDeque<>();
    private Deque<FileListCache> fileListCache = new ArrayDeque<>();
    private SubFile fileParamCache = null;

    private static final class FileListCache {

        private final List<SubContent> content;

        private FileListCache(List<SubContent> content) {
            this.content = content;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (StudIPHelper.getCurrentUser() == null) {
            Intent intent = new Intent(FileListActivity.this, LoadActivity.class);
            startActivity(intent);
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.progress));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        FilelistBinding binding = FilelistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ((StudIPApp) getApplicationContext()).setCurrentActivity(this);
        StudIPHelper.target = "filelist";

        swipeRefresher = binding.appBar.content.swiperefreshFilelist;
        swipeRefresher.setOnRefreshListener(() -> updateData(currentContentCache.peek(), true));

        ListView listView = binding.appBar.content.subcontent;
        prepareListData();
        listAdapter = new FileAdapter(this, listDataHeader);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        updateDataFirst();

        navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        setActive();

        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nameofcurrentuser)).setText(StudIPHelper.getCurrentUser().getName().getFormatted());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.usernameel)).setText(StudIPHelper.getCurrentUser().getUsername());

        if (StudIPHelper.getProfilePic() != null) {
            setProfilePic();
        }

        drawer = binding.drawerLayout;
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer, (Toolbar) actionbar.getCustomView(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(drawerToggle);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
            drawerToggle.syncState();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.button_okay, (dialog, id) -> {
        }).setTitle(R.string.beta_title)
                .setMessage(R.string.beta_desc)
                .create()
                .show();
    }

    @Override
    public void setActive() {
        if (navigationView != null)
            navigationView.getMenu().findItem(R.id.nav_filelist).setChecked(true);
    }

    public void setProfilePic() {
        ((CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView)).setImageBitmap(StudIPHelper.getProfilePic());
    }

    private void updateDataFirst() {
        if (StudIPHelper.isNetworkAvailable(this)) {
            startUpdateAnimation();
            CacheOverviewFolder data = new CacheOverviewFolder(false);
            data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateData(SubContent content, boolean refresh) {
        if (StudIPHelper.isNetworkAvailable(this)) {
            if (content == null) {
                startUpdateAnimation();
                CacheOverviewFolder data = new CacheOverviewFolder(refresh);
                data.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                switch (content.getType()) {
                    case FILE:
                        SubFile file = content.getFile();
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(this,
                                        Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                new AlertDialog.Builder(this)
                                        .setTitle(R.string.app_name)
                                        .setMessage(R.string.need_permission)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .setOnDismissListener(dialogInterface -> {
                                            fileParamCache = file;
                                            ActivityCompat.requestPermissions(FileListActivity.this,
                                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                            Manifest.permission.READ_EXTERNAL_STORAGE},
                                                    EXT_STORAGE_REQUEST_CODE);
                                        })
                                        .show();
                            } else {
                                fileParamCache = file;
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_EXTERNAL_STORAGE},
                                        EXT_STORAGE_REQUEST_CODE);
                            }
                        } else {
                            downloadFile(file);
                        }
                        break;
                    case FOLDER:
                        startUpdateAnimation();
                        CacheFolder data1 = new CacheFolder(refresh);
                        data1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                content.getFolder().getId());
                        break;
                    case COURSE:
                        startUpdateAnimation();
                        CacheFolder data2 = new CacheFolder(refresh);
                        data2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                content.getCourse().getCourse_id(), null);
                        break;
                }
            }
        } else {
            stopUpdateAnimation();
        }
    }

    public void downloadFile(SubFile file) {
        final DownloadTask downloadTask = new DownloadTask();
        downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
        mProgressDialog.setOnCancelListener(dialog -> downloadTask.cancel(true));
    }

    public void openFile(File file, String mime) {
        try {
            Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".studipfileprovider", file);
            Intent fileIntent = new Intent(Intent.ACTION_VIEW);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.setDataAndType(fileUri, mime);
            startActivity(fileIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.cant_open_file_type, Toast.LENGTH_LONG).show();
        }
    }

    private void startUpdateAnimation() {
        if (swipeRefresher != null)
            swipeRefresher.setRefreshing(true);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (refreshManager != null)
                refreshManager.onRefreshBeginning();
        });
    }

    private void stopUpdateAnimation() {
        if (swipeRefresher != null)
            swipeRefresher.setRefreshing(false);
        if (refreshManager != null)
            refreshManager.onRefreshComplete();
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
    }

    private void clearListItems() {
        listDataHeader.clear();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!fileListCache.isEmpty()) {
            currentContentCache.pop();
            setToView(fileListCache.pop());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_refresh, menu);
        refreshManager = new AnimatingRefreshButtonManager(this, menu.findItem(R.id.action_refresh_bar));
        if (swipeRefresher != null && swipeRefresher.isRefreshing())
            refreshManager.onRefreshBeginning();
        else
            refreshManager.onRefreshComplete();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!drawer.isDrawerOpen(GravityCompat.START))
                drawer.openDrawer(GravityCompat.START);
            else
                drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.action_refresh_bar) {
            this.updateData(currentContentCache.peek(), true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        StudIPHelper.updateNavigation(item.getItemId(), R.id.nav_filelist, this);
        return true;
    }

    public void setToView(FileListCache cache) {
        clearListItems();
        listDataHeader.addAll(cache.content);
        listAdapter.notifyDataSetChanged();
    }

    public void setToView(Folder folder, boolean refresh) {
        stopUpdateAnimation();
        if (!refresh && !listDataHeader.isEmpty()) {
            fileListCache.push(new FileListCache(new ArrayList<>(listDataHeader)));
        }
        clearListItems();
        Iterator<SubFolder> folders = folder.getSubfolders().iterator();
        while (folders.hasNext()) {
            SubFolder next = folders.next();
            if (next == null || next.getName() == null) {
                folders.remove();
            }
        }
        Collections.sort(folder.getSubfolders(), (f1, f2) -> f1.getName().compareTo(f2.getName()));
        Iterator<SubFile> files = folder.getFile_refs().iterator();
        while (files.hasNext()) {
            SubFile next = files.next();
            if (next == null || next.getName() == null) {
                files.remove();
            }
        }
        Collections.sort(folder.getFile_refs(), (f1, f2) -> Long.compare(f1.getChdate(), f2.getChdate()));
        for (SubFolder f : folder.getSubfolders()) {
            listDataHeader.add(new SubContent(f));
        }
        for (SubFile f : folder.getFile_refs()) {
            listDataHeader.add(new SubContent(f));
        }
        listAdapter.notifyDataSetChanged();
    }

    public void setToView(List<Course> courses, boolean refresh) {
        stopUpdateAnimation();
        if (!refresh && !listDataHeader.isEmpty()) {
            fileListCache.push(new FileListCache(new ArrayList<>(listDataHeader)));
        }
        clearListItems();
        Collections.sort(courses, (c1, c2) -> c1.getTitle().compareTo(c2.getTitle()));
        for (Course c : courses) {
            listDataHeader.add(new SubContent(c));
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final SubContent sc = listDataHeader.get(position);
        if (sc.getType() != SubContent.SubType.FILE) {
            currentContentCache.push(sc);
        }
        updateData(sc, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == EXT_STORAGE_REQUEST_CODE && grantResults.length > 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED && fileParamCache != null) {
            downloadFile(fileParamCache);
            fileParamCache = null;
        }
    }

    @SuppressWarnings("StaticFieldLeak")
    public class CacheOverviewFolder extends AsyncTask<Void, Void, List<Course>> {

        private final boolean refresh;

        private CacheOverviewFolder(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected List<Course> doInBackground(Void... url) {
            List<Course> courses = new ArrayList<>();
            try {
                courses.addAll(StudIPHelper.getApi().getCourses(StudIPHelper.getCurrentUser().getUser_id()).getCollection().values());
            } catch (IllegalAccessException | OAuthException e) {
                Intent intent = new Intent(FileListActivity.this, LoadActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return courses;
        }

        @Override
        protected void onPostExecute(List<Course> courses) {
            setToView(courses, refresh);
            super.onPostExecute(courses);
        }
    }

    @SuppressWarnings("StaticFieldLeak")
    public class CacheFolder extends AsyncTask<String, Void, Folder> {

        private final boolean refresh;

        private CacheFolder(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected Folder doInBackground(String... params) {
            Folder folder = null;
            try {
                if (params.length > 1) {
                    folder = StudIPHelper.getApi().getCourseTopFolder(params[0]);
                } else {
                    folder = StudIPHelper.getApi().getFolder(params[0]);
                }
            } catch (IllegalAccessException | OAuthException e) {
                Intent intent = new Intent(FileListActivity.this, LoadActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return folder;
        }

        @Override
        protected void onPostExecute(Folder folder) {
            setToView(folder, refresh);
            super.onPostExecute(folder);
        }

    }

    @SuppressWarnings("StaticFieldLeak")
    public class DownloadTask extends AsyncTask<SubFile, Long, DownloadTask.Output> {

        private class Output {
            private final File file;
            private final String mime;

            private Output(File file, String mime) {
                this.file = file;
                this.mime = mime;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected Output doInBackground(SubFile... params) {
            File file = null;
            CustomAccessHttpResponse resp = null;
            InputStream input = null;
            OutputStream output = null;
            try {
                File downloads = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "StudiPassau");
                if (!downloads.exists() || downloads.isFile()) {
                    downloads.mkdirs();
                }
                file = new File(downloads, params[0].getName());
                resp = StudIPHelper.getApi().get("file/" + params[0].getId() + "/download");
                long fileLength = params[0].getSize();
                input = resp.getResponse().body().byteStream();
                output = new FileOutputStream(file);
                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0) {
                        publishProgress(total * 100 / fileLength);
                    }
                    output.write(data, 0, count);
                }
            } catch (IllegalAccessException | OAuthException e) {
                Intent intent = new Intent(FileListActivity.this, LoadActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (resp != null)
                    resp.close();
            }
            return new Output(file, params[0].getMime_type());
        }

        @Override
        protected void onProgressUpdate(Long... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress((int) (long) progress[0]);
        }

        @Override
        protected void onPostExecute(Output file) {
            mProgressDialog.dismiss();
            openFile(file.file, file.mime);
            super.onPostExecute(file);
        }

    }

}
