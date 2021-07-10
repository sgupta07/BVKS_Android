package com.iskcon.bvks.ui.playlist

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface.OnShowListener
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.iskcon.bvks.R
import com.iskcon.bvks.manager.LectureManager
import com.iskcon.bvks.manager.PlaylistsManager
import com.iskcon.bvks.model.Playlist
import com.iskcon.bvks.util.Utils
import kotlinx.android.synthetic.main.fragment_create_new_playlist_sheet_v2.*


class CreateNewPlaylistSheetV2 : BottomSheetDialogFragment() {

    private val mLectureIdKey = "LECTURE_ID_KEY"

    private var currentPlaylistTypeSelection = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_new_playlist_sheet_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ib_close.setOnClickListener {
            this.dismiss()
        }
        buttonSavePlaylist.setOnClickListener {
            if (editTextNewPlaylistTitle.text.isNullOrBlank()
                    || editTextNewPlaylistCategory.text.isNullOrBlank()
                    || editTextNewPlaylistDescription.text.isNullOrBlank()) {
                Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //////0-Private and 1-Public////////////
            currentPlaylistTypeSelection = if (rb_private.isChecked) 0 else 1
            Playlist(
                    authorEmail = FirebaseAuth.getInstance().currentUser?.email,
                    description = editTextNewPlaylistDescription.text.toString(),
                    docPath = "",
                    lastUpdate = System.currentTimeMillis(),
                    lectureCount = 1,
                    lectureIds = listOf(arguments!!.getLong(mLectureIdKey)),
                    lecturesCategory = editTextNewPlaylistCategory.text.toString(),
                    listType = if (currentPlaylistTypeSelection == 0) "Private" else "Public",
                    listID = "",
                    thumbnail = arguments?.getLong(mLectureIdKey)?.let { it1 -> LectureManager.getInstance().getLecture(it1).thumbnailUrl },
                    title = editTextNewPlaylistTitle.text.toString()
            ).apply {
                PlaylistsManager.addNewPlaylist(this)
                val textForToast="${if (currentPlaylistTypeSelection == 0) "Private" else "Public"} playlist ‘${editTextNewPlaylistTitle.text}’ created"
                Utils.getInstance().showToast(context,textForToast)
            }
            dismiss()
        }


    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener(OnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        })
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

    companion object {
        fun newInstance(lectureID: Long): CreateNewPlaylistSheetV2 =
                CreateNewPlaylistSheetV2().apply {
                    arguments = Bundle().apply {
                        putLong(mLectureIdKey, lectureID)
                    }
                }

    }

}