//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.client.sprites.TreeSprite;

/**
 * Handles the state and behavior of the tree piece.
 */
public class Tree extends Piece
{
    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new TreeSprite();
    }

    // documentation inherited
    protected int getWidth (int orient)
    {
        return 3;
    }

    // documentation inherited
    protected int getHeight (int orient)
    {
        return 3;
    }
}
