package com.iskcon.bvks.util;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.iskcon.bvks.R;
import com.iskcon.bvks.listeners.DeletePlaylistPopUpMenuListener;
import com.iskcon.bvks.model.Playlist;

public class PopUpDeletePlaylist {

    //PopupWindow display method

    public void showLectureMenuPopupWindow(final View view, Playlist playlist, DeletePlaylistPopUpMenuListener listener) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_popup_delete_playlist, null);


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
        TextView tvDeletePlaylist = (TextView) popupView.findViewById(R.id.tv_delete_playlist);


        tvDeletePlaylist.setOnClickListener(v -> {
            listener.deletePlaylist(playlist);
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


}