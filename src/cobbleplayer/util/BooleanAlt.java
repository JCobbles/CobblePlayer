/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.util;

/**
 *
 * @author Jacob
 * 
 * A boolean that can be used in nested classes
 */
public class BooleanAlt {

    private boolean bool;

    public BooleanAlt(boolean start) {
        bool = start;
    }

    public void fols() {
        bool = false;
    }

    public void troo() {
        bool = true;
    }

    public boolean is() {
        return bool;
    }
}
