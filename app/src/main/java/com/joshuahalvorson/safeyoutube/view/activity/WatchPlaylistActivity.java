package com.joshuahalvorson.safeyoutube.view.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.joshuahalvorson.safeyoutube.ApiKey;
import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.adapter.PlaylistItemsListRecyclerviewAdapter;
import com.joshuahalvorson.safeyoutube.model.Item;
import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;
import com.joshuahalvorson.safeyoutube.network.YoutubeDataApiViewModel;

import java.util.ArrayList;

public class WatchPlaylistActivity extends AppCompatActivity {
    public static final String PLAYLIST_ID_KEY = "playlistId";
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private YoutubeDataApiViewModel viewModel;
    private ArrayList<Item> items;
    private PlaylistItemsListRecyclerviewAdapter adapter;
    private String playlistId;
    private SharedPreferences sharedPref;
    private int ageValue;
    private YouTubePlayer mYoutubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_playlist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        items = new ArrayList<>();
        adapter = new PlaylistItemsListRecyclerviewAdapter(items, new PlaylistItemsListRecyclerviewAdapter.OnVideoClicked() {
            @Override
            public void onVideoClicked(int itemIndex) {
                mYoutubePlayer.loadPlaylist(playlistId, itemIndex, 1);
                mYoutubePlayer.play();
            }
        });

        viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel.class);
        youTubePlayerFragment =
                (YouTubePlayerSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.youtube_fragment);
        RecyclerView videosRecyclerview = findViewById(R.id.videos_list);
        videosRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        videosRecyclerview.setAdapter(adapter);
        if(getIntent() != null) {
            playlistId = getIntent().getStringExtra(PLAYLIST_ID_KEY);
            initializeVideo(youTubePlayerFragment, playlistId, 0);
        }

        LiveData<PlaylistResultOverview> playlistResultOverview =
                viewModel.getPlaylistOverview(playlistId);

        playlistResultOverview.observe(this, new Observer<PlaylistResultOverview>() {
            @Override
            public void onChanged(@Nullable PlaylistResultOverview playlistResultOverview) {
                items.clear();
                items.addAll(playlistResultOverview.getItems());
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        ageValue = sharedPref.getInt(getString(R.string.age_range_key), 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(loginIntent, SettingsActivity.LOGIN_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeVideo(YouTubePlayerSupportFragment fragment, final String playlistId, final int itemIndex){
        fragment.initialize(ApiKey.KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                mYoutubePlayer = youTubePlayer;
                mYoutubePlayer.loadPlaylist(playlistId);
                switch (ageValue){
                    case 0:
                        mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                        break;
                    case 1:
                        mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                        break;
                    case 2:
                        mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        break;
                }

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                if (youTubeInitializationResult.isUserRecoverableError()) {
                    youTubeInitializationResult.getErrorDialog(getParent(), 1).show();
                } else {
                    String errorMessage = String.format(
                            getString(R.string.error_player), youTubeInitializationResult.toString());
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == SettingsActivity.LOGIN_REQUEST_CODE && resultCode == RESULT_OK){
            Log.i("login", "logged in");
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }else if (requestCode == SettingsActivity.LOGIN_REQUEST_CODE && resultCode == RESULT_CANCELED){
            Log.i("login", "not logged in");
            Snackbar.make(findViewById(R.id.videos_list), "Not logged in", Snackbar.LENGTH_SHORT).show();
        }
    }

}
