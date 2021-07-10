package com.iskcon.bvks.ui.playlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.dynamiclinks.DynamicLink.*
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.iskcon.bvks.R
import com.iskcon.bvks.listeners.OnListItemInteractionListener
import com.iskcon.bvks.model.Lecture
import com.iskcon.bvks.model.Playlist
import com.iskcon.bvks.ui.lectures.LectureListBaseFragment
import com.iskcon.bvks.util.Constants
import com.iskcon.bvks.util.Utils
import kotlinx.android.synthetic.main.fragment_playlist_v2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PlaylistFragmentV3 : LectureListBaseFragment(), OnListItemInteractionListener {


    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mEmptyView: TextView
    private lateinit var mAdapter: PlaylistAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var viewModel: PlaylistViewModel
    private var mSearchString: String? = ""
    private var isPrivateSelected = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewModelFactory = PlaylistModelFactory(isPrivateSelected)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PlaylistViewModel::class.java)
        return inflater.inflate(R.layout.fragment_playlist_v2, container, false)

    }
    private var rlPrivate: RelativeLayout? = null
    private var rlPublic: RelativeLayout? = null
    private var tvPublic: TextView? = null
    private var tvPrivate: TextView? = null
    private var vPrivate: View? = null
    private var vPublic: View? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rlPrivate = view.findViewById<View>(R.id.rl_private) as RelativeLayout
        rlPublic = view.findViewById<View>(R.id.rl_public) as RelativeLayout
        tvPrivate = view.findViewById<View>(R.id.tv_private) as TextView
        tvPublic = view.findViewById<View>(R.id.tv_public) as TextView
        vPrivate = view.findViewById(R.id.v_private) as View
        vPublic = view.findViewById(R.id.v_public) as View

        rlPrivate!!.setOnClickListener {
            if (isPrivateSelected) return@setOnClickListener
            isPrivateSelected = true
            togglePlaylistDisplay()
            tvPrivate!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvPublic!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            vPrivate!!.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            vPublic!!.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }

        rlPublic!!.setOnClickListener {
            if (!isPrivateSelected) return@setOnClickListener
            isPrivateSelected = false
            togglePlaylistDisplay()
            tvPublic!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvPrivate!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            vPublic!!.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            vPrivate!!.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }

        viewModel.loadPrivatePlaylist.observe(this, Observer {
            viewModel.changePlaylistType()
        })

        // Set the adapter
        mEmptyView = view.findViewById(R.id.empty_view)
        mRecyclerView = view.findViewById(R.id.listLectures)
        mRecyclerView.setHasFixedSize(true)
        // Set up Layout Manager, reverse layout
        mLayoutManager = LinearLayoutManager(context)
        mRecyclerView.layoutManager = mLayoutManager
        if (viewModel._mPlaylists.value.isNullOrEmpty()) {
            mRecyclerView.visibility = View.GONE
            mEmptyView.text = "No playlists"
            mEmptyView.visibility = View.VISIBLE
        } else {
            mRecyclerView.visibility = View.VISIBLE
            mEmptyView.visibility = View.GONE
        }
        mAdapter = PlaylistAdapter(this, viewModel._mPlaylists.value)
        mRecyclerView.adapter = mAdapter

        viewModel._mPlaylists.observe(this, Observer {
            mAdapter.setPlaylists(it)
            mAdapter.notifyDataSetChanged()
            if (viewModel._mPlaylists.value.isNullOrEmpty()) {
                mRecyclerView.visibility = View.GONE
                mEmptyView.text = "No playlists"
                mEmptyView.visibility = View.VISIBLE
            } else {
                mRecyclerView.visibility = View.VISIBLE
                mEmptyView.visibility = View.GONE
            }
        })
//        super.onViewCreated(view, savedInstanceState)
    }

    private fun togglePlaylistDisplay() {
        if (isPrivateSelected) {
            //PRIVATE LIST SELECTED
            viewModel.changeLoadedPlaylist()
        } else {
            //PUBLIC LIST SELECTED
            viewModel.changeLoadedPlaylist()
        }
    }

    override fun onResume() {
        viewModel.changePlaylistType()
        setHasOptionsMenu(true)
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun playerStateChanged() {
    }

    override fun deleteFavoritesAllowed(): Boolean {
        return false
    }

    override fun favoritesAllowed(): Boolean {
        return false
    }

    override fun showPlaybackProgress(): Boolean {
        return false
    }

    override fun downloadAllowed(): Boolean {
        return false
    }

    override fun removeDownloadsAllowed(): Boolean {
        return false
    }

    override fun show3dotPopupMenu(): Boolean {
        return false
    }

    override fun playLecture(lecture: Lecture?, isVideoMode: Boolean) {

    }

    override fun shareLecture(lecture: Lecture?) {
        shareWithDeepLink(lecture!!)
    }

    override fun isRemoveLectureAllowed(): Boolean {
        return false
    }

    override fun getPlaylistObject(): Playlist? {
        return null
    }

    override fun isVideoOption(): Boolean {
        return false
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

    override fun rearrangeAllowed(): Boolean {
        return false
    }

    private var mSelectedSortPosition = 0


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.playlist_search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search_playlist)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                if (s.length > 2) {
                    mSearchString = s
                } else {
                    viewModel.changePlaylistType()
                    mSearchString = null
                }
                CoroutineScope(Dispatchers.Default).launch {
                    mSearchString?.let {
                        viewModel._mPlaylists.postValue(getSearchedPlaylists(
                                viewModel._mPlaylists.value, it) as MutableList<Playlist?>?)

                    }
                }
                return true
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_item -> {
                Utils.getInstance().showSortDropDown(activity, "Sort", Constants.ARR_SORTING_PLAYLIST, mSelectedSortPosition) { position, option ->
                    mSelectedSortPosition = position
                    sort(position)
                }
                true
            }
            else -> false
        }
    }

    private fun getSearchedPlaylists(playlists: MutableList<Playlist?>?, searchString: String): List<Playlist?>? {

        val tempList = mutableListOf<Playlist>()
        playlists?.let { _playlists ->
            _playlists.forEach {
                it?.let {
                    if (it.title!!.toLowerCase().contains(searchString.toLowerCase())) {
                        tempList.add(it)
                    }
                }
            }
        }
        return tempList
    }

    private fun sort(position: Int) {
        when (position) {

            0 -> {
                //None
                var playlist = viewModel.playlistUnSorted.value!!
                viewModel._mPlaylists.postValue(playlist);
            }
            1 -> {
                //"Alphabetically: A->Z",
                var playlist = viewModel.playlistUnSorted.value!!
                playlist.sortBy { it!!.title!!.toLowerCase() }
                viewModel._mPlaylists.postValue(playlist);
            }

            2 -> {
                //"Alphabetically: Z->A",
                var playlist = viewModel.playlistUnSorted.value!!
                playlist.sortByDescending { it!!.title!!.toLowerCase() }
                viewModel._mPlaylists.postValue(playlist);
            }


        }
    }
}