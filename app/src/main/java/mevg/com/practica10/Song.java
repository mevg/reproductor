package mevg.com.practica10;

import android.graphics.Bitmap;

/**
 * Created by yjm_e on 11/18/2015.
 */
public class Song {
    private long ID;//identificacdor de la cancion
    private String title;//titulo de la cancion
    private String artis;//artista de la cancion
    private Bitmap thumbnai;//album cover


    public Song(long ID, String title, String artis, Bitmap thumbnai) {
        this.ID = ID;
        this.title = title;
        this.artis = artis;
        this.thumbnai = thumbnai;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public String getArtis() {
        return artis;
    }

    public Bitmap getThumbnai() {
        return thumbnai;
    }

}
