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
    /** Contain the piece id of the flower that pollenated us or zero.
     * Used to ensure we don't pollenate our pollenator. */
    public int pollenator;

    /** If the bee is carrying pollen, this will contain pieceId of the
     * flower whose pollen it is carrying. */
    public int pollen;

    // documentation inherited
    public boolean isFlyer ()
    {
        return true;
    }

    // documentation inherited
    public boolean maybeInteract (Piece other)
    {
        // bees interact with flowers
        if (other instanceof Flower) {
            Flower flower = (Flower)other;

            // if we are carrying pollen, it is of the appropriate type,
            // it is not from this flower, and this flower is not already
            // pollinated, do the deed!
            if (pollenator != 0 && pollenator != flower.pieceId &&
                flower.orientation == pollen && !flower.pollinated) {
                // we also inherit this flower's pollenator id
                pollenator = flower.pieceId;
                flower.pollinated = true;
                return true;

            } else if (pollen != flower.orientation) {
                // otherwise, we become pollinated with the pollen from
                // this flower
                pollenator = flower.pieceId;
                pollen = flower.orientation;
                return true;
            }
        }

        return false;
    }

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
