//
// $Id$

package com.threerings.bugs.data;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.DirectionCodes;

import com.threerings.presents.dobj.DSet;

import com.threerings.bugs.client.PieceSprite;

/**
 * Contains the basic state and interface for a piece that lives on the
 * board.
 */
public abstract class Piece extends SimpleStreamableObject
    implements DSet.Entry, DirectionCodes
{
    /** Uniquely identifies each piece in the game. */
    public int pieceId;

    /** The current x location of this piece. */
    public short x;

    /** The current y location of this piece. */
    public short y;

    /** This piece's orientation. */
    public short orientation;

    /** The game tick on which this piece was last moved. */
    public short lastMoved;

    /**
     * Returns this pieces current board bounds. The upper left of the
     * bound should always match the piece's location. <em>Note:</em> the
     * returned rectangle <em>must not</em> be modified.
     */
    public Rectangle getBounds ()
    {
        if (_bounds == null) {
            _bounds = new Rectangle(
                x, y, getWidth(orientation), getHeight(orientation));
        }
        return _bounds;
    }

    /**
     * Updates this pieces position and orientation. If the information
     * represents a change to the piece's position (or orientation, or
     * both) the {@link #lastMoved} field will be updated with the
     * <code>boardTick</code> value supplied to indicate that it was moved
     * on this tick.
     *
     * @return true if the piece's position changed, false if not.
     */
    public boolean position (int nx, int ny, int orient, short boardTick)
    {
        if ((nx != x) || (ny != y) || (orient != orientation)) {
            x = (short)nx;
            y = (short)ny;
            orientation = (short)orient;
            lastMoved = boardTick;
            _bounds = null; // force a bounds update
            return true;
        }
        return false;
    }

    /**
     * Some pieces can consume other pieces by moving on top of them. When
     * they attempt to occupy the same space, this method will be
     * called. If the piece should consume the other, it should modify its
     * internal state to reflect the consumption and return true, in which
     * case the consumed piece will be removed from the board and this
     * piece will be updated. Otherwise it can return false in which case
     * both pieces will remain unaffected.
     */
    public boolean maybeConsume (Piece other)
    {
        return false;
    }

    /**
     * Verifies that a move to the specified location is within the
     * piece's capabilities (ie. not too far, doesn't turn illegally,
     * doesn't cross illegal tiles).
     */
    public boolean canMoveTo (BugsBoard board, int x, int y)
    {
        // by default, ensure that the location is exactly one unit away
        // from our current location
        if (Math.abs(this.x - x) + Math.abs(this.y - y) != 1) {
            return false;
        }

        // and make sure we can traverse our final location
        return canTraverse(board, x, y);
    }

    /**
     * Returns true if this bug can traverse the board at the specified
     * coordinates.
     */
    public boolean canTraverse (BugsBoard board, int x, int y)
    {
        // ensure that our entire footprint is on traversable tiles
        Rectangle bounds = getBounds();
        for (int px = 0; px < bounds.width; px++) {
            for (int py = 0; py < bounds.height; py++) {
                if (!canTraverse(board.getTile(px+x, py+y))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Allows a piece to modify the board terrain as a result of landing
     * on it.
     *
     * @return {@link BugsBoard#NONE} if the piece does not wish to modify
     * the terrain, or the terrain code for the new terrain type if it
     * does.
     */
    public int modifyBoard (BugsBoard board, int x, int y)
    {
        return BugsBoard.NONE;
    }

    /**
     * Creates and adds {@link Point} objects containing the set of legal
     * moves for this piece from the specified starting location. These
     * moves need not account for terrain or other potential blockage.
     */
    public void enumerateLegalMoves (int x, int y, Collection<Point> moves)
    {
        // the default piece can move one in any of the four cardinal
        // directions
        moves.add(new Point(x+1, y));
        moves.add(new Point(x-1, y));
        moves.add(new Point(x, y+1));
        moves.add(new Point(x, y-1));
    }

    /**
     * Creates the appropriate derivation of {@link PieceSprite} to render
     * this piece.
     */
    public PieceSprite createSprite ()
    {
        return new PieceSprite();
    }

    // documentation inherited from interface DSet.Entry
    public Comparable getKey ()
    {
        return pieceId;
    }

    // documentation inherited
    public int hashCode ()
    {
        return pieceId;
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        return pieceId == ((Piece)other).pieceId;
    }

    /**
     * Returns true if this piece can traverse the specified type of
     * terrain.
     */
    protected boolean canTraverse (int terrain)
    {
        return (terrain == BugsBoard.DIRT ||
                terrain == BugsBoard.LEAF_BRIDGE);
    }

    /**
     * Returns the width of this piece in the specified orientation.
     */
    protected int getWidth (int orient)
    {
        return 1;
    }

    /**
     * Returns the height of this piece in the specified orientation.
     */
    protected int getHeight (int orient)
    {
        return 1;
    }

    /** Contains the pieces current bounds. As the piece is updated, it
     * should maintain its current board bounds in this object. */
    protected transient Rectangle _bounds;
}
