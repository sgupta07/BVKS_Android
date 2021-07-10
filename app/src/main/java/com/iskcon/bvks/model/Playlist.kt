package com.iskcon.bvks.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class Playlist(
        var creationTime: Long = System.currentTimeMillis(),
        var description: String? = "",
        var docPath: String? = "",
        var lastUpdate: Long,
        var lectureCount: Int,
        var lectureIds: List<Long>?,
        var lecturesCategory: String?,
        var listID: String? = "",
        var listType: String? = "",
        var thumbnail: String? = "",
        var title: String? = "",
        var authorEmail: String? = ""

):Serializable{
    constructor() : this(System.currentTimeMillis(),
            "", "", System.currentTimeMillis(), 0,
    listOf<Long>(), "", "", "", "", "","")
}