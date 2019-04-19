package com.joshuahalvorson.safeyoutube.view.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.joshuahalvorson.safeyoutube.R;

public class SettingsActivity extends AppCompatActivity {
    private Button clearSavedPlaylistsButton, changePasswordButton;
    private SeekBar ageSeekBar;
    private Switch dayNightSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dayNightSwitch = findViewById(R.id.day_night_switch);
        ageSeekBar = findViewById(R.id.age_range_seek_bar);
        clearSavedPlaylistsButton = findViewById(R.id.clear_cache_button);
        changePasswordButton = findViewById(R.id.change_password_button);

        dayNightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: add logic to switch day night mode
                if(isChecked){
                    dayNightSwitch.setText("Night");
                }else{
                    dayNightSwitch.setText("Day");
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: add logic to change current password
            }
        });

        ageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //TODO: add logic to change youtube player mode based on selected age range
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
