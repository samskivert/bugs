//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.client.sprites.TreeSprite;

/**
 * Handles the state and behavior of the tree piece.
 */
public class Tree extends BigPiece
{
    public Tree ()
    {
        super(3, 3);
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new TreeSprite();
    }
}
