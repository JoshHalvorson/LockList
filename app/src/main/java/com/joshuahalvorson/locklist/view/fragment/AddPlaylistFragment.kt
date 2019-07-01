package com.joshuahalvorson.locklist.view.fragment

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
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.database.LocalPlaylist
import com.joshuahalvorson.locklist.database.PlaylistDatabase
import com.joshuahalvorson.locklist.network.YoutubeDataApiViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AddPlaylistFragment : androidx.fragment.app.DialogFragment() {
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    val PLAYLIST_URL_KEY = "playlist_url"
    val SHOW_FRAG_KEY = "show_frag"
    private var db: PlaylistDatabase? = null
    private var parentView: ConstraintLayout? = null

    private lateinit var viewModel: YoutubeDataApiViewModel
    private lateinit var disposable: CompositeDisposable

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_playlist, container, false)
    }

    override fun onStart() {
        super.onStart()
        val bundle = arguments
        if (bundle != null) {
            if (bundle.getBoolean(SHOW_FRAG_KEY)) {
                parentView?.visibility = View.GONE
                val window = dialog.window
                val windowParams = window?.attributes
                windowParams?.dimAmount = 0.0f
                windowParams?.flags = windowParams?.flags?.or(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window?.attributes = windowParams
            }
        }
        db = context?.let {
            Room.databaseBuilder(it,
                    PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentView = view.findViewById(R.id.add_playlist_frag_parent)
        val urlEditText = view.findViewById<EditText>(R.id.video_url_edit_text)
        val addPlaylistButton = view.findViewById<Button>(R.id.add_playlist_button)
        viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        val url = arguments?.getString(PLAYLIST_URL_KEY)
        addPlaylist(url)

        addPlaylistButton?.setOnClickListener {
            if (urlEditText?.text.toString() != "") {
                val playlistUrl = urlEditText?.text.toString()
                addPlaylist(playlistUrl)
            }
        }
    }

    private fun addPlaylist(url: String?) {
        disposable = CompositeDisposable()
        val urlParts = url?.split("list=".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        val playlistId = urlParts?.get(1)
        if (playlistId != null) {
            viewModel.getPlaylistInfo(playlistId)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe { playlistInfo ->
                        viewModel.getPlaylistOverview(playlistId)
                                ?.subscribeOn(Schedulers.io())
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribe { playlistResultOverview ->
                                    Thread(Runnable {
                                        if (db?.localPlaylistDao()?.getPlaylistById(playlistId) == true) {
                                            activity?.runOnUiThread {
                                                Toast.makeText(context, "Playlist is already added", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            val item = playlistInfo.items[0]
                                            val title = item.snippet?.title
                                            val results = playlistResultOverview.pageInfo?.totalResults
                                            val thumbnailUrl = item.snippet?.thumbnails?.default?.url
                                            val status = item.status?.privacyStatus

                                            db?.localPlaylistDao()?.insertAll(LocalPlaylist(
                                                    playlistId,
                                                    title,
                                                    results,
                                                    thumbnailUrl,
                                                    status))

                                            dismiss()
                                        }
                                    }).start()
                                }
                    }?.let { subscribe ->
                        disposable.add(
                                subscribe
                        )
                    }
        }

    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
