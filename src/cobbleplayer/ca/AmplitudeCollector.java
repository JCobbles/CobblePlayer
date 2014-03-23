package cobbleplayer.ca;

import static cobbleplayer.AnalysisController.ampChart;
import static cobbleplayer.AnalysisController.series;
import cobbleplayer.Main;
import cobbleplayer.utilities.ModalDialog;
import cobbleplayer.utilities.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
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
                        ampChart.getData().add(series);
                        ampChart.getXAxis().setLabel("Time / arbitrary units");
                        ampChart.getYAxis().setLabel("Amplitude / arbitrary units");
                        Util.err("Finished amplitude collection");
                        Button ok = new Button("OK");
                        ok.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent paramT) {
                                ModalDialog.exit();
                            }
                        });
                        List<Button> buttons = new ArrayList<>();
                        buttons.add(ok);
                        new ModalDialog("Finished", "Analysis has finished completely.", buttons, 210, 75);
                    }
                });

            }
        } catch (IOException | UnsupportedAudioFileException e) {
            Util.err("Error." + e.getLocalizedMessage());
        }
    }

}
