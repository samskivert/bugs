//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.AntHillSprite;
import com.threerings.bugs.client.sprites.PieceSprite;

/**
 * Provides a destination for ants on the board.
 */
public class AntHill extends Piece
{
    /** The number of ants that have entered this hill. */
    public int enteredAnts;

    // documentation inherited
    public boolean preventsOverlap (Piece lapper)
    {
        // ants are allowed to overlap (and thus enter) an anthill
        return !(lapper instanceof Ant);
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new AntHillSprite();
    }

    // documentation inherited
    protected int getWidth (int orient)
    {
        return 2;
    }

    // documentation inherited
    protected int getHeight (int orient)
    {
        return 2;
    }
}
