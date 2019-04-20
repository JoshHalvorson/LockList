package com.joshuahalvorson.safeyoutube.view.activity;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.facebook.stetho.Stetho;
import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.adapter.PlaylistsListRecyclerviewAdapter;
import com.joshuahalvorson.safeyoutube.database.Playlist;
import com.joshuahalvorson.safeyoutube.database.PlaylistDatabase;
import com.joshuahalvorson.safeyoutube.view.fragment.AddPlaylistDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddPlaylistDialogFragment.ReturnDataFromDialogFragment {
    private ArrayList<Playlist> playlists;
    private PlaylistsListRecyclerviewAdapter adapter;
    private ConstraintLayout parent;
    public static PlaylistDatabase db;

    public static boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Stetho.initializeWithDefaults(this);

        parent = findViewById(R.id.parent);
        playlists = new ArrayList<>();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddPlaylistFragment(null);
            }
        });

        adapter = new PlaylistsListRecyclerviewAdapter(playlists, new PlaylistsListRecyclerviewAdapter.OnListItemClick() {
            @Override
            public void onListItemClick(Playlist playlist) {
                Log.i("clicked", playlist.playlistId);
                Intent intent = new Intent(getApplicationContext(), WatchPlaylistActivity.class);
                intent.putExtra(WatchPlaylistActivity.PLAYLIST_ID_KEY, playlist.playlistId);
                startActivity(intent);
            }
        });

        RecyclerView playlistsListRecyclerview = findViewById(R.id.playlists_list);
        playlistsListRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        playlistsListRecyclerview.setAdapter(adapter);

        db = Room.databaseBuilder(getApplicationContext(),
                PlaylistDatabase.class, "database-playlists").build();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                startAddPlaylistFragment(intent.getStringExtra(Intent.EXTRA_TEXT));
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Playlist> tempPlaylists = db.playlistDao().getAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tempPlaylists != null){
                            playlists.clear();
                            playlists.addAll(tempPlaylists);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
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
            if(!MainActivity.isLoggedIn){
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(loginIntent, SettingsActivity.LOGIN_REQUEST_CODE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAddPlaylistFragment(String url){
        AddPlaylistDialogFragment addPlaylistDialogFragment = new AddPlaylistDialogFragment();
        Bundle bundle;
        if (url != null){
            bundle = new Bundle();
            bundle.putString(AddPlaylistDialogFragment.PLAYLIST_URL_KEY, url);
            addPlaylistDialogFragment.setArguments(bundle);
        }
        addPlaylistDialogFragment.show(getSupportFragmentManager(), "add_playlist");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == SettingsActivity.LOGIN_REQUEST_CODE && resultCode == RESULT_OK){
            Log.i("login", "logged in");
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }else if (requestCode == SettingsActivity.LOGIN_REQUEST_CODE && resultCode == RESULT_CANCELED){
            Log.i("login", "not logged in");
            Snackbar.make(parent, "Not logged in", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void returnData(final Playlist playlist) {
        playlists.add(playlist);
        adapter.notifyItemChanged(playlists.size() - 1);
        if(db != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    db.playlistDao().insertAll(playlist);
                }
            }).start();
        }
        Snackbar.make(parent, "Added playlist", Snackbar.LENGTH_SHORT).show();
    }

    public static void clearDb(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.clearAllTables();
            }
        }).start();
    }
}
