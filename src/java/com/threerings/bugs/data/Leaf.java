//
// $Id$

package com.threerings.bugs.data;

import com.threerings.bugs.client.LeafSprite;
import com.threerings.bugs.client.PieceSprite;

/**
 * Handles the state and behavior of the leaf piece.
 */
public class Leaf extends Piece
{
    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new LeafSprite();
    }
}
