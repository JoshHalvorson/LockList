package com.joshuahalvorson.safeyoutube.Kotlin.view.activity

import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.facebook.stetho.Stetho
import com.joshuahalvorson.safeyoutube.Java.view.activity.LoginActivity
import com.joshuahalvorson.safeyoutube.Java.view.activity.SettingsActivity
import com.joshuahalvorson.safeyoutube.Java.view.activity.WatchPlaylistActivity
import com.joshuahalvorson.safeyoutube.Java.view.fragment.AddPlaylistDialogFragment
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.R
import java.util.ArrayList

class PlaylistsListActivity : AppCompatActivity() {
    private var playlists: ArrayList<Playlist> = ArrayList()
    private var adapter: PlaylistsListRecyclerviewAdapter? = null
    private var parent: ConstraintLayout? = null
    var db: PlaylistDatabase? = null
    var isLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists_list)
        Stetho.initializeWithDefaults(this)

        parent = findViewById(R.id.parent)

        val viewAdapter = PlaylistsListRecyclerviewAdapter(false, playlists, object: PlaylistsListRecyclerviewAdapter.OnListItemClick{
            override fun onListItemClick(playlist: Playlist?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        val viewManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.playlists_list).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        db = Room.databaseBuilder(applicationContext,
                PlaylistDatabase::class.java, "database-playlists").build()

        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                startAddPlaylistFragment(intent.getStringExtra(Intent.EXTRA_TEXT), false)
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { startAddPlaylistFragment(null, true) }
    }

    override fun onResume() {
        super.onResume()
        Thread(Runnable {
            val tempPlaylists = db?.playlistDao()?.getAllPlaylists()
            runOnUiThread {
                if (tempPlaylists != null) {
                    playlists.clear()
                    playlists.addAll(tempPlaylists)
                    adapter?.notifyDataSetChanged()
                }
            }
        }).start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            if (!isLoggedIn) {
                val loginIntent = Intent(applicationContext, LoginActivity::class.java)
                startActivityForResult(loginIntent, SettingsActivity.LOGIN_REQUEST_CODE)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startAddPlaylistFragment(url: String?, showFrag: Boolean) {
        val addPlaylistDialogFragment = AddPlaylistDialogFragment()
        val bundle: Bundle
        if (url != null) {
            bundle = Bundle()
            bundle.putString(AddPlaylistDialogFragment.PLAYLIST_URL_KEY, url)
            if (!showFrag) {
                bundle.putBoolean(AddPlaylistDialogFragment.SHOW_FRAG_KEY, false)
            }
            addPlaylistDialogFragment.arguments = bundle
        }
        addPlaylistDialogFragment.show(supportFragmentManager, "add_playlist")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SettingsActivity.LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.i("login", "logged in")
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            startActivity(intent)
        } else if (requestCode == SettingsActivity.LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            Log.i("login", "not logged in")
            Snackbar.make(parent as View, "Not logged in", Snackbar.LENGTH_SHORT).show()
        }
    }
}
