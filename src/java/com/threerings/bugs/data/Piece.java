//
// $Id$

package com.threerings.bugs.data;

import java.awt.Rectangle;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.DirectionCodes;

import com.threerings.presents.dobj.DSet;

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

    /** Returns this pieces current board bounds. The upper left of the
     * bound should always match the piece's location. <em>Note:</em> the
     * returned rectangle <em>must not</em> be modified. */
    public Rectangle getBounds ()
    {
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
    public boolean position (int x, int y, int orient, short boardTick)
    {
        boolean changed = false;
        if ((x != this.x) || (y != this.y) || (orient != this.orientation)) {
            changed = true;
            lastMoved = boardTick;
        }
        this.x = (short)x;
        this.y = (short)y;
        this.orientation = (short)orient;
        return changed;
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

    /** Contains the pieces current bounds. As the piece is updated, it
     * should maintain its current board bounds in this object. */
    protected transient Rectangle _bounds = new Rectangle();
}
