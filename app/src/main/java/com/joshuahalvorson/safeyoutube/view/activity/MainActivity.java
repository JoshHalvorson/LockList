package com.joshuahalvorson.safeyoutube.view.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.adapter.PlaylistsListRecyclerviewAdapter;
import com.joshuahalvorson.safeyoutube.view.fragment.AddPlaylistDialogFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        AddPlaylistDialogFragment.ReturnDataFromDialogFragment {
    private ArrayList<String> playlistIds;
    private PlaylistsListRecyclerviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    public void startWatchPlaylistFragment(String playlistId){

    }

    @Override
    public void returnData(String playlistId) {
        playlistIds.add(playlistId);
        adapter.notifyItemChanged(playlistIds.size() - 1);
        /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
    }
}
