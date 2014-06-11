/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer;

import java.util.Timer;
import java.util.TimerTask;
import javafx.concurrent.Task;

/**
 *
 * @author Jacob
 */
public class UpdateTask extends Task<Void> {

    private final Timer timer = new Timer();
    private final TimerTask task;

    public UpdateTask(TimerTask task) {
        this.task = task;
    }

    @Override
    protected Void call() throws Exception {
        timer.scheduleAtFixedRate(task, 0, 60000);
        return null;
    }

}
