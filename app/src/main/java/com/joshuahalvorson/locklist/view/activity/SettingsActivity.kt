package com.joshuahalvorson.locklist.view.activity

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.joshuahalvorson.locklist.BuildConfig
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.database.PlaylistDatabase
import com.joshuahalvorson.locklist.util.SharedPrefsHelper
import com.joshuahalvorson.locklist.view.fragment.ChangePasswordFragment
import com.joshuahalvorson.locklist.view.fragment.PlaylistsListFragment
import com.joshuahalvorson.locklist.view.fragment.RemovePlaylistFragment
import kotlinx.android.synthetic.main.account_settings_layout.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.app_settings_layout.*
import kotlinx.android.synthetic.main.kids_settings_layout.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var googleAccountCredential: GoogleAccountCredential
    private lateinit var sharedPrefsHelper: SharedPrefsHelper

    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        toolbar.title = "Settings"

        MobileAds.initialize(this) {
            val adRequest = AdRequest.Builder().addTestDevice(BuildConfig.test_device).build()
            adView.loadAd(adRequest)
        }

        val intent = intent
        val action = intent.action
        val type = intent.type

        if (savedInstanceState == null) {
            if (Intent.ACTION_SEND == action && type != null) {
                if ("text/plain" == type) {
                    val bundle = Bundle()
                    bundle.putString("playlist_url", intent.getStringExtra(Intent.EXTRA_TEXT))
                    bundle.putBoolean("show_frag", false)

                    launchPlaylistsListFragment(bundle)

                    intent.removeExtra(intent.type)
                }
            }
        }

        sharedPrefsHelper = SharedPrefsHelper(getSharedPreferences(
                SharedPrefsHelper.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE))

        if (sharedPrefsHelper.get(SharedPrefsHelper.DARK_MODE_KEY, 1) == 2) {
            day_night_switch.isChecked = true
            current_theme_text.text = "Night"
        } else {
            day_night_switch.isChecked = false
            current_theme_text.text = "Day"
        }

        day_night_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            //TODO: add logic to switch day night mode
            if (isChecked) {
                current_theme_text.text = "Night"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val uiModeManager = getSystemService<UiModeManager>(UiModeManager::class.java)
                    uiModeManager?.nightMode = UiModeManager.MODE_NIGHT_YES
                    sharedPrefsHelper.put(SharedPrefsHelper.DARK_MODE_KEY, 2)
                }
            } else {
                current_theme_text.text = "Day"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val uiModeManager = getSystemService<UiModeManager>(UiModeManager::class.java)
                    uiModeManager?.nightMode = UiModeManager.MODE_NIGHT_NO
                    sharedPrefsHelper.put(SharedPrefsHelper.DARK_MODE_KEY, 1)
                }
            }
        }

        change_password_button.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(supportFragmentManager, "change_password_fragment")
        }

        val ageRangeValue = sharedPrefsHelper.get(SharedPrefsHelper.AGE_RANGE_KEY, 0)
        ageRangeValue?.let {
            age_range_seek_bar.progress = it
        }

        age_range_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { sharedPrefsHelper.put("age_range", it) }
            }
        })

        clear_cache_button.setOnClickListener {
            showClearPlaylistsAlert()
        }

        remove_single_playlists.setOnClickListener {
            supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.select_playlist_fragment_container, RemovePlaylistFragment())
                    ?.addToBackStack("")
                    ?.commit()
        }

        select_playlist_button.setOnClickListener {
            launchPlaylistsListFragment(null)
        }

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(YouTubeScopes.YOUTUBE_READONLY))
                .setBackOff(ExponentialBackOff())

        checkLogIn()

    }

    private fun launchPlaylistsListFragment(bundle: Bundle?) {
        val playlistsFrag = PlaylistsListFragment()
        if (bundle != null) {
            playlistsFrag.arguments = bundle
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.select_playlist_fragment_container, playlistsFrag)
                .addToBackStack("")
                .commit()
    }

    private fun checkLogIn() {
        val accountName = sharedPrefsHelper.get(SharedPrefsHelper.ACCOUNT_NAME_KEY, null)
        if (accountName != null) {
            googleAccountCredential.selectedAccountName = accountName
            log_in_to_youtube_button.text = "Log out of $accountName"
            log_in_to_youtube_button.setOnClickListener {
                showLogOutAlertDialog()
            }
        } else {
            log_in_to_youtube_button.setOnClickListener {
                startActivityForResult(
                        googleAccountCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        }
    }

    private fun signOut() {
        val db = Room.databaseBuilder(this.applicationContext,
                PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        sharedPrefsHelper.remove(SharedPrefsHelper.ACCOUNT_NAME_KEY)
        val ids = sharedPrefsHelper.get(SharedPrefsHelper.ACCOUNT_PLAYLISTS_KEY, "")
        val idParts = ids?.split(", ")
        idParts?.forEach {
            Thread(Runnable {
                db.remotePlaylistDao().deletePlaylistById(it)
            }).start()
        }
        log_in_to_youtube_button.text = "Log in"
        Toast.makeText(applicationContext, "Logged out", Toast.LENGTH_LONG).show()
        sharedPrefsHelper.remove(SharedPrefsHelper.CURRENT_PLAYLIST_KEY)
        checkLogIn()
    }

    private fun showLogOutAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this@SettingsActivity, R.style.AlertDialog)
        alertDialogBuilder.setMessage("Are you sure you want to log out of ${googleAccountCredential.selectedAccountName}?")
        alertDialogBuilder.setPositiveButton("Yes"
        ) { arg0, arg1 -> signOut() }

        alertDialogBuilder.setNegativeButton("No") { dialog, which -> }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showClearPlaylistsAlert() {
        val alertDialogBuilder = AlertDialog.Builder(this@SettingsActivity, R.style.AlertDialog)
        alertDialogBuilder.setMessage("Are you sure you want to clear saved playlists?")
        alertDialogBuilder.setPositiveButton("Yes"
        ) { arg0, arg1 -> clearDb() }

        alertDialogBuilder.setNegativeButton("No") { dialog, which -> }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun clearDb() {
        val db = Room.databaseBuilder(this.applicationContext,
                PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        Thread(Runnable { db?.remotePlaylistDao()?.deleteAll() }).start()
        db.close()
    }


    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (googleAccountCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            Toast.makeText(applicationContext, "No network connection available.", Toast.LENGTH_LONG).show()
        } else {
            log_in_to_youtube_button.text = "Log out of ${googleAccountCredential.selectedAccountName}"
            Toast.makeText(applicationContext, "Logged in to ${googleAccountCredential.selectedAccountName}", Toast.LENGTH_LONG).show()
        }
    }

    @AfterPermissionGranted(1003)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                        applicationContext, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = sharedPrefsHelper.get(SharedPrefsHelper.ACCOUNT_NAME_KEY, null)
            if (accountName != null) {
                googleAccountCredential.selectedAccountName = accountName
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
                    sharedPrefsHelper.put(SharedPrefsHelper.ACCOUNT_NAME_KEY, accountName)
                    googleAccountCredential.selectedAccountName = accountName
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
                this@SettingsActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }
}
