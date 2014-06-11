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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.farng.mp3.TagException;

/**
 * @author Jacob Moss
 */
public class FileImporter implements Runnable {
    
    private final ImportListener listener;
    private final List<File> files;
    public DoubleProperty progressProperty;
    
    public FileImporter(ImportListener listener, List<File> files) {
        this.listener = listener;
        this.files = files;
        progressProperty = new SimpleDoubleProperty();
    }
    
    private void updateProgress(double value, double max) {
        Platform.runLater(() -> {
            progressProperty.set(value / max);
        });
        
    }
    
    @Override
    public void run() {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        int val = 0;
        for (File file : files) {
            val++;
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    val++;
                    listener.songImported(importSong(f));
                    updateProgress(val, files.length);
                }
            } else { //files
                listener.songImported(importSong(file));
            }
            updateProgress(val, files.size());
        }
        listener.importFinished();
    }
    
    private Song importSong(File file) {
        String extension = (file.getName().lastIndexOf('.') > 0)
                ? file.getName().substring(file.getName().lastIndexOf('.') + 1) : null;
        if (extension != null && extension.equalsIgnoreCase("mp3")) {
            try {
                return new Song(Util.getTitle(file), Util.getArtist(file), file.getAbsolutePath(),
                        Util.getDurationAsString(file), Util.getAlbum(file), Util.getDuration(file));
            } catch (IOException ex) {
                ex.printStackTrace();
                Util.log(ex.getLocalizedMessage());
                Util.log(ex.getStackTrace().toString());
            }
        }
        return null;
    }
    
}
