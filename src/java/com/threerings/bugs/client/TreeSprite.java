//
// $Id$

package com.threerings.bugs.client;

import java.awt.Color;
import java.awt.Graphics2D;

import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays a tree piece.
 */
public class TreeSprite extends PieceSprite
{
    public TreeSprite ()
    {
        super(3*SQUARE-4, 3*SQUARE-4);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(TREE_BROWN);
        gfx.fillOval(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
    }

    protected static final Color TREE_BROWN = new Color(0x79430E);
}
