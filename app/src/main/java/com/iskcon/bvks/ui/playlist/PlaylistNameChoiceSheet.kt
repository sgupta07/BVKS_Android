package com.iskcon.bvks.ui.playlist

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iskcon.bvks.R
import com.iskcon.bvks.manager.PlaylistsManager
import com.iskcon.bvks.model.Playlist
import com.iskcon.bvks.util.Constants
import com.iskcon.bvks.util.ImageUtil
import com.iskcon.bvks.util.Utils
import java.text.SimpleDateFormat


/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    PlaylistNameChoiceSheet.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class PlaylistNameChoiceSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel : PlaylistViewModel
    private val lectureIDNameChoice = "PLAYLIST_NAMES_NCS"
    private val lectureIDNameChoice2 = "PLAYLIST_NAMES_NCS_2"
    private var currentLectureChoice : Long = 0
    private var mSearchString: String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val loadPrivatePlaylist = arguments!!.getBoolean(lectureIDNameChoice)
        val viewModelFactory = PlaylistModelFactory(loadPrivatePlaylist)
        viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(PlaylistViewModel::class.java)
        return inflater.inflate(R.layout.fragment_playlist_name_choice_sheet_list_item, container, false)
    }
    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
        val layoutParams = bottomSheet.layoutParams
        val windowHeight = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
    private var mSelectedSortPosition = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentLectureChoice = requireArguments().getString(lectureIDNameChoice2)!!.toLong()
        view.findViewById<RecyclerView>(R.id.listLectures)!!.layoutManager = LinearLayoutManager(requireContext())


        viewModel._mPlaylists.observe(this, Observer {
            view.findViewById<RecyclerView>(R.id.listLectures)!!.adapter = PlaylistNameAdapter(it.toTypedArray())
        })
        view.findViewById<ImageButton>(R.id.ib_sort)!!.setOnClickListener {
            Utils.getInstance().showSortDropDown(activity, "Sort", Constants.ARR_SORTING_PLAYLIST, mSelectedSortPosition) { position, option ->
                mSelectedSortPosition = position
                sort(position)
            }
        }
        view.findViewById<EditText>(R.id.et_search)!!.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.length > 2) {
                    mSearchString = s.toString()
                } else {
                    mSearchString = null
                    viewModel.changePlaylistType()
                }
                    mSearchString?.let {
                        viewModel._mPlaylists.postValue(getSearchedPlaylists(
                                viewModel._mPlaylists.value, it) as MutableList<Playlist?>?)

                    }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        /*view.findViewById<SearchView>(R.id.et_search)!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        })*/
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
                playlist.sortBy { it!!.title }
                viewModel._mPlaylists.postValue(playlist);
            }

            2 -> {
                //"Alphabetically: Z->A",
                var playlist = viewModel.playlistUnSorted.value!!
                playlist.sortByDescending { it!!.title }
                viewModel._mPlaylists.postValue(playlist);
            }


        }
    }

    override fun onResume() {
        super.onResume()

        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        val wm = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = if (metrics.widthPixels < 1280) metrics.widthPixels - 100 else 1280
        val height = -1 // MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
    }

    companion object {


        fun newInstance(lectureID : String, isPrivatePlaylist : Boolean): PlaylistNameChoiceSheet =
                PlaylistNameChoiceSheet().apply {
                    arguments = Bundle().apply {
                        putString (lectureIDNameChoice2, lectureID)
                        putBoolean(lectureIDNameChoice, isPrivatePlaylist)
                    }
                }

    }

    //region Recycler View
    private inner class ViewHolder constructor(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_playlist_name_choice_sheet, parent , false)) {

        val text: TextView = itemView.findViewById(R.id.title)
        val verse: TextView = itemView.findViewById(R.id.verse)
        val date: TextView = itemView.findViewById(R.id.date)
        val count: TextView = itemView.findViewById(R.id.count)
        val email: TextView = itemView.findViewById(R.id.email)
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val rl_parent: RelativeLayout = itemView.findViewById(R.id.rl_parent)
    }

    private inner class PlaylistNameAdapter constructor(private val playlists: Array<Playlist?>) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = playlists[position]?.title
            holder.verse.text = playlists[position]?.lecturesCategory
            holder.email.text = playlists[position]?.authorEmail
            holder.count.text = "Lecture :${playlists[position]?.lectureCount.toString()}"
            holder.date.text = Utils.getDate(playlists[position]?.creationTime!!, "dd-MMM-yyyy")
            ImageUtil.loadThumbnail(holder.thumbnail, playlists[position]?.thumbnail ?: "")
            holder.rl_parent.setOnClickListener {
                val lectureID = currentLectureChoice
                if(playlists[position]!!.lectureIds!!.contains(lectureID)){
                    Toast.makeText(context, "Already in the playlist", Toast.LENGTH_SHORT)
                            .show()
                }else{
                    PlaylistsManager.addToPlaylist(lectureID, playlists[position]!!)
                    val textForToast="Lecture added to playlist, ‘${playlists[position]!!.title}’"
                    Utils.getInstance().showToast(context,textForToast)
                }
                dismiss()
            }
        }

        override fun getItemCount(): Int {
            return playlists.size
        }
    }
    //endregion
}