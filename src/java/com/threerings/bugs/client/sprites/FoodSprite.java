//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.bugs.data.pieces.HoneyDrop;
import com.threerings.bugs.data.pieces.SodaDrop;

/**
 * Displays a food piece.
 */
public class FoodSprite extends PieceSprite
{
    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(getColor());
        gfx.fillOval(_bounds.x+4, _bounds.y+4,
                     _bounds.width-8, _bounds.height-8);
    }

    protected Color getColor ()
    {
        if (_piece instanceof SodaDrop) {
            return Color.gray;
        } else if (_piece instanceof HoneyDrop) {
            return Color.yellow;
        } else {
            return Color.white;
        }
    }
}
