//
// $Id$

package com.threerings.bugs.data.pieces;

/**
 * A piece of food in the form of a drop of soda pop.
 */
public class SodaDrop extends Food
{
    @Override // documentation inherited
    public int getEnergy (Piece eater)
    {
        return 50;
    }
}
