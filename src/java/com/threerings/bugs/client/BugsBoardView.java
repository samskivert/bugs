//
// $Id$

package com.threerings.bugs.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.VirtualMediaPanel;

import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.bugs.data.BugsBoard;

/**
 * Displays the main bugs game board.
 */
public class BugsBoardView extends VirtualMediaPanel
{
    public BugsBoardView (ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
    }

    /**
     * Provides us with a reference to the board we will be rendering.
     */
    public void setBoard (BugsBoard board)
    {
        _board = board;
        invalidate();
    }

    // documentation inherited
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);

        Rectangle r = new Rectangle(0, 0, SQUARE, SQUARE);
        for (int yy = 0, hh = _board.getHeight(); yy < hh; yy++) {
            r.x = 0;
            for (int xx = 0, ww = _board.getWidth(); xx < ww; xx++) {
                if (dirtyRect.intersects(r)) {
                    gfx.setColor(getColor(_board.getTile(xx, yy)));
                    gfx.fill(r);
                }
                r.x += SQUARE;
            }
            r.y += SQUARE;
        }
    }

    protected Color getColor (int tile)
    {
        switch (tile) {
        case BugsBoard.DIRT: return Color.orange.darker();
        case BugsBoard.MOSS: return Color.green.darker();
        case BugsBoard.TALL_GRASS: return Color.green;
        case BugsBoard.WATER: return Color.blue;
        default: return Color.black;
        }
    }

    protected BugsBoard _board;

    protected static final int SQUARE = 32;
}
