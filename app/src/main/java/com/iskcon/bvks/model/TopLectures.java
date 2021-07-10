package com.iskcon.bvks.model;

import java.util.List;

/**
 * @AUTHOR Amandeep Singh
 * @date 05/03/2021
 */
public class TopLectures {
    public String documentId;
    public String documentPath;
    public long creationTimestamp;
    public long lastModifiedTimestamp;
    public int audioPlayedTime;
    public int videoPlayedTime;
    public List<String> playedBy;
    public List<Long> playedIds;
    public Day createdDay;

    public TopLectures() {
    }

    public static class Day {
        public int day, month, year;

        public Day() {
        }

    }
}
