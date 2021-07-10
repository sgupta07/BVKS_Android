package com.iskcon.bvks.ui.lectures;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.util.Log;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.iskcon.bvks.R;
import com.iskcon.bvks.listeners.OnPlayerEventListener;
import com.iskcon.bvks.manager.LectureManager;
import com.iskcon.bvks.manager.PlayerManager;
import com.iskcon.bvks.manager.VideoPlayerManagerV4;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.ui.main.MainActivity;
import com.iskcon.bvks.ui.player.PlayerActivity;
import com.iskcon.bvks.ui.videoplayer.VideoPlayerActivity;
import com.iskcon.bvks.ui.videoplayer.VideoPlayerActivityV4;
import com.iskcon.bvks.util.ImageUtil;
import com.iskcon.bvks.util.LectureNotificationService;
import com.iskcon.bvks.util.Utils;
import com.iskcon.bvks.util.VideoNotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class LectureFragment extends Fragment implements OnPlayerEventListener {

    private final String TAG = LectureFragment.class.getSimpleName();
    private LecturesPagerAdapter mLecturePagerAdapter;
    private View mPlayWidgetView;
    private TabLayout mTabLayout;
    private Boolean isVideoMode = false;

    public LectureFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLecturePagerAdapter = new LecturesPagerAdapter(getContext(), getChildFragmentManager());
        PlayerManager.getInstance().addListener(this);

        if (getActivity() != null) {

            //Handle deep link urls
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getActivity().getIntent())
                    .addOnSuccessListener(getActivity(), pendingDynamicLinkData -> {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        if (deepLink != null) {
                            String url = deepLink.toString();
                            Log.i(TAG, "url = " + url);
                            long lectureId = Long.parseLong(Objects.requireNonNull(deepLink.getQueryParameter("lectureId")));

                            if (getActivity() instanceof MainActivity) {
                                Lecture lecture = PlayerManager.getInstance().getDeepLinkLecture(lectureId);
                                Log.i(TAG, "lectureName = " + lecture.name);
                                if (TextUtils.isEmpty(lecture.name)) {
                                    mPlayWidgetView.setVisibility(View.GONE);
                                } else {
                                    PlayerManager.getInstance().play(lecture);
                                    Log.i(TAG, "Deep Link");
                                    updatePlayerWidget(lecture,false);//deep link
                                }
                            }
                        }
                    })
                    .addOnFailureListener(getActivity(), e -> Utils.getInstance().showToast(getContext(), "Something went wrong..."));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lecture, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(mLecturePagerAdapter);

        mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);
        ////////////Home Icon And Text///////////////
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_tab_home);
        mTabLayout.getTabAt(0).setText("Home");
        ////////////Playlist Icon And Text///////////////
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_tab_playlist);
        mTabLayout.getTabAt(1).setText("Playlists");
        ////////////Top Lectures Icon And Text///////////////
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_tab_top_lecture);
        mTabLayout.getTabAt(2).setText("Top Lectures");
        ////////////Downloads Icon And Text///////////////
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_tab_downloads);
        mTabLayout.getTabAt(3).setText("Downloads");
        ////////////Favorites Icon And Text///////////////
        mTabLayout.getTabAt(4).setIcon(R.drawable.ic_tab_favorites);
        mTabLayout.getTabAt(4).setText("Favorites");

        mTabLayout.setVisibility(View.VISIBLE);
        mTabLayout.setTabTextColors(ContextCompat.getColorStateList(getContext(),R.color.white));
        mTabLayout.selectTab(mTabLayout.getTabAt(0));


        mPlayWidgetView = view.findViewById(R.id.play_widget);
        Lecture currentLecture = PlayerManager.getInstance().getCurrentLecture();
        if (TextUtils.isEmpty(currentLecture.name)) {
            mPlayWidgetView.setVisibility(View.GONE);
        } else {
            Log.i(TAG, "onViewCreated");
            updatePlayerWidget(currentLecture,false);//onViewCreated
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PlayerManager.getInstance().removeListener(this);
    }

    @Override
    public void playerStateChanged() {
        Lecture currentLecture = PlayerManager.getInstance().getCurrentLecture();
        if (currentLecture == null) {
            return;
        }
        if (isVideoMode) {
            //VIDEO ACTION
            updatePlayerWidget(currentLecture,false);//playerStateChanged
        } else {
            //AUDIO ACTION
            Log.i(TAG, "playerStateChanged");
            updatePlayerWidget(currentLecture,false);//playerStateChanged
        }
        notifyFragments();
    }

    @Override
    public void playerError() {
        Toast.makeText(getContext(), R.string.error_playing, Toast.LENGTH_LONG).show();
        Lecture currentLecture = PlayerManager.getInstance().getCurrentLecture();
        if (currentLecture == null) {
            return;
        }
        Log.i(TAG, "playerError");
        updatePlayerWidget(currentLecture,false);//playerError
        notifyFragments();
    }

    private void notifyFragments() {
        List<Fragment> fragments = mLecturePagerAdapter.getFragments();
        for (Fragment fragment : fragments) {
            if (!fragment.isAdded()) {
                continue;
            }
            if (fragment instanceof LectureListBaseFragment) {
                ((LectureListBaseFragment) fragment).playerStateChanged();
            }

        }
    }

    public void onLecturePlayed(Lecture lecture, boolean isVideoMode) {
        this.isVideoMode = isVideoMode;
        if (isVideoMode) {
            Intent intent = new Intent(getContext(), VideoPlayerActivityV4.class);
            intent.putExtra("Lecture", lecture);
            startActivity(intent);
            updatePlayerWidget(lecture,false);//onLecturePlayed

            // clear old notification and start new
            NotificationManagerCompat.from(requireActivity()).cancelAll();
            requireActivity().startService(new Intent(requireActivity(), VideoNotificationService.class));

        } else {

            Log.i(TAG, "onLecturePlayed");
            updatePlayerWidget(lecture,false);//onLecturePlayed

            // clear old notification and start new
            NotificationManagerCompat.from(requireActivity()).cancelAll();
            requireActivity().startService(new Intent(requireActivity(), LectureNotificationService.class));

        }
    }

    private void updatePlayerWidget(@NonNull Lecture lecture,Boolean isOnlyPause) {

        mPlayWidgetView.setOnClickListener((View v) -> {
            if (isVideoMode){
                if (PlayerManager.getInstance().isPlaying()) {
                    PlayerManager.getInstance().pause();
                }
                List<Lecture> mLectures=new ArrayList<>();
                mLectures.add(lecture);
                VideoPlayerManagerV4.getSharedInstance(getContext()).setPlaylist(mLectures,0);
                Intent intent = new Intent(getContext(), VideoPlayerActivityV4.class);
                intent.putExtra("Lecture", lecture);
                startActivity(intent);
            }else {
                Lecture tempLecture= LectureManager.getInstance().getLecture(lecture.id);
                Intent intent = new Intent(getContext(), PlayerActivity.class);
                intent.putExtra("Lecture", tempLecture);
                startActivity(intent);
            }

        });
        if (isOnlyPause){
            ImageView playPauseImageView = mPlayWidgetView.findViewById(R.id.playPause);
            playPauseImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            if (PlayerManager.getInstance().isPlaying()) {
                PlayerManager.getInstance().pause();
            }

            TextView verseView = mPlayWidgetView.findViewById(R.id.verse);
            verseView.setText(lecture.category);

            TextView titleView = mPlayWidgetView.findViewById(R.id.title);
            titleView.setSelected(true);
            titleView.setText(lecture.name);

            ImageView thumbnailView = mPlayWidgetView.findViewById(R.id.thumbnail);
            ImageUtil.loadThumbnail(thumbnailView, lecture.thumbnailUrl);
        }else {
            ImageView playPauseImageView = mPlayWidgetView.findViewById(R.id.playPause);
            playPauseImageView.setImageResource(PlayerManager.getInstance().isPlaying() ?
                    R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);
            playPauseImageView.setOnClickListener((View v) -> {
                if (PlayerManager.getInstance().isPlaying()) {
                    PlayerManager.getInstance().pause();
                } else {
                    PlayerManager.getInstance().play(lecture);
                }
            });

            TextView verseView = mPlayWidgetView.findViewById(R.id.verse);
            verseView.setText(lecture.category);

            TextView titleView = mPlayWidgetView.findViewById(R.id.title);
            titleView.setSelected(true);
            titleView.setText(lecture.name);

            ImageView thumbnailView = mPlayWidgetView.findViewById(R.id.thumbnail);
            ImageUtil.loadThumbnail(thumbnailView, lecture.thumbnailUrl);
            mPlayWidgetView.setVisibility(View.VISIBLE);
        }

    }


}
