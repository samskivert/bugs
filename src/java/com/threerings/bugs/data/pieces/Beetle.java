//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.BeetleSprite;
import com.threerings.bugs.client.sprites.PieceSprite;

/**
 * Handles the state and behavior of the beetle piece.
 */
public class Beetle extends Piece
{
    @Override // documentation inherited
    protected void createSegments (int sx, int sy)
    {
        x = new short[] { (short)sx, (short)(sx + REV_X_MAP[orientation]) };
        y = new short[] { (short)sy, (short)(sy + REV_Y_MAP[orientation]) };
    }

    @Override // documentation inherited
    public PieceSprite createSprite ()
    {
        return new BeetleSprite();
    }
}
