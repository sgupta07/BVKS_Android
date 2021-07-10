package com.iskcon.bvks.listeners;

public interface VideoPlayerCallBacks {
    void callbackObserver(Object obj);
    public interface playerCallBack {
        void onItemClickOnItem(Integer albumId);
        void onPlayingEnd();
    }
}