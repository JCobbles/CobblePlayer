/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.utilities;

import cobbleplayer.GUIController;
import cobbleplayer.Song;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jacob
 */
public class TrackChange {

    private final List<Song> songs = new ArrayList<>();
    private final GUIController con;
    /**
     * pointer points to the index of the current song being played
     */
    private int pointer;

    public int getPointer() {
        return pointer;
    }

    public TrackChange(GUIController con) {
        this.con = con;
    }

    /**
     * Add a song to the list
     *
     * @param s
     */
    public void addSong(Song s) {
        if (songs.size() > con.musicTable.getItems().size() - 2) {
            songs.clear();
            Util.err("Clearing saved trackchange songs");
        }
        if (s != null && !songs.contains(s) && !Util.filterArtistContains(GUIController.filters, s.getArtist())) {
            songs.add(s);
        }
    }

    public void forceAddSong(Song s) {
        if (songs.size() > con.musicTable.getItems().size() - 2) {
            songs.clear();
            Util.err("Clearing saved trackchange songs");
        }
        if (s != null && !Util.filterArtistContains(GUIController.filters, s.getArtist())) {
            songs.add(s);
        }
    }

    /**
     * Must be called when a new song is played so the getNext() and
     * getPrevious() methods work
     *
     * @param s the song to add
     */
    public void adjustPointer(Song s) {
//        pointer = songs.lastIndexOf(s);
        pointer = songs.size() - 1;
    }

    public List<Song> getsongs() {
        return songs;
    }

    /**
     * Uses the pointer to get the previous played song
     *
     * @return null if the previous song is outside the range of the list
     */
    public Song getPrevious() {
        try {
            pointer -= 1;
            return songs.get(pointer);
        } catch (IndexOutOfBoundsException e) {
            pointer += 1;
            return null;
        }
    }

    /**
     * Checks if the previous song is not null
     *
     * @return true if there is a previous song to go to
     */
    public boolean isPrevious() {
        if (getPrevious() != null) {
            pointer += 1;
            return true;
        } // else:
        return false;
    }

    /**
     * Checks if the next song is not null
     *
     * @return true if there is a next song to go to
     */
    public boolean isNext() {
        if (getNext() != null) {
            pointer -= 1;
            return true;
        } // else:
        return false;
    }

    /**
     * Uses the pointer to get the next song to play
     *
     * @return null if the next song is outside the range of the list
     */
    public Song getNext() {
        try {
            pointer += 1;
            return songs.get(pointer);
        } catch (IndexOutOfBoundsException e) {
            pointer -= 1;
            return null;
        }
    }
}
