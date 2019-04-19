package com.joshuahalvorson.safeyoutube.view.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.view.fragment.ChangePasswordDialogFragment;

public class SettingsActivity extends AppCompatActivity {
    public static final int LOGIN_REQUEST_CODE = 1;
    private Button clearSavedPlaylistsButton, changePasswordButton;
    private SeekBar ageSeekBar;
    private Switch dayNightSwitch;
    private TextView currentThemeText;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dayNightSwitch = findViewById(R.id.day_night_switch);
        ageSeekBar = findViewById(R.id.age_range_seek_bar);
        clearSavedPlaylistsButton = findViewById(R.id.clear_cache_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        currentThemeText = findViewById(R.id.current_theme_text);

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getSystemService(UiModeManager.class).getCurrentModeType() == 1){
                dayNightSwitch.setChecked(true);
                currentThemeText.setText("Night");
            }else{
                dayNightSwitch.setChecked(false);
                currentThemeText.setText("Day");
            }
        }

        dayNightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: add logic to switch day night mode
                if(isChecked){
                    currentThemeText.setText("Night");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        UiModeManager uiModeManager = getSystemService(UiModeManager.class);
                        uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                    }
                }else{
                    currentThemeText.setText("Day");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        UiModeManager uiModeManager = getSystemService(UiModeManager.class);
                        uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                    }
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialogFragment changePasswordDialogFragment = new ChangePasswordDialogFragment();
                changePasswordDialogFragment.show(getSupportFragmentManager(), "change_password");
            }
        });

        int ageRangeValue = sharedPref.getInt(getString(R.string.age_range_key), 0);
        ageSeekBar.setProgress(ageRangeValue);

        ageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.age_range_key), seekBar.getProgress());
                editor.apply();
            }
        });

        clearSavedPlaylistsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearPlaylistsAlert();
            }
        });
    }

    public void showClearPlaylistsAlert(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to clear saved playlists?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                MainActivity.clearDb();
                            }
                        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
