//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays a tree piece.
 */
public class TreeSprite extends PieceSprite
{
    public TreeSprite (int width, int height)
    {
        super(width*SQUARE-4, height*SQUARE-4);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(TREE_BROWN);
        gfx.fillOval(_bounds.x, _bounds.y, _bounds.width, _bounds.height);

        // TEMP: render our id so I can debug tree jiggling
        int lx = _bounds.x+(_bounds.width-_idLabel.getSize().width)/2;
        int ly = _bounds.y+(_bounds.height-_idLabel.getSize().height)/2;
        _idLabel.render(gfx, lx, ly);
    }

    protected static final Color TREE_BROWN = new Color(0x79430E);
}
