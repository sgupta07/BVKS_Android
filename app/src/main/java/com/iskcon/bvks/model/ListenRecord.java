package com.iskcon.bvks.model;

import com.iskcon.bvks.util.Utils;

import java.util.Date;
import java.util.List;

public class ListenRecord {

    public int audioListen;
    public long creationTimestamp;
    public DateRecord dateOfRecord;
    public String documentId;
    public String documentPath;
    public long lastModifiedTimestamp;
    public int videoListen;
    public Listened listenDetails;
    public List<Long> playedIds;

    public ListenRecord() {
    }

    public Date getDate() {
        return Utils.getDateFromMillis(creationTimestamp);
    }

    public static class DateRecord {
        public int day, month, year;

        public DateRecord() {
        }

    }


    public static class Listened {

        public int BG;
        public int CC;
        public int SB;
        public int Seminars;
        public int VSN;
        public int others;

        public Listened() {
        }

    }
}