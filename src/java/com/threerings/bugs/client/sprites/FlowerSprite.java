//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.bugs.data.pieces.Flower;

import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays a flower piece.
 */
public class FlowerSprite extends PieceSprite
{
    public FlowerSprite ()
    {
        super(2*SQUARE-4, 2*SQUARE-4);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.white);
        gfx.fillOval(_bounds.x, _bounds.y,
                     _bounds.width, _bounds.height);

        // our orientation indicates our color
        gfx.setColor(getColor(_piece.orientation));
        gfx.fillOval(_bounds.x+_bounds.width/3, _bounds.y+_bounds.height/3,
                     _bounds.width/3, _bounds.height/3);

        // if we've been pollinated, draw a circle around our color
        if (((Flower)_piece).pollinated) {
            gfx.setColor(Color.black);
            gfx.drawOval(_bounds.x+_bounds.width/3, _bounds.y+_bounds.height/3,
                         _bounds.width/3, _bounds.height/3);
        }
    }

    /**
     * Returns the color appropriate to the type of "pollen".
     */
    public static Color getColor (int pollen)
    {
        switch (pollen) {
        case NORTH: return Color.pink;
        case SOUTH: return Color.blue;
        case EAST: return Color.yellow;
        case WEST: return Color.red;
        default: return Color.black;
        }
    }
}
