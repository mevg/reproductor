package mevg.com.practica10;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    List<Song> songList;
    MediaPlayer player;
    int songIndex;
    String songName = "";
    public final static int NOTIFY_ID = 1;
    private final IBinder musicBinder = new MusicBinder();

    public MusicService() {
    }

    public class MusicBinder extends Binder{
        MusicService getService(){return MusicService.this;}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        songIndex = 0;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            player.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        player.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();

        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.play)
                .setOngoing(true)
                .setContentTitle("Playing" + songName)
                .setContentText(songName);
        Notification notification = builder.build();
        startForeground(NOTIFY_ID, notification);
    }

    public void playNext(){
        songIndex++;
        if(songIndex >= songList.size()) songIndex=0;
        playSong();
    }

    public void playSong(){
        player.reset();
        Song currentSong = songList.get(songIndex);
        long currentSongId = currentSong.getID();
        songName = currentSong.getTitle();
        Uri currentSongUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currentSongId);
        try {
            player.setDataSource(getApplicationContext(), currentSongUri);
        }catch(IOException e){
            Log.e("MUSIC SERVICE", "Error trying to get song", e);
        }
        player.prepareAsync();
    }

    public int getPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public void resume(){
        player.start();
    }

    public void playerPrev(){
        songIndex--;
        if(songIndex < songList.size()) songIndex = songList.size() - 1;
        playSong();
    }

    public void setSongList(List<Song> songList){
        this.songList = songList;
    }

    public int getSongIndex() {
        return songIndex;
    }

    public void setSongIndex(int songIndex) {
        this.songIndex = songIndex;
    }
}
