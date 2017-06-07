package mevg.com.practica10;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ListView navifableList;
    ActionBarDrawerToggle drawerToggle;
    CharSequence mTitle;
    ImageButton bt_bkFast, bt_back, bt_play, bt_forward, bt_fwFast;
    ImageView imageSong;
    /*****************************************/
    private List<Song> songList;
    private MusicService musicService;
    private Intent musicIntent;
    private boolean musicBound = false;

    private SeekBar seek;
    private TextView current, total;
    private ImageView songArt;
    private Handler handler = new Handler();

    private int idAlbum;

    private Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
            int currentSongPosition = musicService.getPosition();
            current.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(currentSongPosition),
                    TimeUnit.MILLISECONDS.toSeconds(currentSongPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentSongPosition))));
            seek.setProgress(currentSongPosition);
            handler.postDelayed(this,100);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setSongList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(musicIntent == null){
            musicIntent = new Intent(this,MusicService.class);
            bindService(musicIntent,serviceConnection, Context.BIND_AUTO_CREATE);
            startService(musicIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navifableList = (ListView) findViewById(R.id.left_drawer);
        seek = (SeekBar) findViewById(R.id.seek);
        current = (TextView) findViewById(R.id.curret);
        total = (TextView) findViewById(R.id.total);
        songArt = (ImageView) findViewById(R.id.imageSong);
        mTitle = getTitle();
        bt_bkFast = (ImageButton) findViewById(R.id.bt_bkFast);
        bt_back = (ImageButton) findViewById(R.id.bt_back);
        bt_play = (ImageButton) findViewById(R.id.bt_play);
        bt_forward = (ImageButton) findViewById(R.id.bt_forward);
        bt_fwFast = (ImageButton) findViewById(R.id.bt_fwFast);
        imageSong = (ImageView) findViewById(R.id.imageSong);
        //String[] options = getResources().getStringArray(R.array.nav_items);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,options);

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.playerPrev();
            }
        });

        bt_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.playNext();
            }
        });

        bt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicService.isPlaying()){
                    musicService.pausePlayer();
                    bt_play.setImageResource(R.drawable.play);
                }else{
                    musicService.resume();
                    bt_play.setImageResource(R.drawable.pause);

                }
            }
        });

        songList = new ArrayList<>();
        fillSongList();

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song as, Song bs) {
                return as.getTitle().compareTo(bs.getTitle());
            }
        });

        SongAdapter adapter = new SongAdapter(this,songList);

        navifableList.setAdapter(adapter);
        int sabierto = R.string.drawer_opened;
        int scerrado = R.string.drawer_closed;
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,sabierto,scerrado){
            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Selecciona uno");
                supportInvalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

     private void fillSongList(){
         ContentResolver musicResolver = getContentResolver();
         Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
         Toast.makeText(this, musicUri.toString(), Toast.LENGTH_LONG).show();
         Cursor musicCursor = musicResolver.query(musicUri,null,null,null,null);

         if(musicCursor != null && musicCursor.moveToFirst()){
             do{
                 int idcolumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                 int namecolumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                 int artistcolum = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                 int albumcolumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

                 long musicId = musicCursor.getLong(idcolumn);
                 String musicName = musicCursor.getString(namecolumn);
                 String musicArtist = musicCursor.getString(artistcolum);
                 long musicAlbumuId = musicCursor.getLong(albumcolumn);

                 Bitmap albumCover = null;
                 Uri albumUri = Uri.parse("content://media/external/audio/albumart");
                 Uri musicAlbumUri = ContentUris.withAppendedId(albumUri, musicAlbumuId);
                 try {
                     InputStream input = musicResolver.openInputStream(musicAlbumUri);
                     albumCover = BitmapFactory.decodeStream(input);
                 }catch (FileNotFoundException e){
                     //albumCover = BitmapFactory.
                     albumCover = Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888);
                 }

                 songList.add(new Song(musicId,musicName,musicArtist,albumCover));

             }while(musicCursor.moveToNext());
         }
    }

    public void songPicked(View view){
        String stag = view.getTag().toString();
        int itag = Integer.parseInt(stag);
        drawerLayout.closeDrawer(Gravity.LEFT);
        musicService.setSongIndex(itag);
        musicService.playSong();
        bt_play.setImageResource(R.drawable.pause);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int songDuration = musicService.getDuration();
                        total.setText(String.format("%d:%d",
                                TimeUnit.MILLISECONDS.toMinutes(songDuration),
                                TimeUnit.MILLISECONDS.toSeconds(songDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration))));
                        Uri UriArt = Uri.parse("content://media/external/audio/albumart");
                        Uri uri = ContentUris.withAppendedId(UriArt,idAlbum);
                        seek.setMax(songDuration);
                        new BitmapWorkerTask().execute(uri);
                        handler.postDelayed(updateSongTime,100);
                    }
                });
            }
        }).start();
    }
    class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(Uri... params) {
            Bitmap artWork = null;
            InputStream in = null;
            try {
                in = MainActivity.this.getContentResolver().openInputStream(params[0]);
                artWork = BitmapFactory.decodeStream(in);
            } catch (FileNotFoundException e) {
                artWork = Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
            }

            return artWork;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                songArt.setImageBitmap(bitmap);
            }
        }
    }
}
