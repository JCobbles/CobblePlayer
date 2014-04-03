package cobbleplayer;

/**
 * An object containing all information pertaining to one song
 *
 * @author Jacob Moss
 */
public class Song {

    private int seconds;
    public final String title, artist, filepath, time, album;

    public Song(String title, String artist, String filepath, String time, String album, int sec) {
        this.title = title;
        this.artist = artist;
        this.filepath = filepath;
        this.time = time;
        this.album = album;
        seconds = sec;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getGenre() {
        return "";
    }
    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return time;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getAlbum() {
        return album;
    }

    @Override
    public String toString() {
        return title;
    }
}
