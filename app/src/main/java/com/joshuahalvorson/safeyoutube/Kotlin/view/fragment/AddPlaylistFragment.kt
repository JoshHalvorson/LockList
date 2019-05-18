package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models
import com.joshuahalvorson.safeyoutube.Kotlin.network.YoutubeDataApiViewModel
import com.joshuahalvorson.safeyoutube.R
import android.content.DialogInterface

class AddPlaylistFragment : DialogFragment() {
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    val PLAYLIST_URL_KEY = "playlist_url"
    val SHOW_FRAG_KEY = "show_frag"
    private var parentView: ConstraintLayout? = null

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
                parentView?.setVisibility(View.GONE)
                val window = dialog.window
                val windowParams = window!!.attributes
                windowParams.dimAmount = 0.0f
                windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                window.attributes = windowParams
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentView = view.findViewById(R.id.add_playlist_frag_parent)
        var urlEditText = view.findViewById<EditText>(R.id.video_url_edit_text)
        var addPlaylistButton = view.findViewById<Button>(R.id.add_playlist_button)
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
                            val item = playlistInfo.items[0]
                            val title = item.snippet?.title
                            val results = playlistResultOverview.pageInfo?.totalResults!!
                            val thumbnailUrl = item.snippet?.thumbnails?.standard?.url!!

                            val db = Room.databaseBuilder(this.context!!,
                                    PlaylistDatabase::class.java, "database-playlists").build()
                            Thread(Runnable {
                                db.playlistDao().insertAll(Playlist(
                                        playlistId,
                                        title,
                                        results,
                                        thumbnailUrl))
                            }).start()
                            db.close()

                            dismiss()
                        }
                    })
                }
            })
        }

        addPlaylistButton?.setOnClickListener(View.OnClickListener {
            if (urlEditText?.getText().toString() != "") {
                val url = urlEditText?.text.toString()
                val urlParts = url.split("list=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val playlistId = urlParts[1]
                val liveData = viewModel.getPlaylistInfo(playlistId)
                liveData?.observe(viewLifecycleOwner, Observer<Models.PlaylistResultOverview> { playlistInfo ->
                    if (playlistInfo != null) {
                        val liveData = viewModel.getPlaylistOverview(playlistId)
                        liveData?.observe(viewLifecycleOwner, Observer<Models.PlaylistResultOverview> { playlistResultOverview ->
                            if (playlistResultOverview != null) {
                                val item = playlistInfo.items[0]
                                val title = item.snippet?.title
                                val results = playlistResultOverview.pageInfo?.totalResults!!
                                val thumbnailUrl = item.snippet?.thumbnails?.standard?.url!!

                                val db = Room.databaseBuilder(this.context!!,
                                        PlaylistDatabase::class.java, "database-playlists").build()
                                Thread(Runnable {
                                    db.playlistDao().insertAll(Playlist(
                                            playlistId,
                                            title,
                                            results,
                                            thumbnailUrl))
                                }).start()
                                db.close()

                                dismiss()
                            }
                        })
                    }
                })
            }
        })
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }
}
