//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.LeafSprite;
import com.threerings.bugs.client.sprites.PieceSprite;

/**
 * Handles the state and behavior of the leaf piece.
 */
public class Leaf extends Piece
{
    // documentation inherited
    public boolean preventsOverlap (Piece lapper)
    {
        return false;
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new LeafSprite();
    }
}
