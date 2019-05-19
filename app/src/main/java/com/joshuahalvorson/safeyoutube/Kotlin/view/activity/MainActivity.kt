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

    lateinit var googleAccountCredential: GoogleAccountCredential

    internal val REQUEST_ACCOUNT_PICKER = 1000
    internal val REQUEST_AUTHORIZATION = 1001
    internal val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    internal val REQUEST_PERMISSION_GET_ACCOUNTS = 1003

    private val PREF_ACCOUNT_NAME = "account_name"

    private val SCOPES = arrayOf(YouTubeScopes.YOUTUBE_READONLY)

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

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())

        val accountName = getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            googleAccountCredential.selectedAccountName = accountName;
            getResultsFromApi()
        }else{
            startActivityForResult(
                    googleAccountCredential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER)
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

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (googleAccountCredential.getSelectedAccountName() == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            Toast.makeText(applicationContext, "No network connection available.", Toast.LENGTH_LONG).show()
        } else {
            //MakeRequestTask(googleAccountCredential).execute()
            Toast.makeText(applicationContext, googleAccountCredential.selectedAccountName, Toast.LENGTH_LONG).show()
            //add logout code
        }
    }

    @AfterPermissionGranted(1003)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                        applicationContext, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    ?.getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                googleAccountCredential.setSelectedAccountName(accountName)
                getResultsFromApi()
            } else {
                startActivityForResult(
                        googleAccountCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(
                        applicationContext,
                        "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.",
                        Toast.LENGTH_LONG).show()
            } else {
                getResultsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings?.edit()
                    editor?.putString(PREF_ACCOUNT_NAME, accountName)
                    editor?.apply()
                    googleAccountCredential.setSelectedAccountName(accountName)
                    getResultsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this)
    }

    fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(applicationContext)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(applicationContext)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    internal fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }
}
