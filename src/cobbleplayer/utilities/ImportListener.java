/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.utilities;

import cobbleplayer.Song;

/**
 *
 * @author Jacob
 */
public interface ImportListener {

    void songImported(Song s, ModalDialog di);
    void finished(ModalDialog di);
}
