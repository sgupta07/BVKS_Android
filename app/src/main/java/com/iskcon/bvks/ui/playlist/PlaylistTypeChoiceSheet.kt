package com.iskcon.bvks.ui.playlist

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iskcon.bvks.R
import kotlinx.android.synthetic.main.fragment_playlist_type_choice_sheet.*




/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    PlaylistTypeChoiceSheet.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class PlaylistTypeChoiceSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel : PlaylistViewModel
    private  val ARG_ITEM_COUNT = "type_choice"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val viewModelFactory = PlaylistModelFactory()
        viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(PlaylistViewModel::class.java)
        return inflater.inflate(R.layout.fragment_playlist_type_choice_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val selectedLecture = arguments!!.getLong(ARG_ITEM_COUNT)
        publicPlaylistChoice.setOnClickListener {
            PlaylistNameChoiceSheet.newInstance(selectedLecture.toString(), false).show(requireFragmentManager(), "dialog")
            dismiss()
        }
        privatePlaylistChoice.setOnClickListener {
            PlaylistNameChoiceSheet.newInstance(selectedLecture.toString(), true).show(requireFragmentManager(), "dialog")
            dismiss()
        }
        newPlaylistChoice.setOnClickListener {
            CreateNewPlaylistSheetV2.newInstance(selectedLecture).show(requireFragmentManager(), "dialog")
            dismiss()
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

        fun newInstance(lectureID: Long): PlaylistTypeChoiceSheet =
                PlaylistTypeChoiceSheet().apply {
                    arguments = Bundle().apply {
                        putLong(ARG_ITEM_COUNT, lectureID)
                    }
                }

    }
}