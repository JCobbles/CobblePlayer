package cobbleplayer.utilities;

import cobbleplayer.Playlist;
import cobbleplayer.utilities.Util;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jacob
 */
public class PlaylistHolder implements Serializable {

    private List<Playlist> playlists = new ArrayList<>();

    public PlaylistHolder(List<Playlist> playlists) {
        this.playlists = playlists;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Util.PLAYLIST_FILENAME));
            for (Playlist p : playlists) { //give writer to playlist
                p.write(writer); //so for each song within it can write data for
                writer.newLine();
            }
            writer.write(Util.END_CODE);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }
}
