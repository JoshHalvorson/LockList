package com.joshuahalvorson.locklist.view.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.locklist.model.Playlist
import com.joshuahalvorson.locklist.database.RemotePlaylist
import com.joshuahalvorson.locklist.database.PlaylistDatabase
import com.joshuahalvorson.locklist.util.SharedPrefsHelper
import kotlinx.android.synthetic.main.fragment_playlists_list.*
import kotlinx.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class PlaylistsListFragment : androidx.fragment.app.Fragment() {
    private var playlists: ArrayList<Playlist> = ArrayList()
    private var adapter: PlaylistsListRecyclerviewAdapter? = null
    private var db: PlaylistDatabase? = null

    private lateinit var googleAccountCredential: GoogleAccountCredential
    private lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlists_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = context?.let {
            Room.databaseBuilder(it,
                    PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.title = "Settings"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.title = "Select a playlist"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedPrefsHelper = SharedPrefsHelper(activity?.getSharedPreferences(
                SharedPrefsHelper.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE))

        adapter = PlaylistsListRecyclerviewAdapter(false, playlists, object : PlaylistsListRecyclerviewAdapter.OnListItemClick {
            override fun onListItemClick(playlist: Playlist?) {
                sharedPrefsHelper.put(SharedPrefsHelper.CURRENT_PLAYLIST_KEY, "${playlist?.playlistId}, ${playlist?.playlistName}")
                Toast.makeText(context, "Playlist ${playlist?.playlistName} selected", Toast.LENGTH_LONG).show()
                fragmentManager?.popBackStack()
            }
        })

        playlists_list.layoutManager = LinearLayoutManager(context)
        playlists_list.adapter = adapter

        activity?.findViewById<FloatingActionButton>(R.id.add_playlist_button)?.setOnClickListener {
            startAddPlaylistFragment(null, true)
        }

        if (savedInstanceState == null) {
            val bundle = arguments
            val playlistUrl = bundle?.getString("playlist_url")
            val showFrag = bundle?.getBoolean("show_frag")
            if (playlistUrl != null && showFrag == false) {
                startAddPlaylistFragment(playlistUrl, showFrag)
            }
        }

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(YouTubeScopes.YOUTUBE_READONLY))
                .setBackOff(ExponentialBackOff())
        val accountName = sharedPrefsHelper.get("account_name", null)
        if (accountName != null) {
            googleAccountCredential.selectedAccountName = accountName
            MakeRequestTask().execute()
        } else {
            Toast.makeText(context, "Not logged in, no remote playlists available", Toast.LENGTH_LONG)
                    .show()
            //not logged in
        }
    }

    private fun startAddPlaylistFragment(url: String?, showFrag: Boolean) {
        val addPlaylistDialogFragment = AddPlaylistFragment()
        val bundle: Bundle
        if (url != null) {
            bundle = Bundle()
            bundle.putString("playlist_url", url)
            if (!showFrag) {
                bundle.putBoolean("show_frag", false)
            }
            addPlaylistDialogFragment.arguments = bundle
        }

        addPlaylistDialogFragment.setOnDismissListener(DialogInterface.OnDismissListener {
            updatePlaylistsList()
        })

        fragmentManager?.let { addPlaylistDialogFragment.show(it, "add_playlist") }
    }

    private fun updatePlaylistsList() {
        Thread(Runnable {
            playlists.clear()
            val localPlaylists = db?.localPlaylistDao()?.getAllPlaylists()
            val remotePlaylists = db?.remotePlaylistDao()?.getAllPlaylists()

            remotePlaylists?.forEach {
                playlists.add(Playlist(
                        it.playlistId,
                        it.playlistName,
                        it.playlistVideoCount,
                        it.playlistThumbnail,
                        it.privacyStatus,
                        true))
            }

            localPlaylists?.forEach {
                playlists.add(Playlist(
                        it.playlistId,
                        it.playlistName,
                        it.playlistVideoCount,
                        it.playlistThumbnail,
                        it.privacyStatus,
                        false))
            }

            activity?.runOnUiThread {
                if (localPlaylists != null && remotePlaylists != null) {
                    adapter?.notifyDataSetChanged()
                }
            }
        }).start()
    }

    override fun onResume() {
        super.onResume()
        updatePlaylistsList()
    }

    override fun onDestroy() {
        super.onDestroy()
        db?.close()
    }

    private inner class MakeRequestTask : AsyncTask<Void, Void, Void>() {
        private var youtubeService: YouTube? = null
        private var lastError: Exception? = null

        override fun doInBackground(vararg params: Void): Void? {
            try {
                getDataFromApi()
            } catch (e: Exception) {
                lastError = e
                cancel(true)
            }
            return null
        }

        @Throws(IOException::class)
        private fun getDataFromApi() {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            youtubeService = YouTube.Builder(
                    transport, jsonFactory, googleAccountCredential)
                    .setApplicationName("SafeYoutube")
                    .build()

            val tempList = ArrayList<Playlist>()

            val result = youtubeService?.playlists()?.list("status, snippet, contentDetails")
                    ?.setMine(true)
                    ?.execute()

            db?.remotePlaylistDao()?.deleteAll()
            result?.items?.forEach {
                val remotePlaylists = RemotePlaylist(
                        it.id,
                        it.snippet.title,
                        it.contentDetails.itemCount.toInt(),
                        it.snippet.thumbnails.default.url,
                        it.status.privacyStatus)
                db?.remotePlaylistDao()?.insertAll(remotePlaylists)
            }
        }

        override fun onPreExecute() {
            //mProgress.show()
        }

        override fun onPostExecute(output: Void?) {
            //mProgress.hide()
            updatePlaylistsList()
        }

        override fun onCancelled() {
            //mProgress.hide()
            if (lastError != null) {
                when (lastError) {
                    is GooglePlayServicesAvailabilityIOException -> showGooglePlayServicesAvailabilityErrorDialog(
                            (lastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                    is UserRecoverableAuthIOException -> startActivityForResult(
                            (lastError as UserRecoverableAuthIOException).intent,
                            1003)
                    else -> Toast.makeText(context, "The following error occurred:\n" + lastError!!.message, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Request cancelled.", Toast.LENGTH_LONG).show()
            }
        }
    }

    internal fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                1002)
        dialog.show()
    }

}
