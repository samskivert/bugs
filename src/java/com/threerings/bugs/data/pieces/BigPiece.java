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
    /**
     * Returns the bounds of this big piece. <em>Do not</em> modify the
     * returned rectangle.
     */
    public Rectangle getBounds ()
    {
        return _bounds;
    }

    @Override // documentation inherited
    public boolean intersects (int tx, int ty)
    {
        return _bounds.contains(tx, ty);
    }

    @Override // documentation inherited
    public boolean intersects (Piece other)
    {
        if (other instanceof BigPiece) {
            return _bounds.intersects(((BigPiece)other).getBounds());
        } else {
            for (int ii = 0; ii < other.x.length; ii++) {
                if (intersects(other.x[ii], other.y[ii])) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Require that our derived classes tell us how big they are. */
    protected BigPiece (int width, int height)
    {
        _bounds = new Rectangle(0, 0, width, height);
    }

    // documentation inherited
    protected void pieceMoved ()
    {
        super.pieceMoved();
        _bounds.setLocation(x[0], y[0]);
    }

    protected transient Rectangle _bounds;
}
