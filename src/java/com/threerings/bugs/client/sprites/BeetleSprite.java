//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.media.util.LinePath;

import com.threerings.bugs.data.pieces.Piece;

import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays a beetle piece.
 */
public class BeetleSprite extends PieceSprite
{
    @Override // documentation inherited
    public boolean isSelectable ()
    {
        return true;
    }

    @Override // documentation inherited
    public void init (Piece piece)
    {
        super.init(piece);

        // position ourselves properly
        setLocation(SQUARE * piece.x[0],
                    SQUARE * piece.y[0]);
    }

    @Override // documentation inherited
    public void updated (Piece piece)
    {
        // note our new piece
        _piece = piece;

        // move ourselves to our new location
        move(new LinePath(_bounds.x, _bounds.y,
                          piece.x[0] * SQUARE + 2,
                          piece.y[0] * SQUARE + 2, 250L));
    }

    @Override // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.white);
        gfx.fill(_bounds);

        int dx = SQUARE/2, dy = SQUARE/2;
        switch (_piece.orientation) {
        case Piece.NORTH: dy = 2; break;
        case Piece.SOUTH: dy = SQUARE-4; break;
        case Piece.WEST: dx = 2; break;
        case Piece.EAST: dx = SQUARE-4; break;
        }

        gfx.setColor(Color.black);
        gfx.drawLine(_bounds.x + SQUARE/2, _bounds.y + SQUARE/2,
                     _bounds.x + dx, _bounds.y + dy);

        if (_piece.hasPath) {
            gfx.setColor(Color.blue);
            gfx.drawRect(_bounds.x, _bounds.y,
                         _bounds.width-1, _bounds.height-1);
        } else if (_selected) {
            gfx.setColor(Color.green);
            gfx.drawRect(_bounds.x, _bounds.y,
                         _bounds.width-1, _bounds.height-1);
        }

        paintEnergy(gfx);
    }

    @Override // documentation inherited
    public int getRenderOrder ()
    {
        return 5;
    }
}
