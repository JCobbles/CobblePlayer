/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.ca;

import static cobbleplayer.AnalysisController.position;
import static cobbleplayer.GUIController.samples;
import cobbleplayer.utilities.Util;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jacob
 */
public class FrequencyCollector implements Runnable {

//    private File song;
    private CollectionListener listener;
    private Minim minim;
    private AudioPlayer song;

    public FrequencyCollector(AudioPlayer song) {
        this.song = song;
    }
    List<List<Float>> listOfSamples = new ArrayList<>();

    @Override
    public void run() {
//        if(song.exists()) {
//            try{
//                AudioInputStream in = AudioSystem.getAudioInputStream(song);
//                AudioInputStream din = null;
//                AudioFormat baseFormat = in.getFormat();
//                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                        baseFormat.getSampleRate(), //sample rate
//                        16, //sample size (bits)
//                        baseFormat.getChannels(), //channels
//                        baseFormat.getChannels() * 2,//frame size
//                        baseFormat.getSampleRate(), //frame rate
//                        false);                     //bigendian?
//                din = AudioSystem.getAudioInputStream(decodedFormat, in);
//
//                byte[] temp = new byte[4];
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                DataOutputStream dos = new DataOutputStream(bos);
//                int i = 0;
//                while (din.read(temp, 0, 4) != -1) {
//                    if (decodedFormat.getChannels() == 2) {
//
//                        dos.writeShort(((temp[1] * 256 + temp[0]) + (temp[3] * 256 + temp[2])) / 2); //average of two channels
//                        i++;
//                    }
//                }
//
//                byte[] bytes = bos.toByteArray();
//                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//                DataInputStream dis = new DataInputStream(bis);
//                
//               
//            } catch(UnsupportedAudioFileException | IOException e) {
//                
//            }
//            
//        }
//        AudioSample s = minim.loadSample (song.getAbsolutePath(), 2048); 
//        s.trigger(); 
//        float[] sampleLeftChannel = s.getChannel( AudioSample.LEFT );
//
//        
//        
//        
//                final List<Float> one = new ArrayList<>();
//        final List<Float> two = new ArrayList<>();
//        final List<Float> three = new ArrayList<>();
//        final List<Float> four = new ArrayList<>();
//        final List<Float> five = new ArrayList<>();
//        final List<Float> six = new ArrayList<>();

        //old:
        final FFT fft;

        song.play();

        fft = new FFT(song.bufferSize(), song.sampleRate());

        final int frequencyBands = fft.specSize(); //number of frequency bands produced by the fft in the song
        final int songLength = song.length() / 1000;
        final int step = song.length() / (samples + 1);
        Util.err("Step: " + step + " songlength:" + songLength);
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
            position.setText("" + song.position() / 1000);
            one.add(fft.calcAvg(20, 100));
            two.add(fft.calcAvg(100, 300));
            three.add(fft.calcAvg(300, 900));
            four.add(fft.calcAvg(900, 1200));
            five.add(fft.calcAvg(1200, 16000));
            six.add(fft.calcAvg(16000, 20000));
        }
        song.close();
        listener.freqCollectionFinished(one, two, three, four, five, six);
//        
        Util.err("Finished frequency collection");
    }

    private boolean isAvgNull(FFT fft, AudioPlayer song) {
        fft.forward(song.mix);
        return fft.calcAvg(20, 100000) == 0;
    }

    public void setListener(CollectionListener list) {
        listener = list;
    }
}
