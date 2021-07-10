package com.iskcon.bvks.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.iskcon.bvks.R;
import com.iskcon.bvks.base.BvksApplication;
import com.iskcon.bvks.listeners.LectureMenuPopupWindowListener;
import com.iskcon.bvks.model.Lecture;

public class PopUpClass {

    //PopupWindow display method

    public void showLectureMenuPopupWindow(final View view, Lecture lecture, Boolean removeLectureAllowed, LectureMenuPopupWindowListener listener) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_popup_lecture_menu, null);


        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setElevation(5.0f);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        //Set the location of the window on the screen
//        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupWindow.showAsDropDown(
                view,
                0, 0,
                Gravity.START
        );

        //Initialize the elements of our window, install the handler
        TextView tvDownload = (TextView) popupView.findViewById(R.id.tv_download);
        TextView tvFavorite = (TextView) popupView.findViewById(R.id.tv_favorite);
        TextView tvPlaylist = (TextView) popupView.findViewById(R.id.tv_playlist);
        TextView tvComplete = (TextView) popupView.findViewById(R.id.tv_complete);
        TextView tvRemoveLecture = (TextView) popupView.findViewById(R.id.tv_remove_lecture);
        TextView tvShare = (TextView) popupView.findViewById(R.id.tv_share);

        if (removeLectureAllowed) {
            tvRemoveLecture.setVisibility(View.VISIBLE);
        } else {
            tvRemoveLecture.setVisibility(View.GONE);

        }
        if (isDownloaded(view.getContext(), lecture.mediaUrl)) {
            //DOWNLOADED
            tvDownload.setText("Delete from Downloads");
        } else {
            //NOT DOWNLOADED
            tvDownload.setText("Download");
        }

        if (lecture != null && lecture.information != null && lecture.information.isFavourite) {
            //FAVORITE
            tvFavorite.setText("Remove from Favorites");
        } else {
            //NOT FAVORITE
            tvFavorite.setText("Mark as Favorite");
        }
        if (lecture != null && lecture.information != null && lecture.information.isCompleted) {
            //COMPLETED
            tvComplete.setText("Reset progress");
        } else {
            //NOT COMPLETED
            tvComplete.setText("Mark as heard");
        }

        tvDownload.setOnClickListener(v -> {
            if (isDownloaded(view.getContext(), lecture.mediaUrl)) {
                //DOWNLOADED
                listener.removeDownload(lecture);
            } else {
                //NOT DOWNLOADED
                listener.download(lecture);
            }
            popupWindow.dismiss();
        });
        tvFavorite.setOnClickListener(v -> {
            if (lecture != null && lecture.information != null && lecture.information.isFavourite) {
                //FAVORITE
                listener.removeFavorite(lecture);
            } else {
                //NOT FAVORITE
                listener.favorite(lecture);
            }
            popupWindow.dismiss();
        });
        tvPlaylist.setOnClickListener(v -> {
            listener.playlist(lecture);
            popupWindow.dismiss();
        });
        tvComplete.setOnClickListener(v -> {
            if (lecture != null && lecture.information != null && lecture.information.isCompleted) {
                //COMPLETED
                listener.removeComplete(lecture);
            } else {
                //NOT COMPLETED
                listener.complete(lecture);
            }
            popupWindow.dismiss();
        });

        tvRemoveLecture.setOnClickListener(v -> {
            listener.removeLecture(lecture);
            popupWindow.dismiss();
        });
        tvShare.setOnClickListener(v -> {
            listener.share(lecture);
            popupWindow.dismiss();
        });

        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private boolean isDownloaded(@NonNull Context context, @NonNull String mediaUrl) {
        return ((BvksApplication) ((Activity) context).getApplication()).getDownloadTracker().isDownloaded(Uri.parse(mediaUrl));
    }


}