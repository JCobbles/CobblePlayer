/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cobbleplayer.old;

/**
 *
 * @author Jacob
 */
public class Olds {
    private void oldTrackChange260514() {
//        if (shuffle) {
//                Song toPlay = null;
//                if (next && trackChange.isNext()) {
//                    Util.err("Next and trackchange is not null shuffle");
//                    toPlay = trackChange.getNext();
//                } else if (!next && trackChange.isPrevious()) {
//                    Util.err("Previous and trackchange is not null shuffle");
//                    toPlay = trackChange.getPrevious();
//                } else {
//                    toPlay = (Song) musicTable.getItems().get(Util.randInt(0, musicTable.getItems().size()));
//                    while (trackChange.getsongs().contains(toPlay) && Util.filterArtistContains(filters, toPlay.getArtist())) {
//                        toPlay = (Song) musicTable.getItems().get(Util.randInt(0, musicTable.getItems().size()));
//                    }
//                    Util.err("No next so shuffling");
//                }
//                Util.checkArtistFiltersAreUpdated(GUIController.filters);
//                musicController.play(toPlay);
////                if (addSongToTrackChange) {
//                trackChange.addSong(toPlay);
//                trackChange.adjustPointer(toPlay);
////                }
//            } else {// !shuffle
//                int i = -1;
//                if (next && !trackChange.isNext()) {
//                    Util.err("next and no next history");
//                    i = musicTable.getItems().indexOf(musicController.getCurrent()) + 1;
//                    while (Util.filterArtistContains(filters, ((Song) musicTable.getItems().get(i)).getArtist())) {
//                        i++;
//                    }
//                } else if (!next && !trackChange.isPrevious()) {
//                    Util.err("previous and no previous history");
//                    i = musicTable.getItems().indexOf(musicController.getCurrent()) - 1;
//                    while (Util.filterArtistContains(filters, ((Song) musicTable.getItems().get(i)).getArtist())) {
//                        i--;
//                    }
//                } else { //trackchange next and previous not null
//                    if (next) {
//                        Util.err("Next and trackchange is not null");
//                        musicController.play(trackChange.getNext());
//                    } else {
//                        Util.err("Previous and trackchange is not null");
//                        musicController.play(trackChange.getPrevious());
//                    }
//                }
//                if (i != -1) {
//                    if (i < musicTable.getItems().size()) {
//                        Song toPlay = (Song) musicTable.getItems().get(i);
//                        musicController.play(toPlay);
//                        trackChange.addSong(toPlay);
//                        trackChange.adjustPointer(toPlay);
//                    } else {
//                        play((Song) musicTable.getItems().get(0));
//                    }
//                }
//            }
    }
}

//try {
//                                        List<Playlist> playlists = new ArrayList<>();
//                                        BufferedReader br = new BufferedReader(new FileReader(Util.PLAYLIST_FILENAME));
//                                        String line;
//                                        Playlist curp = null;
//                                        while ((line = br.readLine()) != null) {
//                                            if (!line.isEmpty() && !line.equals("") && !line.equals(Util.END_CODE)) {
////                                                Util.err("Line: " + line);
//                                                if (line.contains(Util.NEW_BIT_CODE)) {
//                                                    line = line.substring(Util.NEW_BIT_CODE.length());
//                                                    if (curp == null) {
//                                                        curp = new Playlist(line, null);
//                                                    } else {
//                                                        playlists.add(curp);
//                                                        curp = new Playlist(line, null);
//                                                    }
//                                                } else {
//                                                    try {
//                                                        curp.addSongUnimported(line);
//                                                    } catch (IOException e) {
////                                                        Util.err(e.getLocalizedMessage());
//                                                    }
//                                                }
//                                            } else if (line.equals(Util.END_CODE)) {
//                                                playlists.add(curp);
//                                            }
//                                        }
//                                        br.close();
//                                        System.err.println("Adding " + playlists.size() + " playlists...");
//                                        GUIController.setPlaylists(playlists);
//                                    } catch (IOException e) {
//                                        Util.err(e.getLocalizedMessage());
//                                    }