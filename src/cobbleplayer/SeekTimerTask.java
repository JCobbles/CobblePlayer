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
import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author Jacob
 */
public class SeekTimerTask extends TimerTask {

    private final MusicController con;
    private boolean stop = false, shorten = false, pause = false;
    private SeekTask parent;

    public SeekTimerTask(MusicController con, SeekTask parent) {
        this.con = con;
        this.parent = parent;
    }

    public void pause() {
        pause = true;
    }

    public void resume() {
        pause = false;
    }

    @Override
    public void run() {
        if (con.getCurrent() != null) {
            if (!pause && con.getPosition() != -1) {
                if (con.getPosition() == con.getCurrent().getSeconds()) {
                    Util.err(con.getCurrent().toString() + "   " + con.getPosition() + "     " + con.getCurrent().getSeconds());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            con.songEnded();

                        }
                    });
                    pause = true;
                }
//                Util.err(con.getCurrent().toString() + "" + con.getPosition() + "   " + con.getCurrent().getSeconds());
                updateProgress(con.getPosition(), con.getCurrent().getSeconds());
            } else if (con.getPosition() < con.getCurrent().getSeconds()) {
                pause = false;
            }
        }
    }

    private void updateProgress(double pos, double max) {
        parent.update(pos, max);
    }

}
