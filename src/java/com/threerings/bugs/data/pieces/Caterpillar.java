//
// $Id$

package com.threerings.bugs.data.pieces;

import java.awt.Point;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Terrain;

/**
 * Handles the state and behavior of the caterpillar piece.
 */
public class Caterpillar extends Piece
{
    /** Tracks the amount of grass this caterpillar has eaten. */
    public int grassEaten;

    @Override // documentation inherited
    public boolean react (BugsObject bugsobj, Piece[] pieces)
    {
        // if we've eaten three units of grass, burp up a leaf
        if (grassEaten >= 3) {
            // create a leaf piece and stick it at our head position
            Leaf leaf = new Leaf();
            leaf.init();
            leaf.position(x[0], y[0], NORTH);
            bugsobj.addToPieces(leaf);
            grassEaten -= 3;
            return true;
        }
        return false;
    }

    @Override // documentation inherited
    public Terrain modifyBoard (BugsBoard board, int x, int y)
    {
        // we eat tall grass and turn it into dirt
        if (board.getTile(x, y) == Terrain.TALL_GRASS) {
            grassEaten++;
            return Terrain.DIRT;
        } else {
            return super.modifyBoard(board, x, y);
        }
    }

    @Override // documentation inherited
    protected void createSegments (int sx, int sy, int sorient)
    {
        x = new short[] { (short)sx, 0, 0 };
        y = new short[] { (short)sy, 0, 0 };
        orientation = (short)sorient;

        switch (orientation) {
        case NORTH:
            x[1] = x[0];
            x[2] = x[0];
            y[1] = (short)(y[0]-1);
            y[2] = (short)(y[0]-2);
            break;

        case SOUTH:
            x[1] = x[0];
            x[2] = x[0];
            y[1] = (short)(y[0]+1);
            y[2] = (short)(y[0]+2);
            break;

        case EAST:
            x[1] = (short)(x[0]+1);
            x[2] = (short)(x[0]+2);
            y[1] = y[0];
            y[2] = y[0];
            break;

        case WEST:
            x[1] = (short)(x[0]-1);
            x[2] = (short)(x[0]-2);
            y[1] = y[0];
            y[2] = y[0];
            break;
        }
    }

    @Override // documentation inherited
    protected boolean canTraverse (Terrain terrain)
    {
        return (terrain == Terrain.TALL_GRASS ||
                super.canTraverse(terrain));
    }
}
