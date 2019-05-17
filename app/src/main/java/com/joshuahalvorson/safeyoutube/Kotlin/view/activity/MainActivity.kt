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
import com.joshuahalvorson.safeyoutube.Kotlin.view.fragment.LoginFragment
import com.joshuahalvorson.safeyoutube.Kotlin.view.fragment.PlaylistsListFragment
import com.joshuahalvorson.safeyoutube.R
import java.util.ArrayList

class MainActivity : AppCompatActivity(), PlaylistsListFragment.OnPlaylistFragmentInteractionListener {
    override fun onPlaylistInteraction(playlist: Playlist) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var isLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists_list)
        Stetho.initializeWithDefaults(this)

        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                val playlistsFrag = PlaylistsListFragment()
                var bundle = Bundle()
                bundle.putString("playlist_url", intent.getStringExtra(Intent.EXTRA_TEXT))
                bundle.putBoolean("show_frag", false)
                playlistsFrag.arguments = bundle
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, playlistsFrag)
                        .commit()
            }
        }else{
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PlaylistsListFragment())
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            if (!isLoggedIn) {
                val loginFragDialog = LoginFragment()
                loginFragDialog.show(supportFragmentManager, "login_fragment")
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
