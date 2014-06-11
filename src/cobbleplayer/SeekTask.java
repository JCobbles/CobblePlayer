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
import java.util.Timer;
import javafx.concurrent.Task;

/**
 * Should replace SeekThread, optimised using timers and tasks
 *
 * @author Jacob
 */
public class SeekTask extends Task<Void> {

    private final MusicController con;
    private boolean stop = false;
    private SeekTimerTask task;
    private Timer timer;

    public SeekTask(MusicController con) {
        this.con = con;
        task = new SeekTimerTask(con, this);
        timer = new Timer();
    }

    public void pause() {
        task.pause();
    }

    public void adjustTimerSetting(long newPeriod) {
        task.cancel();
        task = new SeekTimerTask(con, this);
        timer.scheduleAtFixedRate(task, 0, newPeriod);
    }

    public void resume() {
        task.resume();
    }

    @Override
    protected Void call() throws Exception {

        timer.scheduleAtFixedRate(task, 0, 600);
        return null;
    }

    public void update(double pos, double max) {
        updateProgress(pos, max);
    }
}
