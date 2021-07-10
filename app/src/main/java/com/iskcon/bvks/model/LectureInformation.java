package com.iskcon.bvks.model;

import java.io.Serializable;

/**
 * @AUTHOR Amandeep Singh
 * @date 05/02/2021
 */
public class LectureInformation implements Serializable {
    public String documentId;
    public String documentPath;
    public long id;
    public long creationTimestamp;
    public long lastModifiedTimestamp;
    public long totallength;
    public long lastPlayedPoint=0L;
    public int totalPlayedNo;
    public int totalPlayedTime;
    public int downloadPlace;
    public int favouritePlace;
    public FIRResources android;
    public FIRResources ios;
    public boolean isCompleted;
    public boolean isFavourite;
    public boolean isDownloaded;
    public boolean isInPrivateList;
    public boolean isInPublicList;
    public String[] privateListIDs;
    public String[] publicListIDs;

    public LectureInformation() {
    }

    public LectureInformation(String documentId, String documentPath, int id, int creationTimestamp, int lastModifiedTimestamp, int totalLength, int lastPlayedPoint, int totalPlayedNo, int totalPlayedTime, int downloadPlace, int favouritePlace, FIRResources android, FIRResources ios, boolean isCompleted, boolean isFavourite, boolean isDownloaded, boolean isInPrivateList, boolean isInPublicList, String[] privateListIDs, String[] publicListIDs) {
        this.documentId = documentId;
        this.documentPath = documentPath;
        this.id = id;
        this.creationTimestamp = creationTimestamp;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
        this.totallength = totalLength;
        this.lastPlayedPoint = lastPlayedPoint;
        this.totalPlayedNo = totalPlayedNo;
        this.totalPlayedTime = totalPlayedTime;
        this.downloadPlace = downloadPlace;
        this.favouritePlace = favouritePlace;
        this.android = android;
        this.ios = ios;
        this.isCompleted = isCompleted;
        this.isFavourite = isFavourite;
        this.isDownloaded = isDownloaded;
        this.isInPrivateList = isInPrivateList;
        this.isInPublicList = isInPublicList;
        this.privateListIDs = privateListIDs;
        this.publicListIDs = publicListIDs;
    }

    public static class FIRResources implements Serializable {
        public FIRMedia audios;
        public FIRMedia videos;

        public FIRResources() {
        }

        public FIRResources(FIRMedia audios, FIRMedia videos) {
            this.audios = audios;
            this.videos = videos;
        }

        public static class FIRMedia implements Serializable {
            public int downloads;
            public String url;

            public FIRMedia() {
            }

            public FIRMedia(int downloads, String url) {
                this.downloads = downloads;
                this.url = url;
            }
        }

    }

    public String toStringInformation() {
        return "id-"+id
                +"-creationTimestamp-"+creationTimestamp
                +"-lastModifiedTimestamp-"+lastModifiedTimestamp
                +"-totallength-"+totallength
                +"-isCompleted-"+isCompleted
                +"-isFavourite-"+isFavourite
                +"-isDownloaded-"+isDownloaded
                +"-totalPlayedNo-"+totalPlayedNo
                +"-totalPlayedTime-"+totalPlayedTime
                +"-lastPlayedPoint-"+lastPlayedPoint;
    }
}
