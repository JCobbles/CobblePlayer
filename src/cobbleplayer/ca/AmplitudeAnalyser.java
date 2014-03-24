/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.ca;

/**
 *
 * @author Jacob
 */
public class AmplitudeAnalyser implements Runnable {
    short[] amplitudes;
    public AmplitudeAnalyser(short[] amplitudes) {
        this.amplitudes = amplitudes;
    }

    @Override
    public void run() {
        /**
         * start with some step size and threshold limits
         * start with first amplitude
         * add step size, if the next amplitude is within the limits of the start then continue with step size
         * if more than five steps have gone through with the same step size add to a list with the amplitude
         */
    }

}
