/**
 * Copyright (C) 2014 Jacob Moss
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *
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
            writer.write(Util.NEW_BIT_CODE + name);
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

    public void removeAll(ObservableList<Song> songsToRemove) {
        if (songs != null) {
            songs.removeAll(songsToRemove);
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
        } catch (IOException e) {
//            e.printStackTrace();
            String current = file.getName();
            Util.print("Error getting properties from: " + current);
            Song s = new Song(current, "-", current, "-", "-", 0);
            addSong(s);
        }
    }

    public void addSong(Song s) {
//        System.err.println(s.toString());
        try {
            if (songs != null && s != null) {
            songs.add(s);
        }
        } catch (NullPointerException e) {
            Util.log(e.getLocalizedMessage());
        }
        
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
