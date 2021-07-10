package com.iskcon.bvks.ui.popularlecture;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.iskcon.bvks.R;
import com.iskcon.bvks.base.BvksApplication;
import com.iskcon.bvks.listeners.FavoriteListener;
import com.iskcon.bvks.listeners.LectureListener;
import com.iskcon.bvks.listeners.OnListItemInteractionListener;
import com.iskcon.bvks.listeners.PopularLectureListener;
import com.iskcon.bvks.manager.PopularTopLectureManager;
import com.iskcon.bvks.manager.PopularTopLectureUtils;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.model.Playlist;
import com.iskcon.bvks.model.TopLectures;
import com.iskcon.bvks.ui.filter.Filter;
import com.iskcon.bvks.ui.filter.FilterActivity;
import com.iskcon.bvks.ui.lectures.LectureListBaseFragment;
import com.iskcon.bvks.ui.videoplayer.VideoPlayerActivity;
import com.iskcon.bvks.ui.videoplayer.VideoPlayerActivityV4;
import com.iskcon.bvks.util.Constants;
import com.iskcon.bvks.util.DownloadTracker;
import com.iskcon.bvks.util.PrefUtil;
import com.iskcon.bvks.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.iskcon.bvks.base.BvksApplication.DEBUG;
import static com.iskcon.bvks.ui.filter.FilterActivity.FILTER_INTENT_EXTRA;

/**
 * A fragment representing a list of Items.
 */
public class PopularLectureListFragment extends LectureListBaseFragment implements OnListItemInteractionListener,
        DownloadTracker.Listener,
        LectureListener, FavoriteListener {

    private static final String TAG = PopularLectureListFragment.class.getSimpleName();

    private LinearLayoutManager mLayoutManager;
    private PopularLectureAdapter mAdapter;
    private String mSearchString;
    private List<Lecture> mLectures;
    private HashMap<String, List<String>> mSelectedItemMap;
    private PopularLectureViewModel mLectureModel;
    private boolean isVideo = false;
    private Switch mSwitchVideo;
    private TextView mTvSwitchStatus;
    private int mSelectedSortPosition = 0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PopularLectureListFragment() {

    }

    private  List<Lecture> getSortedLectures(@NonNull List<Lecture> lectureList,
                                                   HashMap<String, List<String>> selectedItemMap,
                                                   String searchString,
                                                   boolean isVideo) {
        Calendar calendar = new GregorianCalendar();
        Predicate<Lecture> mYearPredicate = lecture -> {
            List<String> years = selectedItemMap.get(Filter.YEARS.getTitle());
            if (years == null) {
                return true;
            }
            calendar.setTime(lecture.date);
            return years.contains(String.valueOf(calendar.get(Calendar.YEAR)));
        };
        Predicate<Lecture> mLanguagePredicate = lecture -> {
            List<String> languages = selectedItemMap.get(Filter.LANGUAGES.getTitle());
            if (languages == null) {
                return true;
            }
            return languages.contains(lecture.language);
        };
        Predicate<Lecture> mMonthPredicate = lecture -> {
            List<String> months = selectedItemMap.get(Filter.MONTH.getTitle());
            if (months == null) {
                return true;
            }
            calendar.setTime(lecture.date);
            return months.contains(String.valueOf(calendar.get(Calendar.MONTH)));
        };
        Predicate<Lecture> mCountriesPredicate = lecture -> {
            List<String> countries = selectedItemMap.get(Filter.COUNTRIES.getTitle());
            if (countries == null) {
                return true;
            }
            return countries.contains(lecture.country);
        };
        Predicate<Lecture> mPlacesPredicate = lecture -> {
            List<String> places = selectedItemMap.get(Filter.PLACE.getTitle());
            if (places == null) {
                return true;
            }
            return places.contains(lecture.place);
        };
        Predicate<Lecture> mCategoriesPredicate = lecture -> {
            List<String> categories = selectedItemMap.get(Filter.CATEGORIES.getTitle());
            if (categories == null) {
                return true;
            }
            return categories.contains(lecture.category);
        };
        Predicate<Lecture> mTranslationsPredicate = lecture -> {
            List<String> translations = selectedItemMap.get(Filter.TRANSLATION.getTitle());
            if (translations == null) {
                return true;
            }
            if (lecture.translation == null) {
                return false;
            }
            return !Collections.disjoint(lecture.translation, translations);
        };
        Predicate<Lecture> mSearchPredicate = lecture -> {
            if (TextUtils.isEmpty(searchString)) {
                return true;
            }

            if (searchString.startsWith("VSN") || searchString.startsWith("vsn") ||
                    searchString.startsWith("SB") || searchString.startsWith("sb") ||
                    searchString.startsWith("BG") || searchString.startsWith("bg") ||
                    searchString.startsWith("CC") || searchString.startsWith("cc")) {
                return !lecture.searchList.isEmpty() && lecture.searchList.stream().anyMatch(s -> s.matches("(?i).*" + searchString + ".*"));
            }

            if (!lecture.tags.isEmpty() && lecture.tags.stream().filter(s -> s.equalsIgnoreCase(searchString)).findAny().isPresent()) {
                return true;
            }

            String[] searchList = searchString.split("\\s+");
            for (String searchWord : searchList) {
                if (lecture.title.stream().anyMatch(s -> s.matches("(?i).*" + searchWord + ".*"))) {
                    return true;
                }
            }
            return false;
        };
        //Video Predicate
        Predicate<Lecture> mVideoPredicate = lecture -> {
            if (isVideo) {
                if (!TextUtils.isEmpty(lecture.videoLink)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }

        };

        List<Lecture> result = new ArrayList<>(Collections2.filter(lectureList, Predicates.and(
                mYearPredicate, mLanguagePredicate, mMonthPredicate, mCountriesPredicate, mPlacesPredicate, mCategoriesPredicate, mTranslationsPredicate, mSearchPredicate, mVideoPredicate)));
        if (DEBUG) {
            Log.d(TAG, "Lectures sorted with given criteria");
        }


        return result;
    }

    @Override
    public void onDownloadsChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void lecturesUpdated(@NonNull List<Lecture> lectureList,boolean isLoaderHide) {
        Log.d(TAG, "========================lecturesUpdated=========================>");
        try {
            mLectureModel.getLectureList().setValue(getSortedLectures(lectureList, mSelectedItemMap, mSearchString, isVideo));
            sort(mSelectedSortPosition);
        }catch (Exception ex){

        }
    }

    @Override
    public void favoritesUpdated(@NonNull List<Lecture> lectureList) {
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean isVideoOption() {
        return isVideo;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        HashMap<String, List<String>> filteredData;
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == 0) {
                    filteredData = ((HashMap) data.getSerializableExtra(FILTER_INTENT_EXTRA));
                } else {
                    filteredData = new HashMap<>();
                }
                break;
            default:
                filteredData = new HashMap<>();
                break;
        }
        if (!filteredData.equals(mSelectedItemMap)) {
            mSelectedItemMap = filteredData;
            getActivity().invalidateOptionsMenu();
            mLectureModel.getLectureList().setValue(getSortedLectures(mLectures,
                    mSelectedItemMap, mSearchString, isVideo));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedItemMap = (HashMap) PrefUtil.getFromPrefs(getContext(), PrefUtil.FILTER_ITEMS,
                new HashMap<>());
        setHasOptionsMenu(true);

        // Get the ViewModel.
        mLectureModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(PopularLectureViewModel.class);

        // Create the observer which updates the UI.
        Observer<List<Lecture>> lectureObserver = lectureList -> {
            if (DEBUG) {
                Log.d(TAG, "Lectures updated");
            }
            mAdapter.setLectures(lectureList);
            mAdapter.notifyDataSetChanged();
        };
        Observer<Boolean> videoObserver = video -> {
            isVideo = video;
            mLectureModel.getLectureList().setValue(getSortedLectures(mLectures,
                    mSelectedItemMap, mSearchString, isVideo));

        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mLectureModel.getLectureList().observe(this, lectureObserver);
        mLectureModel.getIsVideo().observe(this, videoObserver);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popular_lecture, container, false);

        // Set the adapter
        Context context = view.getContext();
        view.findViewById(R.id.empty_view).setVisibility(View.GONE);
        RecyclerView recyclerView = view.findViewById(R.id.listLectures);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);

        PopularTopLectureManager.getInstance().getPopularTopLecture(PopularTopLectureManager.RetrieveBy.week, new PopularLectureListener() {
            @Override
            public void lectureUpdated(@NonNull List<TopLectures> topLecturesList, PopularTopLectureManager.RetrieveBy statsType) {

                mLectures = PopularTopLectureUtils.INSTANCE.calculatePopularLecture(topLecturesList);
                mAdapter = new PopularLectureAdapter(getContext(),PopularLectureListFragment.this, getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo));
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onError(String msg) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mTvSwitchStatus = (TextView) view.findViewById(R.id.tv_switch_status);
        //add listener to switch video button
        mSwitchVideo = (Switch) view.findViewById(R.id.switch_video);
        mSwitchVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLectureModel.getIsVideo().postValue(isChecked);
                mTvSwitchStatus.setText(isChecked ? "Video" : "Audio");
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        ((BvksApplication) getActivity().getApplication()).getDownloadTracker().addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BvksApplication) getActivity().getApplication()).getDownloadTracker().removeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() > 2) {
                    mSearchString = s;
                } else {
                    mSearchString = null;
                }
                new Thread(() -> mLectureModel.getLectureList().postValue(getSortedLectures(
                        mLectures, mSelectedItemMap, mSearchString, isVideo))).start();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        int size = 0;
        for (List<String> selectedItems : mSelectedItemMap.values()) {
            size += selectedItems.size();
        }
        @DrawableRes int icon;
        switch (size) {
            case 0:
                icon = R.drawable.ic_filter_list_white_24dp;
                break;
            case 1:
                icon = R.drawable.ic_filter_1_black_24dp;
                break;
            case 2:
                icon = R.drawable.ic_filter_2_black_24dp;
                break;
            case 3:
                icon = R.drawable.ic_filter_3_black_24dp;
                break;
            case 4:
                icon = R.drawable.ic_filter_4_black_24dp;
                break;
            case 5:
                icon = R.drawable.ic_filter_5_black_24dp;
                break;
            case 6:
                icon = R.drawable.ic_filter_6_black_24dp;
                break;
            case 7:
                icon = R.drawable.ic_filter_7_black_24dp;
                break;
            case 8:
                icon = R.drawable.ic_filter_8_black_24dp;
                break;
            case 9:
                icon = R.drawable.ic_filter_9_black_24dp;
                break;
            default:
                icon = R.drawable.ic_filter_9_plus_black_24dp;
                break;
        }
        menu.findItem(R.id.action_filter_item).setIcon(icon);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_item:
                Intent intent = new Intent(getContext(), FilterActivity.class);
                intent.putExtra("fragNameForFilter", "LectureListFragment");
                startActivityForResult(intent, 0);
                return true;
            case R.id.action_sort_item:
                Utils.getInstance().showSortDropDown(getActivity(), "Sort", Constants.ARR_SORTING_POPULAR_LECTURE, mSelectedSortPosition, new Utils.DropDownClickListener() {
                    @Override
                    public void onSelect(int position, String option) {
                        mSelectedSortPosition = position;
                       sort(position);
                    }
                });
                return true;
            default:
                return false;
        }
    }

    private void sort(int position) {
        switch (position) {
            case 0:
                //None
                List<Lecture> lectureList0 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                mLectureModel.getLectureList().postValue(lectureList0);
                break;
            case 1:
                //"Duration: Low to High",
                List<Lecture> lectureList1 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                lectureList1.sort(Comparator.comparing(e -> e.mediaLength));
                mLectureModel.getLectureList().postValue(lectureList1);

                break;
            case 2:
                //"Duration: High to Low",
                List<Lecture> lectureList2 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                Comparator<Lecture> comparator2 = Comparator.comparing(e -> e.mediaLength);
                lectureList2.sort(comparator2.reversed());
                mLectureModel.getLectureList().postValue(lectureList2);
                break;
            case 3:
                //"Recording date: oldest first",
                List<Lecture> lectureList3 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                lectureList3.sort(Comparator.comparing(e -> e.date));
                mLectureModel.getLectureList().postValue(lectureList3);

                break;
            case 4:
                //"Recording date: latest first",
                List<Lecture> lectureList4 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                Comparator<Lecture> comparator4 = Comparator.comparing(e -> e.date);
                lectureList4.sort(comparator4.reversed());
                mLectureModel.getLectureList().postValue(lectureList4);
                break;
            case 5:
                //"Alphabetically: A->Z",
                List<Lecture> lectureList5 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                lectureList5.sort(Comparator.comparing(e -> e.name));
                mLectureModel.getLectureList().postValue(lectureList5);

                break;
            case 6:
                //"Alphabetically: Z->A",
                List<Lecture> lectureList6 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                Comparator<Lecture> comparator6 = Comparator.comparing(e -> e.name);
                lectureList6.sort(comparator6.reversed());
                mLectureModel.getLectureList().postValue(lectureList6);
                break;
            case 7:
                //"Popularity",
                break;
            case 8:
                //"Verse Number"
                List<Lecture> lectureList8 = getSortedLectures(mLectures, mSelectedItemMap, mSearchString, isVideo);
                lectureList8.sort(Comparator.comparing(e -> {
                    if (e.searchList != null && !e.searchList.isEmpty()) {
                        return e.searchList.get(0);
                    } else {
                        return "";
                    }
                }));
                mLectureModel.getLectureList().postValue(lectureList8);
                break;
        }
    } 

    @Override
    public void playLecture(Lecture lecture,boolean isVideoMode) {
        if (isVideoMode) {
            Intent intent = new Intent(getContext(), VideoPlayerActivityV4.class);
            intent.putExtra("Lecture", lecture);
            startActivity(intent);
        } else {
            navController.navigate(R.id.action_b_to_a);
        }
    }
    private NavController navController;
    @Override
    public boolean downloadAllowed() {
        return true;
    }

    @Override
    public boolean favoritesAllowed() {
        return true;
    }

    @Override
    public boolean deleteFavoritesAllowed() {
        return false;
    }

    @Override
    public boolean removeDownloadsAllowed() {
        return false;
    }

    @Override
    public boolean rearrangeAllowed() {
        return false;
    }

    @Override
    public boolean show3dotPopupMenu() {
        return true;
    }

    @Override
    public boolean showPlaybackProgress() {
        return true;
    }

    @Override
    public void shareLecture(Lecture lecture) {
        shareWithDeepLink(lecture);
    }

    @Override
    public boolean isRemoveLectureAllowed() {
        return false;
    }

    @Override
    public Playlist getPlaylistObject() {
        return null;
    }

    /**
     * Method to create dynamic link and share it on social platform
     */
    private void shareWithDeepLink(Lecture lecture) {
        String link = Constants.DEEP_LINK_URL + "?lectureId=" + lecture.id;
        String description = Utils.getInstance().createLectureDescriptionForShare(lecture);
        String thumbnailUrl = Utils.getInstance().getLectureThumbnailUrlForShare(lecture);
        DynamicLink longLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDynamicLinkDomain(Constants.DYNAMIC_LINK_DOMAIN)
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .setIosParameters(new DynamicLink.IosParameters.Builder("com.bhakti.bvks").setAppStoreId("1536451261").build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(lecture.name)
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
                        sendIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(shortLink));
                        startActivity(Intent.createChooser(sendIntent, "Share Lecture"));
                    } else {
                        Utils.getInstance().showToast(getContext(), "Something went wrong...");
                    }
                });
    }

    @Override
    public void playerStateChanged() {
        mAdapter.notifyDataSetChanged();
    }




}
