package com.joshuahalvorson.locklist.view.fragment

import android.app.ActionBar
import android.content.DialogInterface
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.database.LocalPlaylist
import com.joshuahalvorson.locklist.database.PlaylistDatabase
import com.joshuahalvorson.locklist.network.YoutubeDataApiViewModel
import com.joshuahalvorson.locklist.util.removeErrorOnTextChange
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_add_playlist.*


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
        dialog?.window?.setLayout(Constraints.LayoutParams.FILL_PARENT, Constraints.LayoutParams.WRAP_CONTENT)
        val bundle = arguments
        if (bundle != null) {
            if (bundle.getBoolean(SHOW_FRAG_KEY)) {
                parentView?.visibility = View.GONE
                val window = dialog?.window
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
        viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        video_url_edit_text.removeErrorOnTextChange(video_url_edit_text_layout)

        val url = arguments?.getString(PLAYLIST_URL_KEY)
        addPlaylist(url)

        add_playlist_button.setOnClickListener {
            if (video_url_edit_text.text.toString() != "") {
                if (isValidUrl(video_url_edit_text.text.toString())) {
                    val playlistUrl = video_url_edit_text.text.toString()
                    addPlaylist(playlistUrl)
                } else {
                    video_url_edit_text_layout.error = "Enter a valid url"
                }
            } else {
                video_url_edit_text_layout.error = "Field is empty"
            }
        }
    }

    private fun isValidUrl(url: String): Boolean {
        val pattern = Patterns.WEB_URL
        val matcher = pattern.matcher(url.toLowerCase())
        return matcher.matches()
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

    override fun onDismiss(dialog: DialogInterface) {
        onDismissListener?.onDismiss(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
