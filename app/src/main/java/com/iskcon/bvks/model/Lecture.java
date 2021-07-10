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

package com.iskcon.bvks.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Lecture implements Serializable {
    public long id;
    public String name;
    public List<String> title;
    public String language;
    public String category;
    public String mediaUrl;
    public String thumbnailUrl;
    public String place;
    public String country;
    public Date date;
    public List<String> translation;
    public String verse;
    public long mediaLength;
    public List<String> searchList;
    public List<String> tags;
    public String videoLink;
    public int globlePlayCount;
    public LectureInformation information=null;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Lecture other = (Lecture) obj;
        return name.equals(other.name) && mediaUrl.equals(other.mediaUrl);
    }

    @Override
    public String toString() {
        return name;
    }



}
