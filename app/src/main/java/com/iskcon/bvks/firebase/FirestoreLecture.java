/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iskcon.bvks.firebase;

import android.text.TextUtils;

import com.iskcon.bvks.model.Lecture;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.iskcon.bvks.base.BvksApplication.DEBUG;

public class FirestoreLecture {
    private static final String DAY = "day";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String MAIN = "main";
    private static final String TRANSLATIONS = "translations";
    private static final String COUNTRY = "country";
    private static final String AUDIOS = "audios";
    private static final String VIDEOS = "videos";
    private static final String URL = "url";
    private static final String VERSE = "verse";
    private static final String ADVANCED = "advanced";

    // Firebase properties
    public List<String> category;
    public Map<String, String> dateOfRecording;
    public Long id;
    public Map<String, Object> language;
    public Long length;
    public Map<String, String> location;
    public List<String> place;
    public Map<String, List<Map<String, Object>>> resources;
    public String thumbnail;
    public List<String> title;
    public int favorites;
    public Map<String, Object> legacyData;
    public Map<String, List<String>> search;
    public List<String> tags;
    public String videoLink;

    // Local properties
    private String categories;
    private String day, month, year;
    private String mainLanguage;
    private List<String> translations;
    private List<String> searchList;
    private String country;
    private String lecturePlace;
    private String url;
    private String name;
    private String verse;


    public FirestoreLecture() {
    }

    public String getVerse() {
        if (verse != null) {
            return verse;
        }

        if (legacyData != null && !legacyData.isEmpty()) {
            verse = (String) legacyData.get(VERSE);
        } else {
            verse = "";
        }

        return verse;
    }

    public long getLength() {
        return length != null ? length : 0;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getDate() {
        return getYear().concat("-").concat(getMonth()).concat("-").concat(getDay());
    }

    public String getCategories() {
        if (categories != null) {
            return categories;
        }

        if (category != null && !category.isEmpty()) {
            categories = category.get(0);
        } else {
            categories = "";
        }

        return categories;
    }

    public String getDay() {
        if (day != null) {
            return day;
        }

        if (dateOfRecording != null && !dateOfRecording.isEmpty()) {
            day = dateOfRecording.get(DAY);
        } else {
            day = "";
        }

        return day;
    }

    public String getMonth() {
        if (month != null) {
            return month;
        }

        if (dateOfRecording != null && !dateOfRecording.isEmpty()) {
            month = dateOfRecording.get(MONTH);
        } else {
            month = "";
        }

        return month;
    }

    public String getYear() {
        if (year != null) {
            return year;
        }

        if (dateOfRecording != null && !dateOfRecording.isEmpty()) {
            year = dateOfRecording.get(YEAR);
        } else {
            year = "";
        }

        return year;
    }

    public String getLanguage() {
        if (mainLanguage != null) {
            return mainLanguage;
        }

        if (language != null && !language.isEmpty()) {
            mainLanguage = (String) language.get(MAIN);
        } else {
            mainLanguage = "";
        }

        return mainLanguage;
    }

    public List<String> getTranslations() {
        if (translations != null) {
            return translations;
        }

        if (language != null && !language.isEmpty()) {
            translations = (List<String>) language.get(TRANSLATIONS);
        } else {
            translations = new ArrayList<>();
        }

        return translations;
    }

    public List<String> getSearchList() {
        if (searchList != null) {
            return searchList;
        }

        if (search != null && !search.isEmpty()) {
            searchList = search.get(ADVANCED);
        } else {
            searchList = new ArrayList<>();
        }

        return searchList;
    }

    public String getCountry() {
        if (country != null) {
            return country;
        }

        if (location != null && !location.isEmpty()) {
            country = location.get(COUNTRY);
        } else {
            country = "";
        }

        return country;
    }

    public String getPlace() {
        if (lecturePlace != null) {
            return lecturePlace;
        }

        if (place != null && !place.isEmpty()) {
            lecturePlace = place.get(0);
        } else {
            lecturePlace = "";
        }

        return lecturePlace;
    }

    public String getUrl() {
        if (url != null) {
            return url;
        }

        if (resources != null && !resources.isEmpty()) {
            List<Map<String, Object>> audios = resources.get(AUDIOS);

            if (audios != null && !audios.isEmpty()) {
                Map<String, Object> audioMap = audios.get(0);
                if (audioMap != null && !audioMap.isEmpty()) {
                    url = (String) audioMap.get(URL);
                }
            }

        } else {
            url = "";
        }

        return url;
    }

    public String getVideoUrl() {

        if (videoLink != null) {
            return videoLink;
        }

        List<Map<String, Object>> videos = resources.get(VIDEOS);
        if (videos != null && !videos.isEmpty()) {
            Map<String, Object> videoMap = videos.get(0);
            if (videoMap != null && !videoMap.isEmpty()) {
                videoLink = (String) videoMap.get(URL);
            }
        } else {
            videoLink = "";
        }

        return videoLink;
    }

    public String getName() {
        if (name != null) {
            return name;
        }
        if (title != null && !title.isEmpty()) {
            name = new String();
            for (int i = 0; i < title.size(); i++) {
                name += title.get(i);
                if (i != title.size() - 1) {
                    name += " ";
                }
            }
        } else {
            name = "";
        }
        return name;
    }

    public Lecture getLecture() {
        Lecture lecture = new Lecture();
        lecture.id = id != null ? id : 0L;
        lecture.name = getName();
        lecture.title = new ArrayList<>(this.title);
        lecture.language = getLanguage();
        lecture.category = getCategories();
        lecture.mediaUrl = getUrl();
        lecture.thumbnailUrl = getThumbnail();
        lecture.place = getPlace();
        lecture.country = getCountry();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            lecture.date = dateFormat.parse(getDate());
        } catch (ParseException e) {
            lecture.date = new Date(0);
        }
        lecture.translation = getTranslations();
        lecture.verse = getVerse();
        lecture.mediaLength = getLength();
        lecture.searchList = getSearchList();
        lecture.tags = new ArrayList<>(tags);
        lecture.videoLink=getVideoUrl();
        return lecture;
    }

    @Override
    public String toString() {
        if (!DEBUG) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Id = ").append(id).append(", ");
        if (category != null && !category.isEmpty()) {
            sb.append("Category = ").append(category.get(0)).append(", ");
        }
        if (dateOfRecording != null && !dateOfRecording.isEmpty()) {
            sb.append("Date of recording = ").append(dateOfRecording.get(DAY)).append("/").append(dateOfRecording.get(MONTH)).append("/").append(dateOfRecording.get(YEAR)).append(", ");
        }
        if (language != null && !language.isEmpty()) {
            sb.append("Language = ").append(language.get(MAIN)).append(", ");
            List<String> translations = (List<String>) language.get(TRANSLATIONS);
            if (translations != null && !translations.isEmpty()) {
                sb.append("Translations = ");
                for (String translation : translations) {
                    sb.append(translation);
                }
                sb.append(", ");
            }
        }
        sb.append("Length = " + length).append(", ");
        if (location != null && !location.isEmpty()) {
            sb.append("Country = ").append(location.get(COUNTRY)).append(", ");
        }
        if (place != null && !place.isEmpty()) {
            sb.append("Place = ").append(place.get(0)).append(", ");
        }
        if (resources != null && !resources.isEmpty()) {
            List<Map<String, Object>> audios = resources.get(AUDIOS);
            if (audios != null && !audios.isEmpty()) {
                Map<String, Object> audioMap = audios.get(0);
                if (audioMap != null && !audioMap.isEmpty()) {
                    sb.append("Resource = ").append(url).append(", ");
                }
            }
        }
        if (!TextUtils.isEmpty(thumbnail)) {
            sb.append("Thumbnail = ").append(thumbnail).append(", ");
        }
        if (title != null && !title.isEmpty()) {
            sb.append("Title = ");
            for (String title : title) {
                sb.append(title).append(" ");
            }
            sb.append(", ");
        }
        return sb.toString();
    }
}
