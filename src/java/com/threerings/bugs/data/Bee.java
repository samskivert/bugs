//
// $Id$

package com.threerings.bugs.data;

import com.threerings.bugs.client.BeeSprite;
import com.threerings.bugs.client.PieceSprite;

/**
 * Handles the state and behavior of the bee piece.
 */
public class Bee extends Piece
{
    // documentation inherited
    public void enumerateLegalMoves (int x, int y, PointSet moves)
    {
        moves.add(x, y-2);
        moves.add(x-1, y-1);
        moves.add(x, y-1);
        moves.add(x+1, y-1);

        moves.add(x+2, y);
        moves.add(x+1, y);
        moves.add(x-1, y);
        moves.add(x-2, y);

        moves.add(x-1, y+1);
        moves.add(x, y+1);
        moves.add(x+1, y+1);
        moves.add(x, y+2);
    }

    // documentation inherited
    public boolean canMoveTo (BugsBoard board, int x, int y)
    {
        // by default, ensure that the location is exactly one unit away
        // from our current location
        if (Math.abs(this.x - x) + Math.abs(this.y - y) > 2) {
            return false;
        }

        // and make sure we can traverse our final location
        return canTraverse(board, x, y);
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new BeeSprite();
    }

    // documentation inherited
    protected boolean canTraverse (int terrain)
    {
        return true;
    }
}
