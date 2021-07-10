package com.iskcon.bvks.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.IllegalSeekPositionException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Log;
import com.iskcon.bvks.base.BvksApplication;
import com.iskcon.bvks.firebase.Analytics;
import com.iskcon.bvks.listeners.OnPlayerEventListener;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.util.Constants;
import com.iskcon.bvks.util.PrefUtil;
import com.iskcon.bvks.util.urlextractor.VideoMeta;
import com.iskcon.bvks.util.urlextractor.YouTubeExtractor;
import com.iskcon.bvks.util.urlextractor.YtFile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.android.exoplayer2.Player.STATE_READY;
import static com.iskcon.bvks.base.BvksApplication.DEBUG;

public class PlayerManager implements PlayerNotificationManager.NotificationListener {
    private static final String TAG = PlayerManager.class.getSimpleName();


    private static PlayerManager sInstance;

    private Context mContext;
    private DataSource.Factory mDataSourceFactory;
    private SimpleExoPlayer mPlayer;
    private ConcatenatingMediaSource mConcatenatingMediaSource;
    private List<Lecture> mLectureQueue;
    private Lecture mCurrentLecture;
    private Lecture mPreviousLecture;
    private List<OnPlayerEventListener> mPlayerListeners;
    private Handler handler;
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private HashMap<String, String> mVideoLinks;

    private PlayerManager(@NonNull Context context) {
        mContext = context;
        mLectureQueue = new ArrayList<>();
        mPlayerListeners = new ArrayList<>();
        mDataSourceFactory = buildDataSourceFactory();
        initializePlayer();
        loadSavedLectures();
        handler = new Handler();
        mVideoLinks = new HashMap<>();
    }

    public static PlayerManager getInstance() {
        return sInstance;
    }

    public static void createInstance(@NonNull Context context) {
        sInstance = new PlayerManager(context);
    }

    private void initializePlayer() {
        mPlayer = new SimpleExoPlayer.Builder(/* context= */ mContext,
                ((BvksApplication) mContext).buildRenderersFactory()).build();
        mPlayer.addListener(new PlayerEventListener());
        mPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
    }

    /*private void loadSavedLectures() {
        mCurrentLecture = (Lecture) PrefUtil.getFromPrefs(mContext, PrefUtil.PLAYER_LECTURE, new Lecture());
        if (!TextUtils.isEmpty(mCurrentLecture.name)) {
            if (DEBUG) {
                Log.d(TAG, "Previous lectured saved was " + mCurrentLecture);
            }
            preparePlayer((ArrayList<Lecture>) PrefUtil.getFromPrefs(mContext, PrefUtil.PREF_KEY_LECTURE_LIST_KEY, new ArrayList<Lecture>()), false);
            try {
                mPlayer.seekTo(mLectureQueue.indexOf(mCurrentLecture), (long) PrefUtil.getFromPrefs(mContext, PrefUtil.KEY_POSITION, 0L));
                Log.e(TAG, "Cannot seek to given position " + (long) PrefUtil.getFromPrefs(mContext, PrefUtil.KEY_POSITION, 0L));
            } catch (IllegalSeekPositionException ex) {
                try {
                    mPlayer.seekTo(mLectureQueue.indexOf(mCurrentLecture), 0L);
                } catch (IllegalSeekPositionException e) {
                    Log.e(TAG, "Cannot seek to given default position");
                }
            }
        }
    }*/
    private void loadSavedLectures() {
        mCurrentLecture = (Lecture) PrefUtil.getFromPrefs(mContext, PrefUtil.PLAYER_LECTURE, new Lecture());
        if (!TextUtils.isEmpty(mCurrentLecture.name)) {
            if (DEBUG) {
                Log.d(TAG, "Previous lectured saved was " + mCurrentLecture);
            }
            preparePlayer((ArrayList<Lecture>) PrefUtil.getFromPrefs(mContext, PrefUtil.PREF_KEY_LECTURE_LIST_KEY, new ArrayList<Lecture>()), false);
            try {
                mPlayer.seekTo(mLectureQueue.indexOf(mCurrentLecture), getLastPlayedTime(mCurrentLecture));
                Log.e(TAG, "Cannot seek to given position " + getLastPlayedTime(mCurrentLecture));
            } catch (IllegalSeekPositionException ex) {
                try {
                    mPlayer.seekTo(mLectureQueue.indexOf(mCurrentLecture), 0L);
                } catch (IllegalSeekPositionException e) {
                    Log.e(TAG, "Cannot seek to given default position");
                }
            }
        }
    }

    private Long getLastPlayedTime(Lecture lecture) {
        if (lecture != null && lecture.information != null) {
            return lecture.information.lastPlayedPoint*1000;
        } else {
            return 0L;
        }
    }

    public void cleanUp() {
        if (DEBUG) {
            Log.d(TAG, "Cleanup");
        }
        mPlayer.release();
        mPlayer = null;
        mLectureQueue.clear();
        mCurrentLecture = null;
        mConcatenatingMediaSource = null;
    }

    public void addListener(OnPlayerEventListener listener) {
        mPlayerListeners.add(listener);
    }

    public void removeListener(OnPlayerEventListener listener) {
        mPlayerListeners.remove(listener);
    }

    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        }
        if (mPlayer.getPlaybackState() == Player.STATE_IDLE) {
            return false;
        }
        if (mPlayer.getPlaybackState() == Player.STATE_ENDED) {
            return false;
        }
        return mPlayer.getPlayWhenReady();
    }

    public void speed(float speed) {
        PlaybackParameters param = new PlaybackParameters(speed);
        mPlayer.setPlaybackParameters(param);
    }

    @Nullable
    public Lecture getCurrentLecture() {
        return mCurrentLecture;
    }

    @Nullable
    public Lecture getDeepLinkLecture(long lectureId) {
        return mCurrentLecture = mLectureQueue.stream().filter(a -> a.id == lectureId).collect(Collectors.toList()).get(0);
    }

    public long getCurrentLecturePosition() {
        if (mPlayer == null) {
            return 0L;
        }
        return Math.max(0L, mPlayer.getContentPosition());
    }

    /*public void play(@NonNull Lecture lecture) {
        Log.i(TAG, "Play with given lecture " + lecture);
        if (lecture.equals(mCurrentLecture)) {
            if (mPlayer.getPlaybackState() == Player.STATE_IDLE) {
                if (DEBUG) {
                    Log.d(TAG, "Retry playing same lecture");
                }
                mPlayer.retry();
            } else if (mPlayer.getPlaybackState() == Player.STATE_ENDED) {
                play(mLectureQueue.indexOf(lecture), C.TIME_UNSET);
            } else {
                play(mLectureQueue.indexOf(lecture), Math.max(0, mPlayer.getCurrentPosition()));
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "Play given lecture " + lecture + " from start position");
            }
            mCurrentLecture = lecture;
            play(mLectureQueue.indexOf(lecture), C.TIME_UNSET);
        }
    }*/
    public void play(@NonNull Lecture lecture) {
        Log.i(TAG, "Play with given lecture " + lecture);
        if (lecture.equals(mCurrentLecture)) {
            if (mPlayer.getPlaybackState() == Player.STATE_IDLE) {
                if (DEBUG) {
                    Log.d(TAG, "Retry playing same lecture");
                }
                mPlayer.retry();
            } else if (mPlayer.getPlaybackState() == Player.STATE_ENDED) {
                play(mLectureQueue.indexOf(lecture), getLastPlayedTime(lecture));
            } else {
                play(mLectureQueue.indexOf(lecture), getLastPlayedTime(lecture));
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "Play given lecture " + lecture + " from start position");
            }
            mCurrentLecture = lecture;
            play(mLectureQueue.indexOf(lecture), getLastPlayedTime(lecture));
        }
    }

    /**
     * Plays a specified queue item in the current player.
     *
     * @param itemIndex The index of the item to play.
     */
    private void play(int itemIndex, long positionMs) {
        if (DEBUG) {
            Log.d(TAG, "Play lecture at seek position " + positionMs);
        }

        try{
            mPlayer.seekTo(itemIndex, positionMs);
        }catch(IllegalSeekPositionException ex){
            mPlayer.seekTo(itemIndex, 0L);
        }
        mPlayer.setPlayWhenReady(true);
    }

    public void pause() {
/*        Log.i("TAG_OF_PLAYER_MANAGER :", String.valueOf(PlayerManager.getInstance().getPlayer().getCurrentPosition() / 1000));
        Log.i("TAG_OF_PLAYER_MANAGER_2 :", String.valueOf(Math.max(0, mPlayer.getCurrentPosition())));
        Log.i("TAG_OF_PLAYER_MANAGER_3 :", String.valueOf(getCurrentLecturePosition()));
        Log.i("TAG_OF_PLAYER_MANAGER_4 :", String.valueOf(mPlayer.getDuration() / 1000));
        Log.i("TAG_OF_PLAYER_MANAGER_5 :", String.valueOf(mCurrentLecture.mediaLength));*/
        mPlayer.setPlayWhenReady(false);
    }

    private void updateProgress() {
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
        long exactPosition = position / 1000;
        Log.i(TAG, String.valueOf(exactPosition));
        Log.i(TAG, String.valueOf(mPlayer.getDuration() / 1000));
        if (getCurrentLecture() != null && exactPosition > 0) {
            LectureManager.getInstance().updateCurrentPlayPoint(getCurrentLecture(), exactPosition);
        }
        StatsManager.getInstance().updateUserListenDetail(10, getCurrentLecture(),false);
        handler.postDelayed(updateProgressAction, 10000);

    }


    public void preparePlayer(@NonNull List<Lecture> lectureList, boolean saveToPrefs) {
        if (lectureList.equals(mLectureQueue)) {
            Log.i(TAG, "Lectures already added");
            return;
        }

        Log.i(TAG, "Adding lectures of size " + lectureList.size());
        mLectureQueue.clear();
        mLectureQueue.addAll(lectureList);

        // Stop and reset the player
        mPlayer.stop(true);

        List<MediaSource> mediaSourceList = buildMediaSource(lectureList);
        if (mConcatenatingMediaSource == null) {
            mConcatenatingMediaSource = new ConcatenatingMediaSource();
            mConcatenatingMediaSource.addMediaSources(mediaSourceList);
        } else {
            mConcatenatingMediaSource.clear();
            mConcatenatingMediaSource.addMediaSources(mediaSourceList);
        }

        mPlayer.prepare(mConcatenatingMediaSource, false, false);

        if (saveToPrefs) {
            PrefUtil.saveToPrefs(mContext, PrefUtil.PREF_KEY_LECTURE_LIST_KEY, lectureList);
        }
    }

    public boolean compareLectureList(@NonNull List<Lecture> lectureList) {
        return lectureList.equals(mLectureQueue);
    }

    public void moveMediaSource(int fromPos, int toPos) {
        if (mConcatenatingMediaSource == null) {
            return;
        }

        mConcatenatingMediaSource.moveMediaSource(fromPos, toPos);
        if (fromPos < toPos) {
            for (int i = fromPos; i < toPos; i++) {
                Collections.swap(mLectureQueue, i, i + 1);
            }
        } else {
            for (int i = fromPos; i > toPos; i--) {
                Collections.swap(mLectureQueue, i, i - 1);
            }
        }
    }

    public void removeMediaSource(int index) {
        if (mConcatenatingMediaSource == null) {
            return;
        }
        mConcatenatingMediaSource.removeMediaSource(index);
        mLectureQueue.remove(index);
    }

    public SimpleExoPlayer getPlayer() {
        return mPlayer;
    }

    private List<MediaSource> buildMediaSource(List<Lecture> lectureList) {
        List<MediaSource> mediaSources = new ArrayList<>(lectureList.size());
        for (Lecture lecture : lectureList) {
            Uri parameters = Uri.parse(lecture.mediaUrl);
            DownloadRequest downloadRequest =
                    ((BvksApplication) mContext)
                            .getDownloadTracker()
                            .getDownloadRequest(parameters);

            mediaSources.add(downloadRequest != null ? DownloadHelper.createMediaSource(downloadRequest, mDataSourceFactory) :
                    new ProgressiveMediaSource.Factory(mDataSourceFactory)
                            .setDrmSessionManager(DrmSessionManager.getDummyDrmSessionManager())
                            .createMediaSource(parameters));
            /*if (lecture.videoLink!=null&&!lecture.videoLink.isEmpty()){
                createHLSUrl(lecture);
            }*/
        }
        return mediaSources;
    }

    private String getYoutubeVideoId(@NotNull String videoLink) {
//        Log.i(TAG, "videoLink---->" + videoLink);
        String lastBit;
        if (videoLink.contains("watch")) {
            lastBit = videoLink.substring(videoLink.lastIndexOf('='));
            lastBit = lastBit.replace("=", "");
        } else {
            lastBit = videoLink.substring(videoLink.lastIndexOf('/'));
            lastBit = lastBit.replace("/", "");
        }
//        Log.i(TAG, "videoID---->" + lastBit);
        return lastBit;
    }

    private void createHLSUrl(Lecture lecture) {
        mVideoLinks = (HashMap) PrefUtil.getFromPrefs(mContext, PrefUtil.VIDEO_LINKS_PREF, new HashMap<>());
        @SuppressLint("StaticFieldLeak") YouTubeExtractor mExtractor = new YouTubeExtractor(mContext) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                if (sparseArray != null) {
                    String urlHLS = sparseArray.get(22).getUrl();
//                    Log.i(TAG, "URL HLS-" + urlHLS);
//                    Log.i(TAG, "URL HLS-" + urlHLS);
//                    Log.i(TAG, "VIDEO ID-" + videoMeta.getVideoId());
                    mVideoLinks.put(videoMeta.getVideoId(), urlHLS);
                    PrefUtil.saveToPrefs(mContext, PrefUtil.VIDEO_LINKS_PREF, mVideoLinks);

                } else {
//                    Utils.getInstance().showToast(mContext,"ErrorX02-SAN");
//                    Log.i(TAG, "sparseArray Null");
                }
            }
        };
        String youTubeLink = Constants.YOU_TUBE_BASE_URL + "/watch?v=" + getYoutubeVideoId(lecture.videoLink);
        mExtractor.extract(youTubeLink, true, true);
    }

    private void updateCurrentLecture() {
        mCurrentLecture = mLectureQueue.get(mPlayer.getCurrentWindowIndex());
        if (mPreviousLecture == null) {
            mPreviousLecture = mCurrentLecture;
        } else {
            if (!mPreviousLecture.equals(mCurrentLecture)) {
                //UPDATE(DIFFERENT LECTURE)
                PopularTopLectureManager.getInstance().updateTopLectureDetail(mCurrentLecture);
                mPreviousLecture = mCurrentLecture;
            }
        }

        Analytics.LectureAnalytics.lecturePlayed(mCurrentLecture);
        if (DEBUG) {
            Log.d(TAG, "Update current lecture to " + mCurrentLecture);
        }
    }

    /**
     * Returns a new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory() {
        return ((BvksApplication) mContext).buildDataSourceFactory();
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, @Player.TimelineChangeReason int reason) {
            if (DEBUG) {
                Log.i(TAG, "onTimelineChanged()");
            }
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
            if (DEBUG) {
                Log.i(TAG, "onPlayerStateChanged()");
            }
            if (playbackState == STATE_READY) {

                updateCurrentLecture();
                if (isPlaying()) {
                    Log.i(TAG, "callback added");
                    handler.removeCallbacks(updateProgressAction);
                    updateProgress();
                } else {
                    Log.i(TAG, "callback removed");
                    handler.removeCallbacks(updateProgressAction);
                }

            }
            if (playbackState == Player.STATE_ENDED) {
                Log.i(TAG, "callback removed");
                handler.removeCallbacks(updateProgressAction);
            }
            for (OnPlayerEventListener listener : mPlayerListeners) {
                listener.playerStateChanged();
            }

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Stop and reset the player
            for (OnPlayerEventListener listener : mPlayerListeners) {
                listener.playerError();
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {

            if (DEBUG) {
                Log.i(TAG, "onPositionDiscontinuity()-----"+reason);
            }
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                LectureManager.getInstance().markUnMarkComplete(getCurrentLecture(), true);
                updateCurrentLecture();
                for (OnPlayerEventListener listener : mPlayerListeners) {
                    listener.playerStateChanged();
                }
            }
        }
    }
}
