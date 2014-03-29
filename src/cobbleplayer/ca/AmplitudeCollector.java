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

                byte[] temp = new byte[4];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                int i = 0;
                while (din.read(temp, 0, 4) != -1) {
                    if (decodedFormat.getChannels() == 2) {

                        dos.writeShort(((temp[1] * 256 + temp[0]) + (temp[3] * 256 + temp[2])) / 2); //average of two channels
                        i++;
                    }
                }

                byte[] bytes = bos.toByteArray();
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(bis);
                final short[] mix = new short[(bytes.length / 2)];

                int len = 0;
                int offset = 0, skip = mix.length / (int) Main.analyser.getWidth();
                
                while (1 > 0) {
                    while (offset < skip) {
                        offset += dis.skip(skip - offset);
                    }
                    len += skip;
                    offset = 0;
                    try {
                        mix[len] = dis.readShort();
                    } catch (ArrayIndexOutOfBoundsException e) { //end of stream
                        Util.err("End of stream");
                        break;
                    }

                    final int templen = len;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            series.getData().add(new XYChart.Data(templen, mix[templen]));
                        }
                    });
                }

                Util.err(len - skip + "  ==  " + bytes.length / 2 + " == " + i);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        
                        Util.err("Finished amplitude collection");
                        Notification.showPopupMessage("Collection has finished, proceeding with analysis", Main.getStage());
                        listener.ampCollectionFinished(mix, series);
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
