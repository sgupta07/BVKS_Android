package com.iskcon.bvks.manager

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object HistoryManager {
    var recentlyPlayedLectures = arrayListOf<Long>()

    val documentUser = FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)

    fun addToHistory(lectureID: Long) {
        documentUser.update(
                "recentPlayIDs", FieldValue.arrayUnion(lectureID)).addOnSuccessListener {
            recentlyPlayedLectures.add(lectureID)
        }.addOnFailureListener { it->
            documentUser.set({"recentPlayIDs";lectureID}, SetOptions.merge())
            recentlyPlayedLectures.add(lectureID)
        }
    }

    fun removeFromHistory(lectureID: Long) {
        documentUser.update(
                "recentPlayIDs", FieldValue.arrayRemove(lectureID)).addOnSuccessListener {
            recentlyPlayedLectures.remove(lectureID)
        }
    }

    fun loadHistory() {
        documentUser.get().addOnSuccessListener { snapshot ->
            try {
                val userData = snapshot?.get("recentPlayIDs") as List<Int>
                if (!userData.isNullOrEmpty()) {
                    recentlyPlayedLectures = (userData as ArrayList<Long>?)!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}