package com.iskcon.bvks.ui.playlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink.*
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.iskcon.bvks.R
import com.iskcon.bvks.listeners.LectureListener
import com.iskcon.bvks.listeners.OnListItemInteractionListener
import com.iskcon.bvks.manager.LectureManager
import com.iskcon.bvks.model.Lecture
import com.iskcon.bvks.model.Playlist
import com.iskcon.bvks.ui.lectures.LectureAdapter
import com.iskcon.bvks.ui.lectures.LectureFragment
import com.iskcon.bvks.ui.lectures.LectureListBaseFragment
import com.iskcon.bvks.util.Constants
import com.iskcon.bvks.util.Utils


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_LECTURE_IDS = "param1"
private const val ARG_PLAYLIST_INSTANCE = "playlistInstance"

class PlaylistContentsFragment : LectureListBaseFragment(), OnListItemInteractionListener,
        LectureListener {

    private var mAdapter: LectureAdapter? = null
    private val mPlaylistLectures: MutableList<Lecture> = mutableListOf()
    private var mRecyclerView: RecyclerView? = null
    private var mEmptyView: TextView? = null
    private var param1: LongArray? = null
    private var playList: Playlist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getLongArray(ARG_LECTURE_IDS)
            playList = it.getSerializable(ARG_PLAYLIST_INSTANCE) as Playlist?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_contents, container, false)
        // Inflate the layout for this fragment
        arguments!!.getLongArray(ARG_LECTURE_IDS)!!.forEach {
            if (LectureManager.getInstance().getLecture(it) != null) {
                mPlaylistLectures.add(LectureManager.getInstance().getLecture(it))
            }
        }
//        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "your title"
        mEmptyView = view!!.findViewById(R.id.empty_view)
        mRecyclerView = view.findViewById(R.id.listLectures)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mRecyclerView!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.layoutManager = linearLayoutManager
        mAdapter = LectureAdapter(this, mPlaylistLectures!!)
        mRecyclerView!!.adapter = mAdapter
//        super.onViewCreated(view, savedInstanceState)
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: LongArray, playList: Playlist) =
                PlaylistContentsFragment().apply {
                    arguments = Bundle().apply {
                        putLongArray(ARG_LECTURE_IDS, param1)
                        putSerializable(ARG_PLAYLIST_INSTANCE, playList)
                    }
                }
    }

    override fun playerStateChanged() {
        mAdapter!!.notifyDataSetChanged()
    }

    override fun deleteFavoritesAllowed(): Boolean {
        return false
    }

    override fun favoritesAllowed(): Boolean {
        return true
    }

    override fun showPlaybackProgress(): Boolean {
        return true
    }

    override fun downloadAllowed(): Boolean {
        return true
    }

    override fun removeDownloadsAllowed(): Boolean {
        return false
    }

    override fun show3dotPopupMenu(): Boolean {
        return true
    }

    override fun isVideoOption(): Boolean {
        return false
    }

    override fun shareLecture(lecture: Lecture?) {
        shareWithDeepLink(lecture!!)
    }

    override fun isRemoveLectureAllowed(): Boolean {
        return playList?.authorEmail == FirebaseAuth.getInstance().currentUser?.email
    }

    override fun getPlaylistObject(): Playlist {
        return playList!!
    }

    /**
     * Method to create dynamic link and share it on social platform
     */
    private fun shareWithDeepLink(lecture: Lecture) {
        val link = Constants.DEEP_LINK_URL + "?lectureId=" + lecture.id
        val description = Utils.getInstance().createLectureDescriptionForShare(lecture)
        val thumbnailUrl = Utils.getInstance().getLectureThumbnailUrlForShare(lecture)
        val longLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDynamicLinkDomain(Constants.DYNAMIC_LINK_DOMAIN)
                .setAndroidParameters(
                        AndroidParameters.Builder()
                                .build())
                .setIosParameters(IosParameters.Builder("com.bhakti.bvks").setAppStoreId("1536451261").build())
                .setSocialMetaTagParameters(
                        SocialMetaTagParameters.Builder()
                                .setTitle(lecture.name)
                                .setDescription(description)
                                .setImageUrl(Uri.parse(thumbnailUrl))
                                .build())
                .buildDynamicLink()
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(longLink.uri)
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener { task: Task<ShortDynamicLink> ->
                    if (task.isSuccessful) {
                        val shortLink = task.result!!.shortLink
                        println("shortLink = $shortLink")
                        //open share dialog
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString())
                        startActivity(Intent.createChooser(sendIntent, "Share Lecture"))
                    } else {
                        Utils.getInstance().showToast(context, "Something went wrong...")
                    }
                }
    }

    override fun playLecture(lecture: Lecture?, isVideoMode: Boolean) {
        (parentFragment as LectureFragment?)!!.onLecturePlayed(lecture, isVideoMode)
    }

    override fun rearrangeAllowed(): Boolean {
        return false
    }

    override fun lecturesUpdated(lectureList: MutableList<Lecture>, isLoaderHide:Boolean) {
        mAdapter!!.lectures = mPlaylistLectures
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        activity!!.invalidateOptionsMenu()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        LectureManager.getInstance().addListener(this)
//        FavoritesManager.getInstance().addListener(this);
    }

    override fun onStop() {
        super.onStop()
        LectureManager.getInstance().removeListener(this)
//        FavoritesManager.getInstance().removeListener(this);
    }


}