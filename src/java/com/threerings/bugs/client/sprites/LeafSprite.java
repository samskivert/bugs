//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Displays a leaf piece.
 */
public class LeafSprite extends PieceSprite
{
    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.green);
        gfx.fillRect(_bounds.x+4, _bounds.y+4,
                     _bounds.width-8, _bounds.height-8);
    }
}
