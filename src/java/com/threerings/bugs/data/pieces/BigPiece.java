//
// $Id$

package com.threerings.bugs.data.pieces;

import java.awt.Rectangle;

/**
 * A base class for pieces that are big rectangles rather than a chain of
 * 1x1 segments. These pieces are not intended for player control, nor to
 * move around the board. Just to sit quietly and be large.
 */
public abstract class BigPiece extends Piece
{
    @Override // documentation inherited
    public boolean intersects (int tx, int ty)
    {
        _bounds.setLocation(x[0], y[0]);
        return _bounds.contains(tx, ty);
    }

    /** Require that our derived classes tell us how big they are. */
    protected BigPiece (int width, int height)
    {
        _bounds = new Rectangle(0, 0, width, height);
    }

    protected transient Rectangle _bounds;
}
