package com.iskcon.bvks.ui.lectures;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.util.Log;
import com.iskcon.bvks.R;
import com.iskcon.bvks.base.BvksApplication;
import com.iskcon.bvks.firebase.Analytics;
import com.iskcon.bvks.listeners.LectureMenuPopupWindowListener;
import com.iskcon.bvks.listeners.OnListItemInteractionListener;
import com.iskcon.bvks.manager.LectureManager;
import com.iskcon.bvks.manager.PlayerManager;
import com.iskcon.bvks.manager.HistoryManager;
import com.iskcon.bvks.manager.PlaylistsManager;
import com.iskcon.bvks.manager.PopularTopLectureManager;
import com.iskcon.bvks.manager.VideoPlayerManagerV2;
import com.iskcon.bvks.manager.VideoPlayerManagerV3;
import com.iskcon.bvks.manager.VideoPlayerManagerV4;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.ui.playlist.PlaylistTypeChoiceSheet;
import com.iskcon.bvks.util.ImageUtil;
import com.iskcon.bvks.util.LectureUtil;
import com.iskcon.bvks.util.PopUpClass;

import java.text.SimpleDateFormat;
import java.util.List;

public class LectureAdapter extends RecyclerView.Adapter<LectureAdapter.ViewHolder> {

    private static final String TAG = "LectureAdapter";
    private OnListItemInteractionListener mOverflowMenuListener;
    private List<Lecture> mLectures;
    private SimpleDateFormat mDateFormat;
    private Lecture selectedLecture;
    private Context mContext;
    private Boolean showTotalCount=false;

    public LectureAdapter(@NonNull OnListItemInteractionListener overflowListener, @NonNull List<Lecture> lectureList) {
        mOverflowMenuListener = overflowListener;
        mLectures = lectureList;
        mDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    }
    public LectureAdapter(@NonNull OnListItemInteractionListener overflowListener, @NonNull List<Lecture> lectureList,Context context) {
        mOverflowMenuListener = overflowListener;
        mLectures = lectureList;
        mDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        mContext=context;
    }

    public void setShowTotalCount(Boolean showTotalCount) {
        this.showTotalCount = showTotalCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lecture_item_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Lecture lecture = mLectures.get(position);
        if (lecture == null) {
            return;
        }
        /////Set Title//////////////
        holder.mTitle.setText(lecture.name);
        /////Place Name///////////////
        holder.mPlace.setText(lecture.place);
        ///////Verse name///////////
        if (lecture.globlePlayCount>0&&showTotalCount){
            holder.mVerse.setText("Total play(s): "+ lecture.globlePlayCount);
        }else {
            holder.mVerse.setText(lecture.verse);
        }
        ///////////Lecture Length///////////
        holder.mLength.setText(LectureUtil.getFormattedTime(lecture.mediaLength));
        ////////////Date//////////////
        holder.mDate.setText(mDateFormat.format(lecture.date));
        /////////////////////Play Animation///////////////////////////////
        if (lecture.equals(PlayerManager.getInstance().getCurrentLecture())) {
            holder.mThumbnail.setVisibility(View.GONE);
            holder.mPeakMeter1.setVisibility(View.VISIBLE);
            holder.mPeakMeter2.setVisibility(View.VISIBLE);
            holder.mPeakMeter3.setVisibility(View.VISIBLE);
            AnimationDrawable frameAnimation1 = (AnimationDrawable) holder.mPeakMeter1.getBackground();
            AnimationDrawable frameAnimation2 = (AnimationDrawable) holder.mPeakMeter2.getBackground();
            AnimationDrawable frameAnimation3 = (AnimationDrawable) holder.mPeakMeter3.getBackground();
            if (PlayerManager.getInstance().isPlaying()) {
                frameAnimation1.start();
                frameAnimation2.start();
                frameAnimation3.start();
            } else {
                frameAnimation1.stop();
                frameAnimation2.stop();
                frameAnimation3.stop();
            }
        } else {
            holder.mThumbnail.setVisibility(View.VISIBLE);
            holder.mPeakMeter1.setVisibility(View.GONE);
            holder.mPeakMeter2.setVisibility(View.GONE);
            holder.mPeakMeter3.setVisibility(View.GONE);
            ImageUtil.loadThumbnail(holder.mThumbnail, lecture.thumbnailUrl);
        }

        ///////////////Manage favorite Icon/////////////////////////
        if (lecture.information != null && lecture.information.isFavourite) {
            holder.mImageViewFavoriteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mImageViewFavoriteIcon.setVisibility(View.INVISIBLE);
        }
        /////////////////////Manage Download Icon//////////////////////
        if (isDownloaded(holder.mImageViewDownloadIcon.getContext(), lecture.mediaUrl)) {
            holder.mImageViewDownloadIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mImageViewDownloadIcon.setVisibility(View.INVISIBLE);
        }
        /////////////////////Manage Downloading Icon//////////////////////
        if (isDownloading(holder.mImageViewDownloadIcon.getContext(), lecture.mediaUrl)) {
            holder.mImageViewDownloadImage.setVisibility(View.VISIBLE);
        } else {
            holder.mImageViewDownloadImage.setVisibility(View.INVISIBLE);
        }
        /////////////////////Manage lecture Complete Icon//////////////////////
        if (lecture.information != null && lecture.information.isCompleted) {
            holder.ivLectureComplete.setVisibility(View.VISIBLE);
            holder.mProgressBar.setVisibility(View.INVISIBLE);
            holder.mProgressText.setVisibility(View.INVISIBLE);
        } else {
            holder.ivLectureComplete.setVisibility(View.INVISIBLE);
            holder.mProgressBar.setVisibility(View.VISIBLE);
            holder.mProgressText.setVisibility(View.VISIBLE);
        }
        /////////////////////Manage lecture Percentage//////////////////////
        if (lecture.information != null && lecture.information.totallength > 0 && lecture.information.lastPlayedPoint > 0) {
            long value = lecture.information.lastPlayedPoint * 100;
            int percentage = Math.toIntExact(value / lecture.information.totallength);
            holder.mProgressBar.setProgress(percentage);
            holder.mProgressText.setText(percentage + "%");
        } else {
            holder.mProgressBar.setProgress(0);
            holder.mProgressText.setText(0 + "%");
        }
        ///////////////Set Tag to View////////////////////////////////
        holder.mView.setTag(lecture);
        ///////////////////Set click listener to view//////////////////////////////
        holder.mView.setOnClickListener(v -> {

            Lecture taggedLecture = (Lecture) v.getTag();
            if (mOverflowMenuListener.isVideoOption()){
                //VIDEO ACTION
                if (PlayerManager.getInstance().isPlaying()) {
                    PlayerManager.getInstance().pause();
                }
                playVideo(taggedLecture);
                if (HistoryManager.INSTANCE.getRecentlyPlayedLectures().contains(taggedLecture.id)) {
                    HistoryManager.INSTANCE.removeFromHistory(taggedLecture.id);
                }
                HistoryManager.INSTANCE.addToHistory(taggedLecture.id);
                PopularTopLectureManager.getInstance().updateTopLectureDetail(taggedLecture);
                mOverflowMenuListener.playLecture(taggedLecture,mOverflowMenuListener.isVideoOption());
                holder.mView.post(() -> {
                    notifyDataSetChanged();
                });
            }else {
                //AUDIO ACTION
                if (PlayerManager.getInstance().isPlaying() && PlayerManager.getInstance().getCurrentLecture().equals(taggedLecture)) {
                    PlayerManager.getInstance().pause();

                } else {
                    playLecture(taggedLecture);
                    if (HistoryManager.INSTANCE.getRecentlyPlayedLectures().contains(taggedLecture.id)) {
                        HistoryManager.INSTANCE.removeFromHistory(taggedLecture.id);
                    }
                    PopularTopLectureManager.getInstance().updateTopLectureDetail(taggedLecture);
                    HistoryManager.INSTANCE.addToHistory(taggedLecture.id);
                    mOverflowMenuListener.playLecture(taggedLecture,mOverflowMenuListener.isVideoOption());
                    holder.mView.post(() -> {
                        notifyDataSetChanged();
                    });
                }
                Log.i(TAG, "mView.setOnClickListener");

            }


        });
        //////////////////////Set tag to Setting view////////////////////////////
        holder.mSettingsPopup.setTag(lecture);
        ////////////////////Set click listener to setting view/////////////////////////
        holder.mSettingsPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenuV2(view,mOverflowMenuListener.isRemoveLectureAllowed());
            }
        });
        //////////////////////Set tag to Favorite Icon view////////////////////////////
        holder.mImageViewFavoriteIcon.setTag(lecture);
        ///////////////////////Set Click Listener on Star Icon For Un Mark Favorite
        holder.mImageViewFavoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lecture taggedLecture = (Lecture) v.getTag();
                LectureManager.getInstance().markUnMarkFavorite(taggedLecture, false);
            }
        });
        if (!mOverflowMenuListener.show3dotPopupMenu()) {
            holder.mSettingsPopup.setVisibility(View.GONE);
        }

        if (!mOverflowMenuListener.showPlaybackProgress()) {
            holder.mCircularProgressBar.setVisibility(View.GONE);
        }

        if (mOverflowMenuListener.rearrangeAllowed()) {
            holder.mRearrangeView.setVisibility(View.VISIBLE);
        } else {
            holder.mRearrangeView.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return mLectures.size();
    }

    private void showPopupMenuV2(View view, boolean removeLectureAllowed) {
        selectedLecture = (Lecture) view.getTag();
        new PopUpClass().showLectureMenuPopupWindow(view, selectedLecture,removeLectureAllowed, new LectureMenuPopupWindowListener() {
            @Override
            public void download(Lecture lecture) {
                Toast.makeText(view.getContext(), R.string.started_download, Toast.LENGTH_SHORT)
                        .show();
                Analytics.DownloadAnalytics.lectureDownload(lecture);
                toggleDownload(view.getContext(), lecture.name, lecture.mediaUrl);
            }

            @Override
            public void removeDownload(Lecture lecture) {
                toggleDownload(view.getContext(), lecture.name, lecture.mediaUrl);
            }

            @Override
            public void favorite(Lecture lecture) {
                LectureManager.getInstance().markUnMarkFavorite(lecture, true);
            }

            @Override
            public void removeFavorite(Lecture lecture) {
                LectureManager.getInstance().markUnMarkFavorite(lecture, false);
            }

            @Override
            public void playlist(Lecture lecture) {
                PlaylistTypeChoiceSheet.Companion.newInstance(lecture.id).show(
                        ((LectureListBaseFragment) mOverflowMenuListener).getChildFragmentManager(),
                        "dialog");
            }

            @Override
            public void complete(Lecture lecture) {
                LectureManager.getInstance().markUnMarkComplete(lecture, true);
            }

            @Override
            public void removeComplete(Lecture lecture) {
                LectureManager.getInstance().markUnMarkComplete(lecture, false);
            }

            @Override
            public void share(Lecture lecture) {
                mOverflowMenuListener.shareLecture(lecture);
            }

            @Override
            public void removeLecture(Lecture lecture) {
                if (mOverflowMenuListener.getPlaylistObject()!=null){
                    PlaylistsManager.INSTANCE.removeFromPlaylist(lecture.id,mOverflowMenuListener.getPlaylistObject());
                    mLectures.remove(lecture);
                    notifyDataSetChanged();
                }
            }
        });

    }

    public List<Lecture> getLectures() {
        return mLectures;
    }

    public void setLectures(List<Lecture> lectures) {
        mLectures = lectures;
    }

    private void playLecture(@NonNull Lecture lecture) {
        PlayerManager.getInstance().preparePlayer(mLectures, true);
        PlayerManager.getInstance().play(lecture);
    }
    /*private void playVideo(@NonNull Lecture lecture) {
        VideoPlayerManagerV3.getSharedInstance(mContext).preparePlayer(mLectures,false);
        VideoPlayerManagerV3.getSharedInstance(mContext).play(lecture);

    }*/
    private void playVideo(@NonNull Lecture lecture) {
        VideoPlayerManagerV4.getSharedInstance(mContext).setPlaylist(mLectures,mLectures.indexOf(lecture));

    }

    private boolean isDownloaded(@NonNull Context context, @NonNull String mediaUrl) {
        return ((BvksApplication) ((Activity) context).getApplication()).getDownloadTracker().isDownloaded(Uri.parse(mediaUrl));
    }


    private boolean isDownloading(@NonNull Context context, @NonNull String mediaUrl) {
        return ((BvksApplication) ((Activity) context).getApplication()).getDownloadTracker().isDownloading(Uri.parse(mediaUrl));
    }

    private void toggleDownload(@NonNull Context context, @NonNull String title, @NonNull String mediaUrl) {
        RenderersFactory renderersFactory =
                ((BvksApplication) ((Activity) context).getApplication()).buildRenderersFactory();
        ((BvksApplication) ((Activity) context).getApplication()).getDownloadTracker().toggleDownload(
                title, Uri.parse(mediaUrl), mediaUrl.substring(mediaUrl.lastIndexOf(".") + 1), renderersFactory);
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mThumbnail;
        public final ImageView mPeakMeter1;
        public final ImageView mPeakMeter2;
        public final ImageView mPeakMeter3;
        public final TextView mTitle;
        public final TextView mPlace;
        public final TextView mVerse;
        public final TextView mLength;
        public final TextView mDate;
        public final RelativeLayout mCircularProgressBar;
        public final ImageButton mSettingsPopup;
        public final ImageView mImageViewDownloadIcon;
        public final ImageView mImageViewFavoriteIcon;
        public final ProgressBar mImageViewDownloadImage;
        public final ImageView ivLectureComplete;
        public final ProgressBar mProgressBar;
        public final TextView mProgressText;
        public final ImageView mRearrangeView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mThumbnail = view.findViewById(R.id.thumbnail);
            mPeakMeter1 = view.findViewById(R.id.peak_meter_1);
            mPeakMeter2 = view.findViewById(R.id.peak_meter_2);
            mPeakMeter3 = view.findViewById(R.id.peak_meter_3);
            mTitle = view.findViewById(R.id.title);
            mPlace = view.findViewById(R.id.place);
            mVerse = view.findViewById(R.id.verse);
            mLength = view.findViewById(R.id.length);
            mDate = view.findViewById(R.id.date);
            mCircularProgressBar = view.findViewById(R.id.playbackBarLayout);
            mSettingsPopup = view.findViewById(R.id.settings_popup);
            mImageViewDownloadIcon = view.findViewById(R.id.image_view_downloaded);
            mImageViewFavoriteIcon = view.findViewById(R.id.image_view_markedFavorite);
            mImageViewDownloadImage = view.findViewById(R.id.image_view_download_progress);
            mProgressBar = view.findViewById(R.id.circularPlaybackBar);
            mProgressText = view.findViewById(R.id.textViewPlaybackPercentage);
            ivLectureComplete = view.findViewById(R.id.iv_lecture_complete);
            mRearrangeView = view.findViewById(R.id.rearrange_view);
        }
    }
}