package com.joshuahalvorson.safeyoutube.Kotlin.view.activity

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.facebook.stetho.Stetho
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.view.fragment.LoginFragment
import com.joshuahalvorson.safeyoutube.Kotlin.view.fragment.PlaylistsListFragment
import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.account_settings_layout.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class MainActivity : AppCompatActivity() {
    var isLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Stetho.initializeWithDefaults(this)

        val intent = intent
        val action = intent.action
        val type = intent.type

        if (savedInstanceState == null){
            if (Intent.ACTION_SEND == action && type != null) {
                if ("text/plain" == type) {
                    val bundle = Bundle()
                    bundle.putString("playlist_url", intent.getStringExtra(Intent.EXTRA_TEXT))
                    bundle.putBoolean("show_frag", false)

                    launchPlaylistsListFragment(bundle)

                    intent.removeExtra(intent.type)
                }
            }else {
                launchPlaylistsListFragment(null)
            }
        }else {
            launchPlaylistsListFragment(null)
        }
    }

    private fun launchPlaylistsListFragment(bundle: Bundle?) {
        val playlistsFrag = PlaylistsListFragment()
        if (bundle != null){
            playlistsFrag.arguments = bundle
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, playlistsFrag)
                .commit()
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
