package com.joshuahalvorson.safeyoutube.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.joshuahalvorson.safeyoutube.R;

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

        dayNightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: add logic to switch day night mode
                if(isChecked){
                    currentThemeText.setText("Night");
                }else{
                    currentThemeText.setText("Day");
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: add logic to change current password
            }
        });

        sharedPref = getPreferences(Context.MODE_PRIVATE);
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
                sharedPref = getPreferences(Context.MODE_PRIVATE);
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
