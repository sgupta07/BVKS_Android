package com.iskcon.bvks.ui.filter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.common.collect.Lists;
import com.iskcon.bvks.manager.LectureManager;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.util.PrefUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FilterHelper {

    private Context mContext;

    private HashMap<String, List<String>> mFilterItemMap;
    private HashMap<String, List<String>> mSelectedItemMap;
    private String mFilterSelection;

    private static List<String> MONTHS_LIST = new ArrayList<>();

    static {
        MONTHS_LIST.add("0");
        MONTHS_LIST.add("1");
        MONTHS_LIST.add("2");
        MONTHS_LIST.add("3");
        MONTHS_LIST.add("4");
        MONTHS_LIST.add("5");
        MONTHS_LIST.add("6");
        MONTHS_LIST.add("7");
        MONTHS_LIST.add("8");
        MONTHS_LIST.add("9");
        MONTHS_LIST.add("10");
        MONTHS_LIST.add("11");
    }

    public FilterHelper(@NonNull Context context, @NonNull String fragName) {
        mContext = context;
        mFilterSelection = fragName;
        mFilterItemMap = new HashMap<>();
        if(mFilterSelection == "FavoriteListFragment")
            mSelectedItemMap = (HashMap) PrefUtil.getFromPrefs(mContext, PrefUtil.FAVORITE_FILTER_ITEMS,
                    new HashMap<>());
        else
        mSelectedItemMap = (HashMap) PrefUtil.getFromPrefs(mContext, PrefUtil.FILTER_ITEMS, new HashMap<>());
    }

    public HashMap<String, List<String>> getSelectedItemMap() {
        return new HashMap<>(mSelectedItemMap);
    }

    public void saveFilteredItems() {
        if (mFilterSelection.equals("FavoriteListFragment"))
            PrefUtil.saveToPrefs(mContext, PrefUtil.FAVORITE_FILTER_ITEMS, mSelectedItemMap);
        else
            PrefUtil.saveToPrefs(mContext, PrefUtil.FILTER_ITEMS, mSelectedItemMap);
    }

    public void clear(boolean removeSavedFilteredIfAny) {
        mSelectedItemMap.clear();
        if (removeSavedFilteredIfAny) {
            if(mFilterSelection.equals("FavoriteListFragment"))
                PrefUtil.removeFromPrefs(mContext, PrefUtil.FAVORITE_FILTER_ITEMS);
            else
            PrefUtil.removeFromPrefs(mContext, PrefUtil.FILTER_ITEMS);
        }
    }

    @WorkerThread
    public List<String> readFilterData(@NonNull Filter filter) {
        List<String> entries = mFilterItemMap.get(filter.getTitle());
        if (entries != null) {
            return entries;
        }

        List<Lecture> lectures = LectureManager.getInstance().getLectures();
        Set<String> distinctEntries = new TreeSet<>();
        switch (filter) {
            case LANGUAGES:
                for (Lecture lecture : lectures) {
                    if (TextUtils.isEmpty(lecture.language)) {
                        continue;
                    }
                    distinctEntries.add(lecture.language);
                }
                entries = Lists.newArrayList(distinctEntries);
                break;
            case CATEGORIES:
                for (Lecture lecture : lectures) {
                    if (TextUtils.isEmpty(lecture.category)) {
                        continue;
                    }
                    distinctEntries.add(lecture.category);
                }
                entries = Lists.newArrayList(distinctEntries);
                break;
            case TRANSLATION:
                for (Lecture lecture : lectures) {
                    if (lecture.translation == null) {
                        continue;
                    }
                    for (String translation : lecture.translation) {
                        if (TextUtils.isEmpty(translation)) {
                            continue;
                        }
                        distinctEntries.add(translation);
                    }
                }
                entries = Lists.newArrayList(distinctEntries);
                break;
            case COUNTRIES:
                for (Lecture lecture : lectures) {
                    if (TextUtils.isEmpty(lecture.country)) {
                        continue;
                    }
                    distinctEntries.add(lecture.country);
                }
                entries = Lists.newArrayList(distinctEntries);
                break;
            case PLACE:
                for (Lecture lecture : lectures) {
                    if (TextUtils.isEmpty(lecture.place)) {
                        continue;
                    }
                    distinctEntries.add(lecture.place);
                }
                entries = Lists.newArrayList(distinctEntries);
                break;
            case YEARS:
                Calendar calendar = new GregorianCalendar();
                for (Lecture lecture : lectures) {
                    calendar.setTime(lecture.date);
                    distinctEntries.add(String.valueOf(calendar.get(Calendar.YEAR)));
                }
                entries = Lists.newArrayList(distinctEntries);
                break;
            case MONTH:
                entries = MONTHS_LIST;
                break;
            default:
                throw new IllegalArgumentException("Unknown item type " + filter);
        }
        mFilterItemMap.put(filter.getTitle(), entries);

        return entries;
    }

    public void sortFilterData(@NonNull Filter filter, List<String> data) {
        switch (filter) {
            case LANGUAGES:
            case COUNTRIES:
            case PLACE:
            case CATEGORIES:
            case TRANSLATION:
                Collections.sort(data, selectedFilterComparator(mSelectedItemMap.get(filter.getTitle()), false));
                break;
            case MONTH:
                Collections.sort(data, selectedMonthComparator(mSelectedItemMap.get(filter.getTitle())));
                break;
            case YEARS:
                Collections.sort(data, selectedFilterComparator(mSelectedItemMap.get(filter.getTitle()), true));
                break;
            default:
                throw new IllegalArgumentException("Unknown item type " + filter);
        }
    }

    public boolean isSelected(@NonNull Filter filter, @NonNull String text) {
        List<String> data = mSelectedItemMap.get(filter.getTitle());
        return data != null && data.contains(text);
    }

    public void filterDataSelected(@NonNull Filter filter, String data) {
        List<String> selectedData = mSelectedItemMap.get(filter.getTitle());
        if (selectedData == null) {
            selectedData = new ArrayList<>();
        }
        selectedData.add(data);
        mSelectedItemMap.put(filter.getTitle(), selectedData);
    }

    public void filterDataUnselected(@NonNull Filter filter, String data) {
        List<String> selectedData = mSelectedItemMap.get(filter.getTitle());
        selectedData.remove(data);
        if (selectedData.isEmpty()) {
            mSelectedItemMap.remove(filter.getTitle());
        } else {
            mSelectedItemMap.put(filter.getTitle(), selectedData);
        }
    }

    private static Comparator<String> selectedFilterComparator(@Nullable List<String> selectedFilterData,
                                                               boolean reverseOrder) {
        return (data1, data2) -> {
            if (selectedFilterData != null && selectedFilterData.contains(data1)) {
                return selectedFilterData.contains(data2) ?
                        selectedFilterData.indexOf(data1) - selectedFilterData.indexOf(data2) : -1;
            }

            if (selectedFilterData != null && selectedFilterData.contains(data2)) {
                return 1;
            }
            return !reverseOrder ? data1.compareTo(data2) : data2.compareTo(data1);
        };
    }

    private static Comparator<String> selectedMonthComparator(@Nullable List<String> selectedFilterData) {
        return (data1, data2) -> {
            if (selectedFilterData != null && selectedFilterData.contains(data1)) {
                return selectedFilterData.contains(data2) ?
                        selectedFilterData.indexOf(data1) - selectedFilterData.indexOf(data2) : -1;
            }

            if (selectedFilterData != null && selectedFilterData.contains(data2)) {
                return 1;
            }
            return Integer.valueOf(data1).compareTo(Integer.valueOf(data2));
        };
    }
}
