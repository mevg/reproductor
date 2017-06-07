package mevg.com.practica10;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yjm_e on 11/18/2015.
 */
public class SongAdapter  extends BaseAdapter{
    private List<Song> songList;
    LayoutInflater inflater;
    public SongAdapter(Context context, List<Song> songList) {
        this.songList = songList;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return songList.size();
    }
    @Override
    public Object getItem(int position) {
        return 0;
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLayout = (LinearLayout) inflater.inflate(R.layout.song, parent, false);
        ImageView thumbnail = (ImageView) songLayout.findViewById(R.id.iv_cover);
        TextView artisName = (TextView) songLayout.findViewById(R.id.tv_artist_name);
        TextView songName = (TextView) songLayout.findViewById(R.id.tv_song_name);

        Song currentSong = songList.get(position);
        artisName.setText(currentSong.getArtis());
        songName.setText(currentSong.getTitle());
        thumbnail.setImageBitmap(currentSong.getThumbnai());

        songLayout.setTag(position);
        return songLayout;
    }
}
