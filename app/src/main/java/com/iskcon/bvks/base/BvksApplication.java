package com.iskcon.bvks.base;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.offline.ActionFileUpgradeUtil;
import com.google.android.exoplayer2.offline.DefaultDownloadIndex;
import com.google.android.exoplayer2.offline.DefaultDownloaderFactory;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.iskcon.bvks.R;
import com.iskcon.bvks.firebase.Analytics;
import com.iskcon.bvks.manager.LectureManager;
import com.iskcon.bvks.manager.PlayerManager;
import com.iskcon.bvks.manager.PopularTopLectureManager;
import com.iskcon.bvks.manager.SettingsManager;
import com.iskcon.bvks.manager.StatsManager;
import com.iskcon.bvks.manager.restapi.APICallMethods;
import com.iskcon.bvks.manager.restapi.APIHandler;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.util.DownloadTracker;
import com.iskcon.bvks.util.PrefUtil;

import java.io.File;
import java.io.IOException;

public class BvksApplication extends Application implements Application.ActivityLifecycleCallbacks {
    public static final boolean DEBUG = true;
    public static final String CHANNEL_ID = "bvks_108";
    public static final String CHANNEL_ID_VIDEO = "bvks_108_video";
    private static final String TAG = "BvksApplication";
    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    protected String userAgent;

    private DatabaseProvider databaseProvider;
    private File downloadDirectory;
    private Cache downloadCache;
    private DownloadManager downloadManager;
    private DownloadTracker downloadTracker;

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DataSource.Factory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSource.Factory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);
        Analytics.init(getApplicationContext());
        // Firebase default configurations
        FirebaseFirestore.setLoggingEnabled(true);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        FirebaseFirestore.getInstance().enableNetwork();


        userAgent = Util.getUserAgent(getApplicationContext(), "Bvks");
        LectureManager.createInstance(getApplicationContext());
        PopularTopLectureManager.createInstance(getApplicationContext());
        StatsManager.createInstance(getApplicationContext());


        PlayerManager.createInstance(getApplicationContext());

        SettingsManager.createInstance(getApplicationContext());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_description));
            getSystemService(NotificationManager.class).createNotificationChannel(channel);

            NotificationChannel channelVideo = new NotificationChannel(CHANNEL_ID_VIDEO,
                    getString(R.string.channel_name_video), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_description));
            getSystemService(NotificationManager.class).createNotificationChannel(channelVideo);
        }

    }

    @Override
    public void onTerminate() {
        Lecture currentLecture = PlayerManager.getInstance().getCurrentLecture();
        if (currentLecture != null) {
            PrefUtil.saveToPrefs(getApplicationContext(), PrefUtil.PLAYER_LECTURE, PlayerManager.getInstance().getCurrentLecture());
            PrefUtil.saveToPrefs(getApplicationContext(), PrefUtil.KEY_POSITION, PlayerManager.getInstance().getCurrentLecturePosition());
        }
        PlayerManager.getInstance().cleanUp();
        super.onTerminate();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Lecture currentLecture = PlayerManager.getInstance().getCurrentLecture();
        if (currentLecture != null) {
            PrefUtil.saveToPrefs(activity, PrefUtil.PLAYER_LECTURE, PlayerManager.getInstance().getCurrentLecture());
            PrefUtil.saveToPrefs(activity, PrefUtil.KEY_POSITION, PlayerManager.getInstance().getCurrentLecturePosition());
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Lecture currentLecture = PlayerManager.getInstance().getCurrentLecture();
        if (currentLecture != null) {
            PrefUtil.saveToPrefs(activity, PrefUtil.PLAYER_LECTURE, PlayerManager.getInstance().getCurrentLecture());
            PrefUtil.saveToPrefs(activity, PrefUtil.KEY_POSITION, PlayerManager.getInstance().getCurrentLecturePosition());
        }
    }

    /**
     * Returns a {@link DataSource.Factory}.
     */
    public DataSource.Factory buildDataSourceFactory() {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(this, buildHttpDataSourceFactory());
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    /**
     * Returns a {@link HttpDataSource.Factory}.
     */
    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }

    public RenderersFactory buildRenderersFactory() {
        return new DefaultRenderersFactory(/* context= */ this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
    }

    public DownloadManager getDownloadManager() {
        initDownloadManager();
        return downloadManager;
    }

    public DownloadTracker getDownloadTracker() {
        initDownloadManager();
        return downloadTracker;
    }

    protected synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache =
                    new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor(), getDatabaseProvider());
        }
        return downloadCache;
    }

    private synchronized void initDownloadManager() {
        if (downloadManager == null) {
            DefaultDownloadIndex downloadIndex = new DefaultDownloadIndex(getDatabaseProvider());
            upgradeActionFile(
                    DOWNLOAD_ACTION_FILE, downloadIndex, /* addNewDownloadsAsCompleted= */ false);
            upgradeActionFile(
                    DOWNLOAD_TRACKER_ACTION_FILE, downloadIndex, /* addNewDownloadsAsCompleted= */ true);
            DownloaderConstructorHelper downloaderConstructorHelper =
                    new DownloaderConstructorHelper(getDownloadCache(), buildHttpDataSourceFactory());
            downloadManager =
                    new DownloadManager(
                            this, downloadIndex, new DefaultDownloaderFactory(downloaderConstructorHelper));
            downloadTracker =
                    new DownloadTracker(/* context= */ this, buildDataSourceFactory(), downloadManager);
        }
    }

    private void upgradeActionFile(
            String fileName, DefaultDownloadIndex downloadIndex, boolean addNewDownloadsAsCompleted) {
        try {
            ActionFileUpgradeUtil.upgradeAndDelete(
                    new File(getDownloadDirectory(), fileName),
                    /* downloadIdProvider= */ null,
                    downloadIndex,
                    /* deleteOnFailure= */ true,
                    addNewDownloadsAsCompleted);
        } catch (IOException e) {
            Log.e(TAG, "Failed to upgrade action file: " + fileName, e);
        }
    }

    private DatabaseProvider getDatabaseProvider() {
        if (databaseProvider == null) {
            databaseProvider = new ExoDatabaseProvider(this);
        }
        return databaseProvider;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = getFilesDir();
            }
        }
        return downloadDirectory;
    }

    public APICallMethods getHandler() {
        return APIHandler.Companion.getInstance().getHandler();
    }

}
