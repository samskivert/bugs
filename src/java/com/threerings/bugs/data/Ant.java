//
// $Id$

package com.threerings.bugs.data;

import com.threerings.bugs.client.AntSprite;
import com.threerings.bugs.client.PieceSprite;

import static com.threerings.bugs.Log.log;

/**
 * Handles the state and behavior of the ant piece.
 */
public class Ant extends Piece
{
    /** Indicates whether this ant is carrying a leaf. */
    public boolean enleafed;

    public Ant ()
    {
    }

    // documentation inherited
    public boolean maybeConsume (Piece other)
    {
        log.info("Consume? " + this + " <-- " + other);
        if (other instanceof Leaf && !enleafed) {
            enleafed = true;
            return true;
        }
        return false;
    }

    // documentation inherited
    public int modifyBoard (BugsBoard board, int x, int y)
    {
        // if we have a leaf and landed on water, build a bridge
        if (enleafed && board.getTile(x, y) == BugsBoard.WATER) {
            enleafed = false;
            return BugsBoard.LEAF_BRIDGE;
        } else {
            return super.modifyBoard(board, x, y);
        }
    }

    // documentation inherited
    protected boolean canTraverse (int terrain)
    {
        if (terrain == BugsBoard.WATER && enleafed) {
            return true;
        } else {
            return super.canTraverse(terrain);
        }
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new AntSprite();
    }
}
