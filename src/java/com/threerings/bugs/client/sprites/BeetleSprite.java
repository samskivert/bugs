//
// $Id$

package com.threerings.bugs.client.sprites;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.media.util.LinePath;

import com.threerings.bugs.data.pieces.Piece;

import static com.threerings.bugs.Log.log;
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

        // compute our starting bounds and set our location based on that
        _bounds = computeBounds(piece);
        setLocation(_bounds.x, _bounds.y);
    }

    @Override // documentation inherited
    public void updated (Piece piece)
    {
        // note our new piece
        _piece = piece;

        // dirty our old bounds
        invalidate();

        log.info("New piece: " + piece);

        // compute our new bounds
        _bounds = computeBounds(piece);

        // "move" to the new location (this will redraw at our new loc)
        setLocation(_bounds.x, _bounds.y);
    }

    @Override // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.white);
        gfx.fill(_bounds);

        // draw a spot on our head
        gfx.setColor(Color.black);
        gfx.fillRect(SQUARE * _piece.x[0]+4, SQUARE * _piece.y[0]+4,
                     SQUARE-8, SQUARE-8);

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
