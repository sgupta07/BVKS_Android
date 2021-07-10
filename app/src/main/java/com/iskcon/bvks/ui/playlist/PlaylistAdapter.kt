package com.iskcon.bvks.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.iskcon.bvks.R
import com.iskcon.bvks.listeners.DeletePlaylistPopUpMenuListener
import com.iskcon.bvks.listeners.OnListItemInteractionListener
import com.iskcon.bvks.manager.PlaylistsManager
import com.iskcon.bvks.model.Playlist
import com.iskcon.bvks.util.ImageUtil
import com.iskcon.bvks.util.PopUpDeletePlaylist
import com.iskcon.bvks.util.Utils
import java.text.SimpleDateFormat

class PlaylistAdapter(overflowListener: OnListItemInteractionListener, playlist: MutableList<Playlist?>?)
    : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    private lateinit var selectedLecture: Playlist
    var mOverflowMenuListener: OnListItemInteractionListener? = null
    private var mPlaylists: MutableList<Playlist?>? = null
    var mDateFormat: SimpleDateFormat? = null

    init {
        mOverflowMenuListener = overflowListener
        mPlaylists = playlist
        mDateFormat = SimpleDateFormat("dd-MMM-yyyy")
    }

    fun setPlaylists(playlists: MutableList<Playlist?>) {
        mPlaylists = playlists
    }

    fun clearList() {
        mPlaylists = mutableListOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_item_playlist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPlaylists!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist: Playlist? = mPlaylists!![position]
        holder.mTitle.text = playlist?.title
        holder.mVerse.text = playlist?.lecturesCategory
        holder.mCount.text = "Lecture :${playlist?.lectureCount.toString()}"
        holder.mDate.text = Utils.getDate(playlist!!.creationTime, "dd-MMM-yyyy")
        holder.mEmail.text = playlist.authorEmail
        holder.mThumbnail.visibility = View.VISIBLE
        if (FirebaseAuth.getInstance().currentUser?.email.equals(playlist?.authorEmail)) {
            holder.mSettingPopup.visibility = View.VISIBLE
        } else {
            holder.mSettingPopup.visibility = View.GONE
        }

        ImageUtil.loadThumbnail(holder.mThumbnail, playlist?.thumbnail ?: "")

        holder.mView.tag = playlist
        holder.mView.setOnClickListener { v: View ->
            val arr = mutableListOf<Long>()
            mPlaylists!![position]!!.lectureIds!!.forEach {
                arr.add(it)
            }
            (mOverflowMenuListener as Fragment).requireFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_playlist_layout,
                            PlaylistContentsFragment.newInstance(arr.toLongArray(),playlist), null)
//                    .setReorderingAllowed(true)
                    .addToBackStack(null) // name can be null
                    .commit()
        }
        holder.mSettingPopup.tag = playlist

        holder.mSettingPopup.setOnClickListener {
            showPopupMenuV2(it)
        }


    }

    private fun showPopupMenuV2(view: View) {
        selectedLecture = view.tag as Playlist
        PopUpDeletePlaylist().showLectureMenuPopupWindow(view, selectedLecture) { playlist ->
            PlaylistsManager.deletePlaylist(playlist!!)
            mPlaylists!!.remove(playlist)
            notifyDataSetChanged()
        }
    }

    class ViewHolder internal constructor(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mThumbnail: ImageView
        val mTitle: TextView
        val mVerse: TextView
        val mDate: TextView
        val mCount: TextView
        val mEmail: TextView
        val mSettingPopup: ImageButton

        init {
            mThumbnail = mView.findViewById(R.id.thumbnail)
            mTitle = mView.findViewById(R.id.title)
            mCount = mView.findViewById(R.id.count)
            mVerse = mView.findViewById(R.id.verse)
            mEmail = mView.findViewById(R.id.email)
            mDate = mView.findViewById(R.id.date)
            mSettingPopup = mView.findViewById(R.id.settings_popup)
        }
    }
}