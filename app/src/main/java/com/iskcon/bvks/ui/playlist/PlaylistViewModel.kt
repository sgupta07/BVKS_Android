package com.iskcon.bvks.ui.playlist

import androidx.lifecycle.*
import com.iskcon.bvks.manager.PlaylistsManager
import com.iskcon.bvks.model.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel(isPrivatePlaylistSelected: Boolean = true) : ViewModel() {

    private var mPlaylistsMap: MutableMap<String?, Playlist?> = mutableMapOf()

     var _mPlaylists = MutableLiveData<MutableList<Playlist?>>()
     var playlistUnSorted = MutableLiveData<MutableList<Playlist?>>()

    private var _loadPrivatePlaylist = MutableLiveData<Boolean>()
    val loadPrivatePlaylist: LiveData<Boolean>
        get() = _loadPrivatePlaylist

    init {
        _loadPrivatePlaylist.value = isPrivatePlaylistSelected
        changePlaylistType()
    }

    fun changePlaylistType(){
        if (_loadPrivatePlaylist.value!!)
            loadPrivatePlaylists()
        else
            loadPublicPlaylists()
    }

    fun changeLoadedPlaylist(){
        _loadPrivatePlaylist.value = !_loadPrivatePlaylist.value!!
    }

    private fun loadPublicPlaylists() {
        CoroutineScope(Dispatchers.Main).launch {
            PlaylistsManager.documentPublic.get()
                    .addOnSuccessListener { documents ->
                        mPlaylistsMap.clear()
                        for (document in documents) {
                            val playlist = document.toObject(Playlist::class.java)
                            mPlaylistsMap[playlist.listID] = playlist
                        }
                        _mPlaylists.value = mPlaylistsMap.values.toMutableList()
                        playlistUnSorted.value = mPlaylistsMap.values.toMutableList()
                    }
                    .addOnFailureListener {
                        return@addOnFailureListener
                    }
        }
    }


    private fun loadPrivatePlaylists() {
        CoroutineScope(Dispatchers.Main).launch {
            PlaylistsManager.documentPrivate.get()
                    .addOnSuccessListener { documents ->
                        mPlaylistsMap.clear()
                        for (document in documents) {
                            val playlist = document.toObject(Playlist::class.java)
                            mPlaylistsMap[playlist.listID] = playlist
                        }
                        _mPlaylists.value = mPlaylistsMap.values.toMutableList()
                        playlistUnSorted.value = mPlaylistsMap.values.toMutableList()
                    }
                    .addOnFailureListener {
                        return@addOnFailureListener
                    }
        }
    }

    fun forceLoadPrivatePlaylists() {
        loadPrivatePlaylists()
    }

    fun forceLoadPublicPlaylists() {
        loadPublicPlaylists()
    }

    fun getPlaylistIds(): MutableList<String> {
        val arr: MutableList<String> = mutableListOf()
        _mPlaylists.value?.let {
            for (p in it) {
                p?.let { pl ->
                    arr.add(pl.listID!!)
                }
            }
        }
        return arr
    }

    fun getPlaylistNames(ids: Array<String>): MutableList<String> {
        val arr: MutableList<String> = mutableListOf()
        if (!mPlaylistsMap.isNullOrEmpty()) {
            for (i in ids) {
                arr.add(mPlaylistsMap[i]!!.title!!)
            }
        }
        return arr
    }

}

class PlaylistModelFactory(private val isPrivatePlaylistSelected: Boolean = true) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            return PlaylistViewModel(isPrivatePlaylistSelected) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}