//
// $Id$

package com.threerings.bugs.data.pieces;

/**
 * A piece of food in the form of a drop of soda pop.
 */
public class SodaDrop extends Food
{
    @Override // documentation inherited
    protected int startingEnergy ()
    {
        return 250;
    }

    @Override // documentation inherited
    public int takeEnergy (Piece eater)
    {
        energy -= 50;
        return 50;
    }
}
