//
// $Id$

package com.threerings.bugs.data;

import com.threerings.bugs.client.PieceSprite;
import com.threerings.bugs.client.TreeSprite;

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
