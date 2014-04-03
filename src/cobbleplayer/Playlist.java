/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer;

import cobbleplayer.utilities.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.farng.mp3.TagException;

/**
 *
 * @author Jacob
 */
public class Playlist {

    private String name;
    private ObservableList<Song> songs = FXCollections.observableArrayList();

    public void write(BufferedWriter writer) {
        try {
            writer.write(Util.PLAYLIST_CODE + name);
            writer.newLine();
            for (Song s : songs) {
                writer.write(s.getFilepath());
                writer.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Playlist(String name, List<Song> songs) {
        this.name = name;
        if (songs != null) {
            for (Song song : songs) {
                songs.add(song);
            }
        }
    }

    public Playlist(String name, ObservableList<Song> songs) {
        this.name = name;
        if (songs != null) {
            this.songs = songs;
        }
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getName() {
        return name;
    }

    public void addSongUnimported(String filename) throws IOException {
        File f = new File(filename);
        addSongUnimported(f);
    }
    public void addSongUnimported(File file) throws IOException {
        try {
            
            addSong(new Song(Util.getTitle(file), Util.getArtist(file),
                    file.getAbsolutePath(), Util.getDurationAsString(file),
                    Util.getAlbum(file), Util.getDuration(file)));
        } catch (TagException e) {
            String current = file.getName();
            Util.print("Error getting properties from: " + current);
            Song s = new Song(current, "-", current, "-", "-", 0);
            addSong(s);
        }
    }
    
    public void addSong(Song s) {
//        System.err.println(s.toString());
        songs.add(s);
    }

    @Override
    public String toString() {
        return "Playlist";
    }

    public ObservableList<Song> getSongs() {
        return songs;
    }

//    public void addSongs(ObservableList<Song> songs) {
//        this.songs.addAll(songs);
//    }
}
