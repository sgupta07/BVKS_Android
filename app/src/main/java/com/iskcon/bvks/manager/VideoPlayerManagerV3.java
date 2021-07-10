package com.iskcon.bvks.manager;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.iskcon.bvks.firebase.Analytics;
import com.iskcon.bvks.listeners.OnVideoPlayerEventListener;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.util.PrefUtil;
import com.iskcon.bvks.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.android.exoplayer2.Player.STATE_READY;
import static com.iskcon.bvks.base.BvksApplication.DEBUG;
@Deprecated
public class VideoPlayerManagerV3 {
    /**
     * declare some usable variable
     */
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String TAG = VideoPlayerManagerV3.class.getSimpleName();
    private static VideoPlayerManagerV3 mInstance = null;
    private final String mYouTubeBaseUrl = "https://www.youtube.com";
    PlayerView mPlayerView;
    DefaultDataSourceFactory dataSourceFactory;
    String uriString = "";
    List<Lecture> mPlayList = null;
    Integer playlistIndex = 0;
    Context mContext;
    private SimpleExoPlayer mPlayer;
    private Handler handler;
    private String mYouTubeVideoId = "";
    private List<Lecture> mLectureQueue;
    private Lecture mCurrentLecture;
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private ConcatenatingMediaSource mConcatenatingMediaSource;
    private List<OnVideoPlayerEventListener> mPlayerListeners;
    private Lecture mPreviousLecture;

    private VideoPlayerManagerV3(Context context) {
        mContext = context;
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        mPlayerView = new PlayerView(context);
        mPlayerView.setUseController(true);
        mPlayerView.requestFocus();
        mPlayerView.setPlayer(mPlayer);
        Uri mp4VideoUri = Uri.parse(uriString);
        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "androidwave"), BANDWIDTH_METER);
        final MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mp4VideoUri);
        mPlayer.prepare(videoSource);
        mPlayer.addListener(new PlayerEventListener());

        mLectureQueue = new ArrayList<>();
        mPlayerListeners = new ArrayList<>();
        handler = new Handler();
    }

    public static VideoPlayerManagerV3 getInstance() {
        return mInstance;
    }

    public static VideoPlayerManagerV3 getSharedInstance(Context context) {
        if (mInstance == null) {
            Log.i(TAG, ".*.*.*.*.*.*.*.NEW INSTANCE.*.*.*.*.*.*.*.*.");
            mInstance = new VideoPlayerManagerV3(context);
        }
        return mInstance;
    }
    private String getYoutubeVideoId(@NotNull String videoLink) {
        Log.i(TAG, "videoLink---->" + videoLink);
        String lastBit;
        if (videoLink.contains("&")){
            String result = videoLink.split("&")[0];
            if (result.contains("watch")) {
                lastBit = result.substring(result.lastIndexOf('='));
                lastBit = lastBit.replace("=", "");
            } else {
                lastBit = result.substring(result.lastIndexOf('/'));
                lastBit = lastBit.replace("/", "");
            }
        }else {
            if (videoLink.contains("watch")) {
                lastBit = videoLink.substring(videoLink.lastIndexOf('='));
                lastBit = lastBit.replace("=", "");
            } else {
                lastBit = videoLink.substring(videoLink.lastIndexOf('/'));
                lastBit = lastBit.replace("/", "");
            }
        }
        Log.i(TAG, "videoID---->" + lastBit);
        return lastBit;
    }

    public PlayerView getPlayerView() {
        return mPlayerView;
    }

    public void destroyPlayer() {
        mPlayer.stop();
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

    public void addListener(OnVideoPlayerEventListener listener) {
        mPlayerListeners.add(listener);
    }

    public void removeListener(OnVideoPlayerEventListener listener) {
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

    public long getCurrentLecturePosition() {
        if (mPlayer == null) {
            return 0L;
        }
        return Math.max(0L, mPlayer.getContentPosition());
    }

    public SimpleExoPlayer getPlayer() {
        return mPlayer;
    }

    private void updateProgress() {
        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
        long exactPosition = position / 1000;
        com.google.android.exoplayer2.util.Log.i("LOCATION", String.valueOf(exactPosition));
        com.google.android.exoplayer2.util.Log.i("LOCATION_DURATION", String.valueOf(mPlayer.getDuration() / 1000));
        if (getCurrentLecture() != null && exactPosition > 0) {
            LectureManager.getInstance().updateCurrentPlayPoint(getCurrentLecture(), exactPosition);
        }
        StatsManager.getInstance().updateUserListenDetail(10, getCurrentLecture(),true);
        handler.postDelayed(updateProgressAction, 10000);

    }

    public void pause() {
        mPlayer.setPlayWhenReady(false);
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

    public void play(@NonNull Lecture lecture) {
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
    }

    private void play(int itemIndex, long positionMs) {
        if (DEBUG) {
            Log.d(TAG, "Play lecture at index position " + itemIndex);
        }

        mPlayer.seekTo(itemIndex, positionMs);
        mPlayer.setPlayWhenReady(true);
    }

    public void preparePlayer(@NonNull List<Lecture> lectureList, boolean saveToPrefs) {
        /*if (lectureList.equals(mLectureQueue)) {
           Log.i(TAG, "Lectures already added");
            return;
        }*/

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

    private List<MediaSource> buildMediaSource(List<Lecture> lectureList) {
        List<MediaSource> mediaSources = new ArrayList<>(lectureList.size());
        HashMap<String, String> videoLinks = (HashMap) PrefUtil.getFromPrefs(mContext, PrefUtil.VIDEO_LINKS_PREF,
                new HashMap<>());
        for (Lecture lecture : lectureList) {
            try {
                if (lecture.videoLink != null) {
                    String urlHLS = videoLinks.get(getYoutubeVideoId(lecture.videoLink));
                    uriString = urlHLS;
                    Uri mp4VideoUri = Uri.parse(uriString);
                    if (uriString.toUpperCase().contains("M3U8")) {
                        mediaSources.add(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri));
                    } else {
                        mp4VideoUri = Uri.parse(urlHLS);
                        mediaSources.add(new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, new DefaultExtractorsFactory(),
                                null, null));
                    }
                }
            } catch (Exception ex) {
                Log.e("ErrorX01-UN", Objects.requireNonNull(ex.getMessage()));
               // Utils.getInstance().showToast(mContext,"ErrorX01-UN");
            }

        }
        return mediaSources;
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

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, @Player.TimelineChangeReason int reason) {
            if (DEBUG) {
                Log.i(TAG, "onTimelineChanged()");
            }
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
            for (OnVideoPlayerEventListener listener : mPlayerListeners) {
                listener.playerState(playbackState);
            }
            if (DEBUG) {
                Log.i(TAG, "onPlayerStateChanged()");
            }
            if (playbackState == STATE_READY) {

                updateCurrentLecture();
                if (isPlaying()) {
                    Log.i("LOCATION", "callback added");
                    handler.removeCallbacks(updateProgressAction);
                    updateProgress();
                } else {
                    Log.i("LOCATION", "callback removed");
                    handler.removeCallbacks(updateProgressAction);
                }

            }
            if (playbackState == Player.STATE_ENDED) {
                Log.i("LOCATION", "callback removed");
                handler.removeCallbacks(updateProgressAction);
            }
            for (OnVideoPlayerEventListener listener : mPlayerListeners) {
                listener.playerStateChanged();
            }

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Stop and reset the player
            e.printStackTrace();
//            Utils.getInstance().showToast(mContext,"ErrorX03-EPE");
            Log.i("ErrorX03-EPE", e.getLocalizedMessage());
            for (OnVideoPlayerEventListener listener : mPlayerListeners) {
                listener.playerError();
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (DEBUG) {
                Log.i(TAG, "onPositionDiscontinuity()");
            }
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                updateCurrentLecture();
                for (OnVideoPlayerEventListener listener : mPlayerListeners) {
                    listener.playerStateChanged();
                }
            }
        }
    }
}