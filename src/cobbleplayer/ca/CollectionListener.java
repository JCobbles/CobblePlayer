package cobbleplayer.ca;

import java.util.List;

/**
 *
 * @author Jacob Moss
 */
public interface CollectionListener {

    void ampCollectionFinished(short[] amplitudes);

    void freqCollectionFinished(List<Float> one, List<Float> two, List<Float> three, List<Float> four, List<Float> five, List<Float> six);
}
