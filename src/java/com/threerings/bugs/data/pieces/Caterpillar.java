//
// $Id$

package com.threerings.bugs.data.pieces;

import java.awt.Point;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Terrain;

import com.threerings.bugs.client.sprites.CaterpillarSprite;
import com.threerings.bugs.client.sprites.PieceSprite;

/**
 * Handles the state and behavior of the caterpillar piece.
 */
public class Caterpillar extends Piece
{
    /** Tracks the amount of grass this caterpillar has eaten. */
    public int grassEaten;

    @Override // documentation inherited
    public PieceSprite createSprite ()
    {
        return new CaterpillarSprite();
    }

    @Override // documentation inherited
    public boolean react (BugsObject bugsobj, Piece[] pieces)
    {
        // if we've eaten three units of grass, burp up a leaf
        if (grassEaten >= 3) {
            // create a leaf piece and stick it at our head position
            Leaf leaf = new Leaf();
            leaf.init();
            leaf.position(x[0], y[0]);
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
    protected void createSegments (int sx, int sy)
    {
        x = new short[] { (short)sx, (short)(sx + REV_X_MAP[orientation]),
                          (short)(sx + 2*REV_X_MAP[orientation]) };
        y = new short[] { (short)sy, (short)(sy + REV_Y_MAP[orientation]),
                          (short)(sy + 2*REV_Y_MAP[orientation]) };
    }

    @Override // documentation inherited
    protected boolean canTraverse (Terrain terrain)
    {
        return (terrain == Terrain.TALL_GRASS ||
                super.canTraverse(terrain));
    }
}
