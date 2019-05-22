package com.joshuahalvorson.safeyoutube.kotlin.view.fragment

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.joshuahalvorson.safeyoutube.kotlin.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.account_settings_layout.*
import kotlinx.android.synthetic.main.app_settings_layout.*
import kotlinx.android.synthetic.main.kids_settings_layout.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class SettingsFragment : androidx.fragment.app.Fragment() {
    lateinit var googleAccountCredential: GoogleAccountCredential

    internal val REQUEST_ACCOUNT_PICKER = 1000
    internal val REQUEST_AUTHORIZATION = 1001
    internal val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    internal val REQUEST_PERMISSION_GET_ACCOUNTS = 1003

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        if (sharedPref?.getInt(getString(R.string.dark_mode_key), 1) == 2) {
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
                    val uiModeManager = activity?.getSystemService<UiModeManager>(UiModeManager::class.java)
                    uiModeManager?.nightMode = UiModeManager.MODE_NIGHT_YES
                    val editor = sharedPref?.edit()
                    editor?.putInt(getString(R.string.dark_mode_key), 2)
                    editor?.apply()
                }
            } else {
                current_theme_text.text = "Day"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val uiModeManager = activity?.getSystemService<UiModeManager>(UiModeManager::class.java)
                    uiModeManager?.nightMode = UiModeManager.MODE_NIGHT_NO
                    val editor = sharedPref?.edit()
                    editor?.putInt(getString(R.string.dark_mode_key), 1)
                    editor?.apply()
                }
            }
        }

        change_password_button.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(fragmentManager, "change_password_fragment")
        }

        val ageRangeValue = sharedPref?.getInt(getString(R.string.age_range_key), 0)
        if (ageRangeValue != null) {
            age_range_seek_bar.progress = ageRangeValue
        }

        age_range_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val editor: SharedPreferences.Editor? = sharedPref?.edit()
                editor?.putInt("age_range", seekBar?.progress!!)
                editor?.apply()
            }
        })

        clear_cache_button.setOnClickListener {
            showClearPlaylistsAlert()
        }

        remove_single_playlists.setOnClickListener {
            fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_container, RemovePlaylistFragment())
                    ?.addToBackStack("")
                    ?.commit()
        }

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(YouTubeScopes.YOUTUBE_READONLY))
                .setBackOff(ExponentialBackOff())

        checkLogIn()
    }

    private fun checkLogIn() {
        val accountName = activity?.getPreferences(Context.MODE_PRIVATE)
                ?.getString(getString(R.string.account_name_key), null)
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
        val db = Room.databaseBuilder(this.context!!,
                PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = prefs?.edit()
        editor?.remove(getString(R.string.account_name_key))
        val ids = prefs?.getString(getString(R.string.account_playlists_key), "")
        val idParts = ids?.split(", ")
        idParts?.forEach {
            Thread(Runnable {
                db.playlistDao().deletePlaylistById(it)
            }).start()
        }
        editor?.apply()
        log_in_to_youtube_button.text = "Log in"
        Toast.makeText(context, "Logged out", Toast.LENGTH_LONG).show()
        checkLogIn()
    }

    private fun showLogOutAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this.context!!, R.style.AlertDialog)
        alertDialogBuilder.setMessage("Are you sure you want to log out of ${googleAccountCredential.selectedAccountName}?")
        alertDialogBuilder.setPositiveButton("Yes"
        ) { arg0, arg1 -> signOut() }

        alertDialogBuilder.setNegativeButton("No") { dialog, which -> }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showClearPlaylistsAlert() {
        val alertDialogBuilder = AlertDialog.Builder(this.context!!, R.style.AlertDialog)
        alertDialogBuilder.setMessage("Are you sure you want to clear saved playlists?")
        alertDialogBuilder.setPositiveButton("Yes"
        ) { arg0, arg1 -> clearDb() }

        alertDialogBuilder.setNegativeButton("No") { dialog, which -> }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun clearDb() {
        val db = Room.databaseBuilder(this.context!!,
                PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        Thread(Runnable { db.clearAllTables() }).start()
        db.close()
    }


    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (googleAccountCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            Toast.makeText(context, "No network connection available.", Toast.LENGTH_LONG).show()
        } else {
            log_in_to_youtube_button.text = "Log out of ${googleAccountCredential.selectedAccountName}"
            Toast.makeText(context, "Logged in to ${googleAccountCredential.selectedAccountName}", Toast.LENGTH_LONG).show()
        }
    }

    @AfterPermissionGranted(1003)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                        context!!, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = activity?.getPreferences(Context.MODE_PRIVATE)
                    ?.getString(getString(R.string.account_name_key), null)
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
                        context,
                        "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.",
                        Toast.LENGTH_LONG).show()
            } else {
                getResultsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = activity?.getPreferences(Context.MODE_PRIVATE)
                    val editor = settings?.edit()
                    editor?.putString(getString(R.string.account_name_key), accountName)
                    editor?.apply()
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
        val connMgr = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    internal fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

}