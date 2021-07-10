package com.iskcon.bvks.ui.lectures;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.iskcon.bvks.R;
import com.iskcon.bvks.ui.download.DownloadsFragment;
import com.iskcon.bvks.ui.favorites.FavoritesListFragment;
import com.iskcon.bvks.ui.home.HomeFragmentV2;
import com.iskcon.bvks.ui.playlist.PlaylistFragmentV3;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class LecturesPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.menu_home, R.string.menu_playlist,
            R.string.menu_lectures, R.string.menu_downloads, R.string.menu_favorites};
    private final Context mContext;
    private List<Fragment> mFragmentList;

    @SuppressLint("WrongConstant")
    public LecturesPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        mFragmentList = new ArrayList<>(TAB_TITLES.length);
        mFragmentList.add(0, new LectureListFragment());
        mFragmentList.add(1, new PlaylistFragmentV3());
        mFragmentList.add(2, new HomeFragmentV2());
        mFragmentList.add(3, new DownloadsFragment());
        mFragmentList.add(4, new FavoritesListFragment());
    }

    @Override
    public Fragment getItem(int position) {
        if(position > 4 || position <0){
            throw new IllegalArgumentException("Incorrect position of view pager");
        }else{
            return mFragmentList.get(position);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
//        return mContext.getResources().getString(TAB_TITLES[position]);
        return "";
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    @NonNull
    public List<Fragment> getFragments() {
        return mFragmentList;
    }

    @Nullable
    public Fragment getFragment(int position) {
        return mFragmentList.get(position);
    }
}