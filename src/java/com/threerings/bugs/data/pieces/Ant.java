//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.AntSprite;
import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Terrain;

import static com.threerings.bugs.Log.log;

/**
 * Handles the state and behavior of the ant piece.
 */
public class Ant extends Piece
    implements PlayerPiece
{
    /** Indicates whether this ant is carrying a leaf. */
    public boolean enleafed;

    @Override // documentation inherited
    public Interaction maybeInteract (Piece other)
    {
        if (other instanceof Leaf && !enleafed) {
            enleafed = true;
            return Interaction.CONSUMED;
        } else if (other instanceof AntHill) {
            ((AntHill)other).enteredAnts++;
            return Interaction.ENTERED;
        }
        return super.maybeInteract(other);
    }

    @Override // documentation inherited
    public Terrain modifyBoard (BugsBoard board, int x, int y)
    {
        // if we have a leaf and landed on water, build a bridge
        if (enleafed && board.getTile(x, y) == Terrain.WATER) {
            enleafed = false;
            return Terrain.LEAF_BRIDGE;
        } else {
            return super.modifyBoard(board, x, y);
        }
    }

    @Override // documentation inherited
    public PieceSprite createSprite ()
    {
        return new AntSprite();
    }

    @Override // documentation inherited
    protected boolean canTraverse (Terrain terrain)
    {
        if (terrain == Terrain.WATER && enleafed) {
            return true;
        } else {
            return super.canTraverse(terrain);
        }
    }
}
