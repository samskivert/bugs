//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.BeeSprite;
import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.PointSet;
import com.threerings.bugs.data.Terrain;

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
        // we can move up to two squares in a turn
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
    protected boolean canTraverse (Terrain terrain)
    {
        return true;
    }
}
