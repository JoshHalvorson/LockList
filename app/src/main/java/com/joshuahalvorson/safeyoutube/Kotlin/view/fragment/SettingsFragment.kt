package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.app.UiModeManager
import android.arch.persistence.room.Room
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.database.PlaylistDatabase

import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.account_settings_layout.*
import kotlinx.android.synthetic.main.app_settings_layout.*
import kotlinx.android.synthetic.main.kids_settings_layout.*
import kotlinx.android.synthetic.main.playlists_list_element_layout.*

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        if (sharedPref?.getInt(getString(R.string.dark_mode_key), 1) == 2) {
            day_night_switch.setChecked(true)
            current_theme_text.setText("Night")
        } else {
            day_night_switch.setChecked(false)
            current_theme_text.setText("Day")
        }

        day_night_switch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            //TODO: add logic to switch day night mode
            if (isChecked) {
                current_theme_text.setText("Night")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val uiModeManager = activity?.getSystemService<UiModeManager>(UiModeManager::class.java)
                    uiModeManager?.nightMode = UiModeManager.MODE_NIGHT_YES
                    val editor = sharedPref?.edit()
                    editor?.putInt(getString(R.string.dark_mode_key), 2)
                    editor?.apply()
                }
            } else {
                current_theme_text.setText("Day")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val uiModeManager = activity?.getSystemService<UiModeManager>(UiModeManager::class.java)
                    uiModeManager?.setNightMode(UiModeManager.MODE_NIGHT_NO)
                    val editor = sharedPref?.edit()
                    editor?.putInt(getString(R.string.dark_mode_key), 1)
                    editor?.apply()
                }
            }
        })

        change_password_button.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(fragmentManager, "change_password_fragment")
        }

        val ageRangeValue = sharedPref?.getInt(getString(R.string.age_range_key), 0)
        if (ageRangeValue != null) {
            age_range_seek_bar.progress = ageRangeValue
        }

        age_range_seek_bar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
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

    private fun clearDb(){
        val db = Room.databaseBuilder(this.context!!,
                PlaylistDatabase::class.java, "database-playlists").build()
        Thread(Runnable { db.clearAllTables() }).start()
        db.close()
    }
}
