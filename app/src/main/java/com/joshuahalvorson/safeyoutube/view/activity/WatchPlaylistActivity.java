package com.joshuahalvorson.safeyoutube.view.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private YoutubeDataApiViewModel viewModel;
    private ArrayList<Item> items;
    private PlaylistItemsListRecyclerviewAdapter adapter;
    private String playlistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_playlist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        items = new ArrayList<>();
        adapter = new PlaylistItemsListRecyclerviewAdapter(items);
        viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel.class);
        youTubePlayerFragment =
                (YouTubePlayerSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.youtube_fragment);
        RecyclerView videosRecyclerview = findViewById(R.id.videos_list);
        videosRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        videosRecyclerview.setAdapter(adapter);
        if(getIntent() != null) {
            playlistId = getIntent().getStringExtra("playlistId");
            initializeVideo(youTubePlayerFragment, playlistId);
        }

        LiveData<PlaylistResultOverview> playlistResultOverview =
                viewModel
                        .getPlaylistOverview(
                                playlistId,
                                "50");

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeVideo(YouTubePlayerSupportFragment fragment, final String playlistId){
        fragment.initialize(ApiKey.KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadPlaylist(playlistId);
                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
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

}
