/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.ca;

import cobbleplayer.AnalysisController;
import static cobbleplayer.AnalysisController.position;
import static cobbleplayer.GUIController.samples;
import cobbleplayer.utilities.Util;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jacob
 */
public class FrequencyCollector implements Runnable {

    private AudioPlayer song;
    private CollectionListener listener;

    public FrequencyCollector(AudioPlayer song) {
        this.song = song;
    }
    List<List<Float>> listOfSamples = new ArrayList<>();

    @Override
    public void run() {
        final FFT fft;

        song.play();

        fft = new FFT(song.bufferSize(), song.sampleRate());

        final int frequencyBands = fft.specSize(); //number of frequency bands produced by the fft in the song
        final int songLength = song.length() / 1000;
        final int step = song.length() / (samples + 1);
        Util.err("Step: " + step + " songlength:" + songLength);
        Util.err("freq bands: " + frequencyBands);
        final List<Float> one = new ArrayList<>();
        final List<Float> two = new ArrayList<>();
        final List<Float> three = new ArrayList<>();
        final List<Float> four = new ArrayList<>();
        final List<Float> five = new ArrayList<>();
        final List<Float> six = new ArrayList<>();
        while (song.position() < song.length() - step) {
            song.skip(step);
            while (isAvgNull(fft, song)) { //wait for mix audiobuffer to fill
//                System.err.println("Mix empty");
            }
            fft.forward(song.mix);
            position.setText("" + song.position() * 1000);
            one.add(fft.calcAvg(20, 1000));
            two.add(fft.calcAvg(1000, 3000));
            three.add(fft.calcAvg(3000, 10000));
            four.add(fft.calcAvg(10000, 30000));
            five.add(fft.calcAvg(30000, 60000));
            six.add(fft.calcAvg(60000, 100000));
        }
        song.close();
        listener.collectionFinished(one, two, three, four, five, six);
        
        Util.err("Finished frequency collection");
    }

    private boolean isAvgNull(FFT fft, AudioPlayer song) {
        fft.forward(song.mix);
        return fft.calcAvg(20, 100000) == 0;
    }

    private void addSampleList(List<Float> list) {
        listOfSamples.add(list);
    }
    public void setListener(CollectionListener list) {
        listener = list;
    }
}
