/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer;

/**
 *
 * @author Jacob
 */
public class SeekThread implements Runnable {

    private final MusicController con;
    private boolean stop = false, shorten = false;

    public SeekThread(MusicController con) {
        this.con = con;
    }
    
    public SeekThread(MusicController con, boolean shorten) {
        this.con = con;
        this.shorten = shorten;
    }

    public void end() {
        stop = true;
    }

    @Override
    public void run() {
        Song curSong = con.getCurrent();
        while ((con.compareSong(curSong) && con.getPosition() < curSong.getSeconds()) && !stop) {
            con.update();
//            System.err.println(curSong.getPositionAsString() + "  ,  " + curSong.getDuration());
            
            if(shorten) {
                
            }
            if (!con.compareSong(curSong)) {
                break;
            }
            if (con.getPosition() >= curSong.getSeconds()) {
                con.songEnded();
                break;
            }

        }

    }
}