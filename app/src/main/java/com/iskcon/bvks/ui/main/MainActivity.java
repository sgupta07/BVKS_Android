package com.iskcon.bvks.ui.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.material.navigation.NavigationView;
import com.iskcon.bvks.R;

import com.iskcon.bvks.util.LectureDownloadService;
import com.iskcon.bvks.util.LectureNotificationService;
import com.iskcon.bvks.util.Utils;
import com.iskcon.bvks.util.VideoNotificationService;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawer;
    private NavController mNavController;
    private NavigationView mNavigationView;
    public static Boolean SHOW_LOADER=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setItemIconTintList(null);
        changeMenuTitleColor();
        View headerView = mNavigationView.getHeaderView(0);
        TextView subtitleView = headerView.findViewById(R.id.navSubtitle);
        subtitleView.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bvks.com")));
        });
//        Linkify.addLinks(subtitleView, Linkify.ALL);
        ImageView facebookView = headerView.findViewById(R.id.facebook_view);
        facebookView.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/872903862835384/")));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/BhaktiVikasaSwami/")));
            }
        });
        ImageView youtubeView = headerView.findViewById(R.id.youtube_view);
        youtubeView.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/user/bvksmediaministry")));
        });
        ImageView instagramView = headerView.findViewById(R.id.instagram_view);
        instagramView.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/hhbvs/")));
        });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_lectures,
                R.id.nav_history,
                R.id.nav_stats,
                R.id.nav_popular_lecture,
                R.id.nav_about,
                R.id.nav_share,
                R.id.nav_donate,
                R.id.nav_settings,
                R.id.nav_signout,
                R.id.nav_rate_us
        ).setDrawerLayout(mDrawer).build();
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, mNavController);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        // Start the download service if it should be running but it's not currently.
        // Starting the service in the foreground causes notification flicker if there is no scheduled
        // action. Starting it in the background throws an exception if the app is in the background too
        // (e.g. if device screen is locked).
        try {
            DownloadService.start(this, LectureDownloadService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(this, LectureDownloadService.class);
        }

        startService(new Intent(this, LectureNotificationService.class));
        startService(new Intent(this, VideoNotificationService.class));
    }

    private void changeMenuTitleColor() {
        Menu menu = mNavigationView.getMenu();
       /* MenuItem bedMenuItem = menu.findItem(R.id.nav_lectures);

        for (int i = 0, count = mNavigationView.getChildCount(); i < count; i++) {
            final View child = mNavigationView.getChildAt(i);
            int paddingDp = 50;
            float density = getResources().getDisplayMetrics().density;
            int paddingPixel = (int)(paddingDp * density);


            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(paddingPixel, 0, 0, 0);
            child.setLayoutParams(layoutParams);
           *//* if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }*//*
        }*/
       /* MenuItem tools= menu.findItem(R.id.submenu_1);
        SpannableString s = new SpannableString(tools.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceMenuTitle), 0, s.length(), 0);
        tools.setTitle(s);*/
//        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Utils.getInstance().showToast(this, "We have received your donation.");
        Checkout.clearUserData(this);

    }

    @Override
    public void onPaymentError(int i, String s) {
        Utils.getInstance().showToast(this, "Donation Error:-" + s);
    }


   /* public void playLecture(Lecture lecture, boolean isVideoMode) {
        ((LectureFragment) getForegroundFragment()).onLecturePlayed(lecture, isVideoMode);
    }

    public Fragment getForegroundFragment(){
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        return navHostFragment == null ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
    }*/
}
