//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays an ant hill piece.
 */
public class AntHillSprite extends PieceSprite
{
    public AntHillSprite ()
    {
        super(2*SQUARE-4, 2*SQUARE-4);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(TREE_BROWN);
        gfx.fillOval(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
    }

    protected static final Color TREE_BROWN = new Color(0x79430E);
}
