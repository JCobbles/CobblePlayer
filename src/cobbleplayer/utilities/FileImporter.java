/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.utilities;

import cobbleplayer.Playlist;
import cobbleplayer.Song;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.Dragboard;
import org.farng.mp3.TagException;

/**
 * @author Jacob Moss
 */
public class FileImporter implements Runnable {

    private ImportListener listener;
    private List<File> files;
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
        listener.finished(di);
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
            }
        }
        return null;
    }

}
