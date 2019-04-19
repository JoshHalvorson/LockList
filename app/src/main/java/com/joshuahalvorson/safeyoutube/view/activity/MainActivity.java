package com.joshuahalvorson.safeyoutube.view.activity;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.joshuahalvorson.safeyoutube.ApiKey;
import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.adapter.PlaylistsListRecyclerviewAdapter;
import com.joshuahalvorson.safeyoutube.view.fragment.AddPlaylistDialogFragment;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        AddPlaylistDialogFragment.ReturnDataFromDialogFragment {
    private ArrayList<String> playlistIds;
    private PlaylistsListRecyclerviewAdapter adapter;
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private ConstraintLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        parent = findViewById(R.id.parent);

        youTubePlayerFragment =
                (YouTubePlayerSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.youtube_fragment);

        playlistIds = new ArrayList<>();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddPlaylistFragment();
            }
        });

        adapter = new PlaylistsListRecyclerviewAdapter(playlistIds, new PlaylistsListRecyclerviewAdapter.OnListItemClick() {
            @Override
            public void onListItemClick(String playlistId) {
                Log.i("clicked", playlistId);
                initializeVideo(youTubePlayerFragment, playlistId);

            }
        });

        RecyclerView playlistsListRecyclerview = findViewById(R.id.playlists_list);
        playlistsListRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        playlistsListRecyclerview.setAdapter(adapter);

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

    private void startAddPlaylistFragment(){
        AddPlaylistDialogFragment addPlaylistDialogFragment = new AddPlaylistDialogFragment();
        addPlaylistDialogFragment.show(getSupportFragmentManager(), "add_playlist");
    }

    private void initializeVideo(YouTubePlayerSupportFragment fragment, final String playlistId){
        fragment.initialize(ApiKey.KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cuePlaylist(playlistId);
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

    @Override
    public void returnData(String playlistId) {
        playlistIds.add(playlistId);
        adapter.notifyItemChanged(playlistIds.size() - 1);
        Snackbar.make(parent, "Added playlist", Snackbar.LENGTH_SHORT).show();
    }
}
