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
    public PieceSprite createSprite ()
    {
        return new BeetleSprite();
    }

    @Override // documentation inherited
    protected void createSegments (int sx, int sy)
    {
        x = new short[] { (short)sx, (short)(sx + REV_X_MAP[orientation]) };
        y = new short[] { (short)sy, (short)(sy + REV_Y_MAP[orientation]) };
    }

    @Override // documentation inherited
    protected void updatePosition (int nx, int ny)
    {
        // if we are moving backwards, do some special business
        if (nx == x[1] && ny == y[1]) {
            // move backwards rigidly without changing orientation
            x[0] += REV_X_MAP[orientation];
            y[0] += REV_Y_MAP[orientation];
            x[1] += REV_X_MAP[orientation];
            y[1] += REV_Y_MAP[orientation];

        } else {
            super.updatePosition(nx, ny);
        }
    }
}
