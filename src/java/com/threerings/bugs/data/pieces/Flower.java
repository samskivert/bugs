//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.FlowerSprite;
import com.threerings.bugs.client.sprites.PieceSprite;

/**
 * Handles the state and behavior of the flower piece.
 */
public class Flower extends BigPiece
{
    /** False if this flower has not been pollinated, true if it has. */
    public boolean pollinated;

    public Flower ()
    {
        super(2, 2);
    }        

    // documentation inherited
    public boolean preventsOverlap (Piece lapper)
    {
        return !lapper.isFlyer();
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new FlowerSprite();
    }
}
