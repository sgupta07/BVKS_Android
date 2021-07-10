package com.iskcon.bvks.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.exoplayer2.ExoPlayerFactory;
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
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.util.urlextractor.VideoMeta;
import com.iskcon.bvks.util.urlextractor.YouTubeExtractor;
import com.iskcon.bvks.util.urlextractor.YtFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;


@Deprecated//not in use(Copy of V3). work is on going on V3
public class VideoPlayerManagerV2 {
    /**
     * declare some usable variable
     */
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String TAG = VideoPlayerManagerV2.class.getSimpleName();
    private static VideoPlayerManagerV2 mInstance = null;
    PlayerView mPlayerView;
    DefaultDataSourceFactory dataSourceFactory;
    String uriString = "";
    List<Lecture> mPlayList = null;
    Integer playlistIndex = 0;
    private SimpleExoPlayer mPlayer;
    Context mContext;

    /**
     * private constructor
     * @param context
     */
    private VideoPlayerManagerV2(Context context) {
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
                } else if (playbackState == 4 && mPlayList != null && playlistIndex + 1 == mPlayList.size()) {
                    mPlayer.setPlayWhenReady(false);
                }
            }
        });
    }
    /**
     * Return ExoPlayerManager instance
     * @param context context of calling view
     * @return
     */
    public static VideoPlayerManagerV2 getSharedInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VideoPlayerManagerV2(context);
        }
        return mInstance;
    }

    private String mYouTubeVideoId = "";

    private final String mYouTubeBaseUrl = "https://www.youtube.com";

    private void getYoutubeVideoId(@NotNull String videoLink) {
        Log.i(TAG, "videoLink---->" + videoLink);
        String lastBit;
        if (videoLink.contains("watch")){
            lastBit = videoLink.substring(videoLink.lastIndexOf('='));
            lastBit = lastBit.replace("=", "");
        }else {
            lastBit = videoLink.substring(videoLink.lastIndexOf('/'));
            lastBit = lastBit.replace("/", "");
        }
        Log.i(TAG, "videoID---->" + lastBit);
        mYouTubeVideoId = lastBit;
    }


    public PlayerView getPlayerView() {
        return mPlayerView;
    }

    public void playStream(Lecture lecture) {
        getYoutubeVideoId(lecture.videoLink);
        @SuppressLint("StaticFieldLeak") YouTubeExtractor mExtractor = new YouTubeExtractor(mContext) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                if (sparseArray != null) {
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
                    mPlayer.setPlayWhenReady(true);
                }
            }
        };
        String youTubeLink = mYouTubeBaseUrl + "/watch?v=" + mYouTubeVideoId;
        mExtractor.extract(youTubeLink, true, true);
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