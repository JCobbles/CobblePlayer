package cobbleplayer.utilities;

import cobbleplayer.Song;

/**
 *
 * @author Jacob
 */
public interface ImportListener {

    void songImported(Song s);
    void importFinished();
}
