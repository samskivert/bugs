//
// $Id$

package com.threerings.bugs.data.pieces;

/**
 * A piece of food in the form of a drop of honey.
 */
public class HoneyDrop extends Food
{
    @Override // documentation inherited
    protected int startingEnergy ()
    {
        return 300;
    }

    @Override // documentation inherited
    public int takeEnergy (Piece eater)
    {
        // we always use up 75 units, but bees get more "value" from it
        energy -= 75;
        if (eater instanceof Bee) {
            return 125;
        } else {
            return 75;
        }
    }
}
