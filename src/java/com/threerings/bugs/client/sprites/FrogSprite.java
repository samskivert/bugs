//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.bugs.data.pieces.Piece;

import static com.threerings.bugs.Log.log;
import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays a frog piece.
 */
public class FrogSprite extends PieceSprite
{
    public FrogSprite ()
    {
        super(2*SQUARE-4, 2*SQUARE-4);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.white);
        gfx.fill(_bounds);

        int dx = SQUARE/2, dy = SQUARE/2;
        switch (_piece.orientation) {
        case Piece.NORTH: dy = 2; break;
        case Piece.SOUTH: dy = 2*SQUARE-4; break;
        case Piece.WEST: dx = 2; break;
        case Piece.EAST: dx = 2*SQUARE-4; break;
        }

        gfx.setColor(Color.black);
        gfx.drawLine(_bounds.x + SQUARE, _bounds.y + SQUARE,
                     _bounds.x + dx, _bounds.y + dy);
    }

    // documentation inherited
    public int getRenderOrder ()
    {
        return 5;
    }
}
