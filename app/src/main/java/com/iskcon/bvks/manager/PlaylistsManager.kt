package com.iskcon.bvks.manager

import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.iskcon.bvks.model.Playlist
import com.iskcon.bvks.model.TopLectures
import com.iskcon.bvks.util.Utils

object PlaylistsManager {
    val documentPrivate = FirebaseFirestore.getInstance()
            .collection("PrivatePlaylists")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection(FirebaseAuth.getInstance().currentUser!!.email!!)
    val documentPublic = FirebaseFirestore.getInstance().collection("PublicPlaylists")

    fun addNewPlaylist(playlist: Playlist) {

        if (playlist.listType == "Private") {
            playlist.docPath = "PrivatePlaylists/${FirebaseAuth.getInstance().currentUser!!.uid}" +
                    "/${FirebaseAuth.getInstance().currentUser!!.email!!}/"
            documentPrivate.add(playlist)
                    .addOnSuccessListener { documentReference ->
                        playlist.docPath = playlist.docPath + documentReference.id
                        playlist.listID = documentReference.id
                        documentPrivate.document(documentReference.id).set(playlist)
                                .addOnSuccessListener {}
                                .addOnFailureListener {}
                    }
                    .addOnFailureListener {}
        } else if (playlist.listType == "Public") {
            playlist.docPath = "PublicPlaylists/"
            documentPublic.add(playlist)
                    .addOnSuccessListener { documentReference ->
                        playlist.docPath = playlist.docPath + documentReference.id
                        playlist.listID = documentReference.id
                        documentPublic.document(documentReference.id).set(playlist)
                    }
        }
    }

    fun addToPlaylist(lectureID: Long, playlist: Playlist) {
        if (playlist.listType == "Private") {
            documentPrivate.document(playlist.listID!!).update(
                    "lectureCount", FieldValue.increment(1)
            )
            documentPrivate.document(playlist.listID!!).update(
                    "lectureIds", FieldValue.arrayUnion(lectureID)
            )
        } else if (playlist.listType == "Public") {
            documentPublic.document(playlist.listID!!).update(
                    "lectureCount", FieldValue.increment(1)
            )
            documentPublic.document(playlist.listID!!).update(
                    "lectureIds", FieldValue.arrayUnion(lectureID)
            )
        }
    }

    fun removeFromPlaylist(lectureID: Long, playlist: Playlist) {
        if (playlist.listType == "Private") {
            documentPrivate.document(playlist.listID!!).get().addOnSuccessListener { documentSnapshot->
                val dataModal = documentSnapshot.toObject(Playlist::class.java)
                documentPrivate.document(playlist.listID!!).update(
                        "lectureCount", dataModal?.lectureCount!!-1
                )

            }

            documentPrivate.document(playlist.listID!!).update(
                    "lectureIds", FieldValue.arrayRemove(lectureID)
            )
        } else if (playlist.listType == "Public") {
            documentPublic.document(playlist.listID!!).get().addOnSuccessListener { documentSnapshot->
                val dataModal = documentSnapshot.toObject(Playlist::class.java)
                documentPublic.document(playlist.listID!!).update(
                        "lectureCount", dataModal?.lectureCount!!-1
                )

            }
            documentPublic.document(playlist.listID!!).update(
                    "lectureIds", FieldValue.arrayRemove(lectureID)
            )
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        FirebaseFirestore.getInstance()
                .document(playlist.docPath!!)
                .delete()
                .addOnSuccessListener { }
                .addOnFailureListener { }
    }

}