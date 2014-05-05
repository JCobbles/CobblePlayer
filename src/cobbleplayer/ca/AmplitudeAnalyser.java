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
package cobbleplayer.ca;

import cobbleplayer.utilities.ModalDialog;
import cobbleplayer.utilities.Util;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

/**
 *
 * @author Jacob
 */
public class AmplitudeAnalyser implements Runnable {

    short[] mix;
    short step_size = 1000;

    public AmplitudeAnalyser(short[] amplitudes) {
        mix = amplitudes;
    }

    @Override
    public void run() {

        short lower = 50, upper = 50;

        if (mix.length < 1000) {
            Button b = new Button("OK");
            b.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    ModalDialog.exit();
                }
            });
            List<Button> bs = new ArrayList<>();
            bs.add(b);
            new ModalDialog("Error", "Song is not long enought (length of mix < step size)", bs);
        } else {
            int idx = mix.length;
            while (idx > 2) {
                if (isWithinThresholds(mix[idx], mix[idx + 1])) {
                    Util.err(mix[idx] + " within thresholds");
                }
                idx--;
            }
        }
        /**
         * start with some step size and threshold limits start with first
         * amplitude add step size, if the next amplitude is within the limits
         * of the start then continue with step size if more than five steps
         * have gone through with the same step size add to a list with the
         * amplitude
         */
    }

    private boolean isWithinThresholds(int first, int second) {
        int upper = first + 5;
        int lower = first - 5;
        for (int i = upper; i > first; i--) {
            Util.err(i + "==" + second);
            if (i == second) {
                return true;
            }
        }
        Util.err("Second for loop:");
        for (int i = lower; i < first; i++) {
            Util.err(i + "==" + second);
            if (i == second) {
                return true;
            }
        }
        return false;
    }
}
