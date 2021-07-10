package com.iskcon.bvks.manager

import com.iskcon.bvks.model.Lecture
import com.iskcon.bvks.model.TopLectures
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.set

/**
 * @AUTHOR Amandeep Singh
 * @date 05/03/2021
 */
object PopularTopLectureUtils {

    fun calculatePopularLecture(topObj: MutableList<TopLectures>):MutableList<Lecture> {
        val playedList  = topObj.flatMap { it.playedIds }
        val sortDetails: MutableMap<Long, Int> = HashMap()
        for (s in playedList) {
            var count = sortDetails[s]
            if (count == null) count = 0
            sortDetails[s] = count + 1
        }
        val topLectures = mutableListOf<Lecture>()
        sortDetails.forEach { (key, value) ->
            val first=LectureManager.getInstance().getLecture(key)
            val raw=first
            if (raw!=null){
                raw.globlePlayCount = value
                topLectures.add(raw)
            }

        }
        topLectures.sortByDescending { it.globlePlayCount }
        return topLectures
    }

    fun calculateTopLecture(topObj: MutableList<TopLectures>):List<Lecture> {
        val playedList  = topObj.flatMap { it.playedIds }
        val sortDetails: MutableMap<Long, Int> = HashMap()
        for (s in playedList) {
            var count = sortDetails[s]
            if (count == null) count = 0
            sortDetails[s] = count + 1
        }
        val topLectures = mutableListOf<Lecture>()
        sortDetails.forEach { (key, value) ->
            val first=LectureManager.getInstance().getLecture(key.toLong())
            val raw=first
            if (raw!=null){
                raw.globlePlayCount = value
                topLectures.add(raw)
            }
        }
        topLectures.sortBy { it.globlePlayCount }

        return topLectures.reversed().takeLast(10)
    }


}