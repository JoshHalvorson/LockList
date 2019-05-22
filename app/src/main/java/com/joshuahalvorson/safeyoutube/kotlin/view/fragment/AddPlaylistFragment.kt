package com.joshuahalvorson.safeyoutube.kotlin.view.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import com.joshuahalvorson.safeyoutube.kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.kotlin.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.kotlin.model.Models
import com.joshuahalvorson.safeyoutube.kotlin.network.YoutubeDataApiViewModel
import com.joshuahalvorson.safeyoutube.R

class AddPlaylistFragment : androidx.fragment.app.DialogFragment() {
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    val PLAYLIST_URL_KEY = "playlist_url"
    val SHOW_FRAG_KEY = "show_frag"
    private var parentView: ConstraintLayout? = null

    private lateinit var db: PlaylistDatabase

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_playlist, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (arguments != null) {
            if (!arguments!!.getBoolean(SHOW_FRAG_KEY)) {
                parentView?.visibility = View.GONE
                val window = dialog.window
                val windowParams = window!!.attributes
                windowParams.dimAmount = 0.0f
                windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                window.attributes = windowParams
            }
        }
        db = Room.databaseBuilder(this.context!!,
                PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentView = view.findViewById(R.id.add_playlist_frag_parent)
        val urlEditText = view.findViewById<EditText>(R.id.video_url_edit_text)
        val addPlaylistButton = view.findViewById<Button>(R.id.add_playlist_button)
        val viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        if (arguments != null) {
            val url = arguments!!.getString(PLAYLIST_URL_KEY)
            val urlParts = url!!.split("list=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val playlistId = urlParts[1]
            val liveData = viewModel.getPlaylistInfo(playlistId)
            liveData?.observe(this, Observer<Models.PlaylistResultOverview> { playlistInfo ->
                if (playlistInfo != null) {
                    val liveData = viewModel.getPlaylistOverview(playlistId)
                    liveData?.observe(viewLifecycleOwner, Observer<Models.PlaylistResultOverview> { playlistResultOverview ->
                        if (playlistResultOverview != null) {
                            Thread(Runnable {
                                if (db.playlistDao().getPlaylistById(playlistId)) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(context, "Playlist is already added", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    val item = playlistInfo.items[0]
                                    val title = item.snippet?.title
                                    val results = playlistResultOverview.pageInfo?.totalResults!!
                                    val thumbnailUrl = item.snippet?.thumbnails?.standard?.url!!
                                    val status = item.status?.privacyStatus

                                    db.playlistDao().insertAll(Playlist(
                                            playlistId,
                                            title,
                                            results,
                                            thumbnailUrl,
                                            status))

                                    dismiss()
                                }
                            }).start()
                        }
                    })
                }
            })
        }

        addPlaylistButton?.setOnClickListener {
            if (urlEditText?.text.toString() != "") {
                val url = urlEditText?.text.toString()
                val urlParts = url.split("list=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val playlistId = urlParts[1]
                val liveData = viewModel.getPlaylistInfo(playlistId)
                liveData?.observe(viewLifecycleOwner, Observer<Models.PlaylistResultOverview> { playlistInfo ->
                    if (playlistInfo != null) {
                        val liveData = viewModel.getPlaylistOverview(playlistId)
                        liveData?.observe(viewLifecycleOwner, Observer<Models.PlaylistResultOverview> { playlistResultOverview ->
                            if (playlistResultOverview != null) {
                                Thread(Runnable {
                                    if (db.playlistDao().getPlaylistById(playlistId)) {
                                        activity?.runOnUiThread {
                                            Toast.makeText(context, "Playlist is already added", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        val item = playlistInfo.items[0]
                                        val title = item.snippet?.title
                                        val results = playlistResultOverview.pageInfo?.totalResults!!
                                        val thumbnailUrl = item.snippet?.thumbnails?.standard?.url!!
                                        val status = item.status?.privacyStatus

                                        db.playlistDao().insertAll(Playlist(
                                                playlistId,
                                                title,
                                                results,
                                                thumbnailUrl,
                                                status))
                                    }

                                    dismiss()
                                }).start()
                            }
                        })
                    }
                })
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }
}
