//
// $Id$

package com.threerings.bugs.data;

import java.awt.Rectangle;

import com.threerings.io.SimpleStreamableObject;

/**
 * Contains the basic state and interface for a piece that lives on the
 * board.
 */
public abstract class Piece extends SimpleStreamableObject
{
    /** The game tick on which this piece was last moved. */
    public short lastMoved;

    /** The current x location of this piece. */
    public short x;

    /** The current y location of this piece. */
    public short y;

    /** This piece's orientation. */
    public short orientation;

    /** Returns this pieces current board bounds. The upper left of the
     * bound should always match the piece's location. <em>Note:</em> the
     * returned rectangle <em>must not</em> be modified. */
    public Rectangle getBounds ()
    {
        return _bounds;
    }

    /** Contains the pieces current bounds. As the piece is updated, it
     * should maintain its current board bounds in this object. */
    protected transient Rectangle _bounds = new Rectangle();
}
