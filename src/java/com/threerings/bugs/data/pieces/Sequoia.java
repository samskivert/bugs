//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.client.sprites.TreeSprite;

/**
 * Handles the state and behavior of the tree piece.
 */
public class Sequoia extends BigPiece
{
    public Sequoia ()
    {
        super(5, 5);
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new TreeSprite(5, 5);
    }
}
