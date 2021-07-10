package com.iskcon.bvks.ui.player;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.Log;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.iskcon.bvks.R;
import com.iskcon.bvks.base.BvksApplication;
import com.iskcon.bvks.listeners.OnPlayerEventListener;
import com.iskcon.bvks.manager.LectureManager;
import com.iskcon.bvks.manager.PlayerManager;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.ui.playlist.PlaylistTypeChoiceSheet;
import com.iskcon.bvks.util.Constants;
import com.iskcon.bvks.util.DownloadTracker;
import com.iskcon.bvks.util.ImageUtil;
import com.iskcon.bvks.util.Utils;

import java.text.SimpleDateFormat;

import static com.iskcon.bvks.base.BvksApplication.DEBUG;

/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class PlayerActivity extends AppCompatActivity implements OnPlayerEventListener, PlaybackPreparer,
        View.OnClickListener, AdapterView.OnItemSelectedListener, DownloadTracker.Listener {

    private final static int FAST_FORWARD_MS = 10000;
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private final float SIZE_FAVORITE = 28.0f;
    private final float SIZE_NOT_FAVORITE = 14.0f;
    ImageButton ibBack, ibPlaylist, ibFavorite, ibDownload, ibShare;
    ProgressBar pbDownloadProgress;
    TextView tvLectureTitle, tvRecordingDate, tvPlaceOfRecording, tvCountry, tvLanguage, tvCategory;
    RelativeLayout rlButtonSpeed;
    private PlayerManager mPlayerMgr;
    private PlayerControlView mPlayerControlView;
    private DownloadTracker mDownloadTracker;
    private Lecture mLecture;
    private SimpleExoPlayer mPlayer;
    private ImageView mAlbumArtImage;
    private SharedPreferences mSharedPref;
    private Handler handler;
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        mLecture = (Lecture) getIntent().getSerializableExtra("Lecture");

        //Getting current or default playback speed
        mSharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.playback_speed_pref_key),
                Context.MODE_PRIVATE);
        int currentSpeed = mSharedPref.getInt(getString(R.string.playback_speed_pref_key), 2);
        mPlayerMgr = PlayerManager.getInstance();
        double currentPosition = Double.parseDouble("" + mPlayerMgr.getPlayer().getCurrentPosition()) / mPlayerMgr.getPlayer().getDuration();
        Log.i(TAG, "PLAYERPOSITION:" + (int) (currentPosition * 100));
        mDownloadTracker = ((BvksApplication) getApplication()).getDownloadTracker();

        mPlayerControlView = findViewById(R.id.player_control_view);
        mPlayerControlView.setPlaybackPreparer(this);
        mPlayerControlView.setShowTimeoutMs(0);
        mPlayerControlView.setFastForwardIncrementMs(FAST_FORWARD_MS);

        mAlbumArtImage = findViewById(R.id.album_art);


        Spinner spinner = findViewById(R.id.dropdown_play_speed);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.play_speed_array, R.layout.play_speed_item);

        adapter.setDropDownViewResource(R.layout.play_speed_all_items);
        spinner.setAdapter(adapter);
        spinner.setSelection(currentSpeed);
        //set toolbar listeners
        ibBack = findViewById(R.id.ib_back);
        ibPlaylist = findViewById(R.id.ib_playlist);
        ibFavorite = findViewById(R.id.ib_favorite);
        ibDownload = findViewById(R.id.ib_download);
        pbDownloadProgress = findViewById(R.id.pb_download_progress);
        ibShare = findViewById(R.id.ib_share);
        ibBack.setOnClickListener(v -> finish());
        ibPlaylist.setOnClickListener(v -> addToPlaylist());
        ibFavorite.setOnClickListener(V -> markFavorite());
        ibDownload.setOnClickListener(V -> downloadLecture());
        ibShare.setOnClickListener(V -> shareWithDeepLink());
        //Details Text Views
        tvLectureTitle = findViewById(R.id.tv_lecture_title);
        tvRecordingDate = findViewById(R.id.tv_lecture_date);
        tvPlaceOfRecording = findViewById(R.id.tv_lecture_place);
        tvCountry = findViewById(R.id.tv_lecture_country);
        tvLanguage = findViewById(R.id.tv_lecture_language);
        tvCategory = findViewById(R.id.tv_lecture_category);
        rlButtonSpeed = findViewById(R.id.rl_button_speed);
        placeDownloadIcon();
        placeFavoriteIcon();
        setAlbumArtAndDetails();
        handler = new Handler();
        pbDownloadProgress.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPlayer = mPlayerMgr.getPlayer();
        mPlayerControlView.setPlayer(mPlayer);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // See whether the player view wants to handle media or DPAD keys events.
        return mPlayerControlView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }
    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            if (DEBUG) {
                Log.d(TAG, "Headphone key pressed " + keyCode);
                if (PlayerManager.getInstance().isPlaying()) {
                    PlayerManager.getInstance().pause();
                } else {
                    PlayerManager.getInstance().play(mLecture);
                }
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (PlayerManager.getInstance().isPlaying()) {
                PlayerManager.getInstance().pause();
            } else {
                PlayerManager.getInstance().play(mLecture);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void placeDownloadIcon() {
        if (mDownloadTracker.isDownloaded(Uri.parse(mLecture.mediaUrl))) {
            ibDownload.setEnabled(false);
            ibDownload.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_download_orange));
        } else {
            ibDownload.setEnabled(true);
            ibDownload.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_download));
        }
    }

    private void downloadLecture() {
        if (mDownloadTracker.isDownloaded(Uri.parse(mLecture.mediaUrl))) {
            Utils.getInstance().showToast(this, getString(R.string.downloaded_already));
        } else {
            RenderersFactory renderersFactory =
                    ((BvksApplication) getApplication()).buildRenderersFactory();
            mDownloadTracker.toggleDownload(mLecture.name,
                    Uri.parse(mLecture.mediaUrl), mLecture.mediaUrl.substring(mLecture.mediaUrl.
                            lastIndexOf(".") + 1), renderersFactory);
            Utils.getInstance().showToast(this, getString(R.string.downloading));
            ibDownload.setEnabled(false);
            // todo commented by PS
            // ibDownload.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_download_done));
        }
    }

    private void placeFavoriteIcon() {
        if (mLecture != null && mLecture.information != null && mLecture.information.isFavourite) {
            //FAVORITE
            changeButtonSize(ibFavorite, (int) convertDpToPx(this,SIZE_FAVORITE));
            ibFavorite.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_favorite_gold_player));
        } else {
            //NOT FAVORITE
            changeButtonSize(ibFavorite, (int) convertDpToPx(this,SIZE_NOT_FAVORITE));
            ibFavorite.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_favorite));
        }
    }

    private void changeButtonSize(ImageButton btn, int size) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btn.getLayoutParams();
        // Set the height of this ImageButton
        params.height = size;
        // Set the width of that ImageButton
        params.width = size;
        // Apply the updated layout parameters to last ImageButton
        btn.setLayoutParams(params);
    }

    private void markFavorite() {
        if (mLecture != null && mLecture.information != null && mLecture.information.isFavourite) {
            //FAVORITE(REMOVE FAVORITE)
            LectureManager.getInstance().markUnMarkFavorite(mLecture, false);
            changeButtonSize(ibFavorite, (int) convertDpToPx(this,SIZE_NOT_FAVORITE));
            ibFavorite.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_favorite));
        } else {
            //NOT FAVORITE(ADD FAVORITE)
            LectureManager.getInstance().markUnMarkFavorite(mLecture, true);
            changeButtonSize(ibFavorite, (int) convertDpToPx(this,SIZE_FAVORITE));
            ibFavorite.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_favorite_gold_player));
        }
    }

    private void addToPlaylist() {
        PlaylistTypeChoiceSheet.Companion.newInstance(mLecture.id).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerMgr.removeListener(this);
        mDownloadTracker.removeListener(this);
        handler.removeCallbacks(updateProgressAction);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayerMgr.addListener(this);
        mDownloadTracker.addListener(this);
    }

    /**
     * Method to create dynamic link and share it on social platform
     */
    private void shareWithDeepLink() {
        String link = Constants.DEEP_LINK_URL + "?lectureId=" + mLecture.id;
        String description = Utils.getInstance().createLectureDescriptionForShare(mLecture);
        String thumbnailUrl = Utils.getInstance().getLectureThumbnailUrlForShare(mLecture);
        DynamicLink longLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDynamicLinkDomain(Constants.DYNAMIC_LINK_DOMAIN)
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .setIosParameters(new DynamicLink.IosParameters.Builder("com.bhakti.bvks").setAppStoreId("1536451261").build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(mLecture.name)
                                .setDescription(description)
                                .setImageUrl(Uri.parse(thumbnailUrl))
                                .build())
                .buildDynamicLink();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(longLink.getUri())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri shortLink = task.getResult().getShortLink();
                        System.out.println("shortLink = " + shortLink);
                        //open share dialog
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.valueOf(shortLink));
                        startActivity(Intent.createChooser(sendIntent, "Share Lecture"));
                    } else {
                        Utils.getInstance().showToast(this, "Something went wrong...");
                    }
                });
    }

    @Override
    public void playerStateChanged() {
        mLecture = mPlayerMgr.getCurrentLecture();
        setAlbumArtAndDetails();
        placeDownloadIcon();
        placeFavoriteIcon();
    }

    @Override
    public void playerError() {
    }

    @Override
    public void preparePlayback() {
        if (mPlayer != null) {
            mPlayer.retry();
        }
    }

    private void setAlbumArtAndDetails() {
        SimpleDateFormat mDateFormat;
        mDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        ImageUtil.loadPlayerImage(mAlbumArtImage, mLecture.thumbnailUrl);
        tvLectureTitle.setText("Lecture Title: " + mLecture.name);
        tvRecordingDate.setText("Recording Date: " + mDateFormat.format(mLecture.date));
        tvPlaceOfRecording.setText("Place of Recording: " + mLecture.place);
        tvCountry.setText("Country: " + mLecture.country);
        tvCategory.setText("Category: " + mLecture.category);
        tvLanguage.setText("Language: " + mLecture.language);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        switch (i) {
            case 0:
                mPlayerMgr.speed(0.50f);
                break;
            case 1:
                mPlayerMgr.speed(0.75f);
                break;
            case 2:
                mPlayerMgr.speed(1.0f);
                break;
            case 3:
                mPlayerMgr.speed(1.15f);
                break;
            case 4:
                mPlayerMgr.speed(1.25f);
                break;
            case 5:
                mPlayerMgr.speed(1.50f);
                break;
            case 6:
                mPlayerMgr.speed(1.75f);
                break;
            case 7:
                mPlayerMgr.speed(2.0f);
                break;
        }
        editor.putInt(getString(R.string.playback_speed_pref_key), i);
        editor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void updateProgress() {
        Log.i(TAG, "UpdateProgress");
        if (mDownloadTracker.isDownloading(Uri.parse(mLecture.mediaUrl))) {
            Log.i(TAG, "UpdateProgress1");
            ibDownload.setVisibility(View.GONE);
            pbDownloadProgress.setVisibility(View.VISIBLE);
        } else {
            Log.i(TAG, "UpdateProgress2");
            if (mDownloadTracker.isDownloaded(Uri.parse(mLecture.mediaUrl))) {
                ibDownload.setVisibility(View.VISIBLE);
                pbDownloadProgress.setVisibility(View.GONE);
                Log.i(TAG, "UpdateProgress3");
                ibDownload.setEnabled(false);
                ibDownload.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_download_orange));
                handler.removeCallbacks(updateProgressAction);
            }
        }

    }

    @Override
    public void onDownloadsChanged() {
        Log.i(TAG, "onDownloadsChanged");
        handler.postDelayed(updateProgressAction, 1000);
    }
}
