package com.iskcon.bvks.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
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
import com.iskcon.bvks.util.Constants;
import com.iskcon.bvks.util.urlextractor.VideoMeta;
import com.iskcon.bvks.util.urlextractor.YouTubeExtractor;
import com.iskcon.bvks.util.urlextractor.YtFile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;



import static com.google.android.exoplayer2.Player.STATE_READY;
import static com.iskcon.bvks.base.BvksApplication.DEBUG;

public class VideoPlayerManagerV4 {
    /**
     * declare some usable variable
     */
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String TAG = VideoPlayerManagerV4.class.getSimpleName();
    private static VideoPlayerManagerV4 mInstance = null;
    PlayerView mPlayerView;
    DefaultDataSourceFactory dataSourceFactory;
    String uriString = "";
    List<Lecture> mPlayList = null;
    Integer playlistIndex = 0;
    private SimpleExoPlayer mPlayer;
    Context mContext;

    public void speed(float speed) {
        PlaybackParameters param = new PlaybackParameters(speed);
        mPlayer.setPlaybackParameters(param);
    }
    private Handler handler;

    private VideoPlayerManagerV4(Context context) {
        mContext=context;
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
        mPlayer.addListener(new Player.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i(TAG, "onPlayerStateChanged: ");
                if (playbackState == 4 && mPlayList != null && playlistIndex + 1 < mPlayList.size()) {
                    Log.e(TAG, "Song Changed...");
                    playlistIndex++;
                    playStream(mPlayList.get(playlistIndex));
                    for (OnVideoPlayerEventListener listener : mPlayerListeners) {
                        listener.playerStateChanged();
                    }
                } else if (playbackState == 4 && mPlayList != null && playlistIndex + 1 == mPlayList.size()) {
                    mPlayer.setPlayWhenReady(false);
                }
                if (DEBUG) {
                    Log.i(TAG, "onPlayerStateChanged()");
                }
                if (playbackState == STATE_READY) {

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
        });
        handler = new Handler();
        mPlayerListeners = new ArrayList<>();
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
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
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
    private Lecture mCurrentLecture;
    @Nullable
    public Lecture getCurrentLecture() {
        return mCurrentLecture;
    }
    public void cleanUp() {
        if (DEBUG) {
            Log.d(TAG, "Cleanup");
        }
        mPlayer.release();
        mPlayer = null;
        mCurrentLecture = null;
    }
    public static VideoPlayerManagerV4 getSharedInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VideoPlayerManagerV4(context);
        }
        return mInstance;
    }
    public static VideoPlayerManagerV4 getInstance() {
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

    public SimpleExoPlayer getPlayer() {
        return mPlayer;
    }

    public PlayerView getPlayerView() {
        return mPlayerView;
    }

    public void playStream(Lecture lecture) {
        mCurrentLecture=lecture;
        @SuppressLint("StaticFieldLeak") YouTubeExtractor mExtractor = new YouTubeExtractor(mContext) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                if (sparseArray != null) {
                    try{
                        String urlHLS=sparseArray.get(22).getUrl();
                        Log.i(TAG,"URL HLS-"+urlHLS);
                        uriString = urlHLS;
                        Uri mp4VideoUri = Uri.parse(uriString);
                        MediaSource videoSource;
                        if (uriString.toUpperCase().contains("M3U8")) {
                            Log.i(TAG,"M3U8");
                            videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri);
                        } else {
                            Log.i(TAG,"Not M3U8");
                            mp4VideoUri = Uri.parse(urlHLS);
                            videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, new DefaultExtractorsFactory(),
                                    null, null);
                        }
                        // Prepare the player with the source.
                        mPlayer.prepare(videoSource);
                        mPlayer.seekTo(0, getLastPlayedTime(lecture));
                        mPlayer.setPlayWhenReady(true);
                    }catch(Exception e){
                        String urlHLS=sparseArray.get(18).getUrl();
                        Log.i(TAG,"URL HLS-"+urlHLS);
                        uriString = urlHLS;
                        Uri mp4VideoUri = Uri.parse(uriString);
                        MediaSource videoSource;
                        if (uriString.toUpperCase().contains("M3U8")) {
                            Log.i(TAG,"M3U8");
                            videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri);
                        } else {
                            Log.i(TAG,"Not M3U8");
                            mp4VideoUri = Uri.parse(urlHLS);
                            videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, new DefaultExtractorsFactory(),
                                    null, null);
                        }
                        // Prepare the player with the source.
                        mPlayer.prepare(videoSource);
                        mPlayer.setPlayWhenReady(true);

                    }
                }else {
                    Log.i(TAG,"sparseArray null");
                    playStream(lecture);
                }
            }
        };
        String youTubeLink = Constants.YOU_TUBE_BASE_URL + "/watch?v=" + getYoutubeVideoId(lecture.videoLink);
        mExtractor.extract(youTubeLink, true, true);
    }
    private Long getLastPlayedTime(Lecture lecture) {
        if (lecture != null && lecture.information != null) {
            return lecture.information.lastPlayedPoint*1000;
        } else {
            return 0L;
        }
    }
    @Deprecated
    public void playStream(String urlToPlay) {
        uriString = urlToPlay;
        Uri mp4VideoUri = Uri.parse(uriString);
        MediaSource videoSource;
        if (uriString.toUpperCase().contains("M3U8")) {
            videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri);
        } else {
            mp4VideoUri = Uri.parse(urlToPlay);
            videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, new DefaultExtractorsFactory(),
                    null, null);
        }
        // Prepare the player with the source.
        mPlayer.prepare(videoSource);
        mPlayer.setPlayWhenReady(true);
    }

    public void addListener(OnVideoPlayerEventListener listener) {
        mPlayerListeners.add(listener);
    }
    private List<OnVideoPlayerEventListener> mPlayerListeners;
    public void removeListener(OnVideoPlayerEventListener listener) {
        mPlayerListeners.remove(listener);
    }

    public void setPlaylist(List<Lecture> uriArrayList, Integer index) {
        Log.i(TAG,"playlistSet-Size"+uriArrayList.size());
        mPlayList = uriArrayList;
        playlistIndex = index;
        playStream(mPlayList.get(playlistIndex));
    }

    public void playerPlaySwitch() {
        if (uriString != "") {
            mPlayer.setPlayWhenReady(!mPlayer.getPlayWhenReady());
        }
    }
    public void stopPlayer(boolean state) {
        mPlayer.setPlayWhenReady(!state);
    }
    public void destroyPlayer() {
        mPlayer.stop();
    }

    public Boolean isPlayerPlaying() {
        return mPlayer.getPlayWhenReady();
    }

}