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
package cobbleplayer.utilities;

import cobbleplayer.Song;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.farng.mp3.TagException;

/**
 * @author Jacob Moss
 */
public class FileImporter implements Runnable {

    private final ImportListener listener;
    private final List<File> files;
    private ModalDialog di;

    public FileImporter(ImportListener listener, List<File> files, ModalDialog dialog) {
        this.listener = listener;
        this.files = files;
        di = dialog;
    }

    @Override
    public void run() {
        for (File file : files) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                int idx = files.length - 1;
                while (idx != -1) {
//                    addFileToList(files[idx]);
                    listener.songImported(importSong(files[idx]), di);
                    
                    idx--;
                }
            } else { //files
//                addFileToList(file);
                listener.songImported(importSong(file), di);
            }
        }
        listener.importFinished(di);
    }

    private Song importSong(File file) {
        String extension = (file.getName().lastIndexOf('.') > 0)
                ? file.getName().substring(file.getName().lastIndexOf('.') + 1) : null;
        if (extension != null && extension.equalsIgnoreCase("mp3")) {
            try {
                return new Song(Util.getTitle(file), Util.getArtist(file), file.getAbsolutePath(),
                        Util.getDurationAsString(file), Util.getAlbum(file), Util.getDuration(file));
            } catch (IOException | TagException ex) {
                Util.log(ex.getLocalizedMessage());
                Util.log(ex.getStackTrace().toString());
            }
        }
        return null;
    }

}
