package cobbleplayer.ca;

import cobbleplayer.Main;
import cobbleplayer.utilities.Notification;
import cobbleplayer.utilities.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Jacob Moss
 */
public class AmplitudeCollector implements Runnable {
    
    private final File song;
    private XYChart.Series series = new XYChart.Series();
    private CollectionListener listener;
    
    public AmplitudeCollector(File song) {
        this.song = song;
    }
    
    @Override
    public void run() {
        try {
            if (song.exists()) {
                AudioInputStream in = AudioSystem.getAudioInputStream(song);
                AudioInputStream din = null;
                AudioFormat baseFormat = in.getFormat();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(), //sample rate
                        16, //sample size (bits)
                        baseFormat.getChannels(), //channels
                        baseFormat.getChannels() * 2,//frame size
                        baseFormat.getSampleRate(), //frame rate
                        false);                     //bigendian?
                din = AudioSystem.getAudioInputStream(decodedFormat, in);

                /*
                 din: the audio byte stream containing amplitudes
                 temp: byte array to store the next four bytes in the din
                 dos: data stream to write shorts onto
                 */
                byte[] temp = new byte[4];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                int i = 0;
                while (din.read(temp, 0, 4) != -1) {
                    
                    dos.writeShort((temp[1] * 256 + temp[0]) / 2 + (temp[3] * 256 + temp[2]) / 2); //average of two channels (divided by two twice to give make sure number does not wrap around)
                    i++;
                    
                }
                
                byte[] bytes = bos.toByteArray();
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(bis);
                
                int len = 0, index = 0;
                int offset = 0, skip = (bytes.length / 2) / (int) Main.analyser.getWidth();
                final short[] mix = new short[((bytes.length / 2) / skip)];
                int disLength = bytes.length / 2; //a prediction on how large the dos stream is 
                float xInterval = (float) Util.getDuration(song) / (float) mix.length; //step-size for x axis
                final float add = xInterval; //copy of the interval so that the it can be added on to the interval each time
                Util.err(Util.getDuration(song) + "/" + mix.length);
                Util.err(xInterval);
                while (1 > 0) { //infinite, only stopped when the ArrayIndexOutOfBoundsException is thrown (or program crashes!)
                    while (offset < skip) {
                        offset += dis.skip(skip - offset);
                    }
                    len += skip;
                    offset = 0;
                    xInterval += add;
                    try {
                        mix[index] = dis.readShort();
                    } catch (ArrayIndexOutOfBoundsException e) { //end of stream
                        Util.err("End of stream");
                        break;
                    }
                    
                    series.getData().add(new XYChart.Data(xInterval, mix[index]));
                    index++;
                }

                //==== Predictions should equal real values ====//
                Util.err(disLength + " == " + i);
                Util.err(index + "  ==  " + mix.length);
                //==============================================//
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.ampCollectionFinished(mix, series); //finish
                    }
                });
                
            }
        } catch (IOException | UnsupportedAudioFileException e) {
            Util.err("Error." + e.getLocalizedMessage());
        }
    }
    
    public void setListener(CollectionListener list) {
        listener = list;
    }
}
