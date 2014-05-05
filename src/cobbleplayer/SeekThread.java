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

import javafx.application.Platform;

/**
 *
 * @author Jacob
 */
@Deprecated
public class SeekThread implements Runnable {

    private final MusicController con;
    private boolean stop = false, shorten = false;
    public boolean pause = false;

    public SeekThread(MusicController con) {
        this.con = con;
    }

    public SeekThread(MusicController con, boolean shorten) {
        this.con = con;
        this.shorten = shorten;
    }

    public void end() {
        stop = true;
    }

    public void pause() {
        pause = true;
    }

    public void resume() {
        pause = false;
    }

    @Override
    public void run() {
        Song curSong = con.getCurrent();
        while ((con.compareSong(curSong) && con.getPosition() < curSong.getSeconds()) && !stop) {

            con.update(pause);

//            System.err.println(curSong.getPositionAsString() + "  ,  " + curSong.getDuration());
//            if (shorten) {
//
//            }
            if (!con.compareSong(curSong)) {
                break;
            }
            if (con.getPosition() >= curSong.getSeconds()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        con.songEnded();
                    }
                });
                break;
            }

        }

    }
}
