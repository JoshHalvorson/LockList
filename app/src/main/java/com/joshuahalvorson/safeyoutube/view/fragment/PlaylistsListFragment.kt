package com.joshuahalvorson.safeyoutube.view.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
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
import com.joshuahalvorson.safeyoutube.R
import com.joshuahalvorson.safeyoutube.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.database.Playlist
import com.joshuahalvorson.safeyoutube.database.PlaylistDatabase
import kotlinx.android.synthetic.main.fragment_playlists_list.*
import kotlinx.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class PlaylistsListFragment : androidx.fragment.app.Fragment() {
    private var playlists: ArrayList<Playlist> = ArrayList()
    private var adapter: PlaylistsListRecyclerviewAdapter? = null
    private var db: PlaylistDatabase? = null
    private var sharedPref: SharedPreferences? = null
    private lateinit var googleAccountCredential: GoogleAccountCredential

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val ids =
                sharedPref?.getString(getString(R.string.account_playlists_key), "")?.split(", ")
        adapter = ids?.let {
            PlaylistsListRecyclerviewAdapter(false, playlists, object : PlaylistsListRecyclerviewAdapter.OnListItemClick {
                override fun onListItemClick(playlist: Playlist?) {
                    val editor = sharedPref?.edit()
                    editor?.putString(getString(R.string.current_playlist_key), playlist?.playlistId)
                    editor?.apply()
                    Toast.makeText(context, "Playlist ${playlist?.playlistName} selected", Toast.LENGTH_LONG).show()
                    fragmentManager?.popBackStack()
                }
            }, it)
        }

        playlists_list.apply {
            playlists_list.layoutManager = LinearLayoutManager(context)
            playlists_list.adapter = adapter
        }

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
        val accountName = sharedPref?.getString("account_name", null)
        if (accountName != null) {
            googleAccountCredential.selectedAccountName = accountName
            MakeRequestTask().execute()
        } else {
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

        addPlaylistDialogFragment.show(fragmentManager, "add_playlist")
    }

    private fun updatePlaylistsList() {
        Thread(Runnable {
            val tempPlaylists = db?.playlistDao()?.getAllPlaylists()
            activity?.runOnUiThread {
                if (tempPlaylists != null) {
                    playlists.clear()
                    playlists.addAll(tempPlaylists)
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

    private inner class MakeRequestTask : AsyncTask<Void, Void, ArrayList<Playlist>>() {
        private var youtubeService: YouTube? = null
        private var lastError: Exception? = null

        override fun doInBackground(vararg params: Void): ArrayList<Playlist>? {
            return try {
                getDataFromApi()
            } catch (e: Exception) {
                lastError = e
                cancel(true)
                null
            }

        }

        @Throws(IOException::class)
        private fun getDataFromApi(): ArrayList<Playlist> {
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

            result?.items?.forEach {
                if (db?.playlistDao()?.getPlaylistById(it.id) == true) {
                    //playlist already in db
                    return tempList
                } else {
                    val playlist = Playlist(it.id,
                            it.snippet.title,
                            it.contentDetails.itemCount.toInt(),
                            it.snippet.thumbnails.default.url,
                            it.status.privacyStatus)

                    db?.playlistDao()?.insertAll(playlist)
                    tempList.add(playlist)
                }
            }
            return tempList
        }

        override fun onPreExecute() {
            //mProgress.show()
        }

        override fun onPostExecute(output: ArrayList<Playlist>?) {
            //mProgress.hide()
            if (output == null || output.size == 0) {
                //account playlists already added
            } else {
                val editor = sharedPref?.edit()
                editor?.putString(getString(R.string.account_playlists_key), output.joinToString { it.playlistId })
                editor?.apply()

                updatePlaylistsList()
            }
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
