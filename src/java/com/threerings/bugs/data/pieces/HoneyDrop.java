//
// $Id$

package com.threerings.bugs.data.pieces;

/**
 * A piece of food in the form of a drop of honey.
 */
public class HoneyDrop extends Food
{
    @Override // documentation inherited
    public int getEnergy (Piece eater)
    {
        if (eater instanceof Bee) {
            return 125;
        } else {
            return 75;
        }
    }
}
