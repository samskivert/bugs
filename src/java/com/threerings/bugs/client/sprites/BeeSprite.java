//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.bugs.data.pieces.Bee;
import com.threerings.bugs.data.pieces.Piece;

import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays a bee piece.
 */
public class BeeSprite extends PieceSprite
{
    // documentation inherited
    public boolean isSelectable ()
    {
        return true;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.yellow);
        gfx.fill(_bounds);

        // if we are pollinated, draw a little circle in our center
        Bee bee = (Bee)_piece;
        if (bee.pollenator != 0) {
            gfx.setColor(FlowerSprite.getColor(bee.pollen));
            gfx.fillOval(_bounds.x+3, _bounds.y+3,
                         _bounds.width-6, _bounds.height-6);
        }

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

    // documentation inherited
    public int getRenderOrder ()
    {
        return 5;
    }
}
