//
// $Id$

package com.threerings.bugs.client;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LinePath;

import com.threerings.bugs.data.Piece;

import static com.threerings.bugs.Log.log;
import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Handles the rendering of a particular piece on the board.
 */
public class PieceSprite extends Sprite
{
    public PieceSprite ()
    {
        super(SQUARE-4, SQUARE-4);
    }

    /** Returns the id of the piece associated with this sprite. */
    public int getPieceId ()
    {
        return _pieceId;
    }

    /** Indicates to this piece that it is selected by the user. Triggers
     * a special "selected" rendering mode. */
    public void setSelected (boolean selected)
    {
        if (_selected != selected) {
            _selected = selected;
            invalidate();
        }
    }

    /**
     * Called when we are first created and immediately before we are
     * added to the display.
     */
    public void init (Piece piece, int boardTick)
    {
        _pieceId = piece.pieceId;

        // position ourselves properly
        setLocation(SQUARE * piece.x + 2,
                    SQUARE * piece.y + 2);

        // TEMP: note our current piece
        _piece = piece;

        // start out with the proper tick settings
        tick(boardTick);
    }

    /**
     * Called when we receive an event indicating that our piece was
     * updated in some way.
     */
    public void updated (Piece piece)
    {
        // move ourselves to our new location
        move(new LinePath(_bounds.x, _bounds.y,
                          piece.x * SQUARE + 2, piece.y * SQUARE + 2, 250L));
        _piece = piece;
    }

    /**
     * Called when our piece is removed from the board state.
     */
    public void removed ()
    {
    }

    /**
     * Called when the board ticks advances.
     */
    public void tick (int boardTick)
    {
        boolean movable = (_currentTick > _piece.lastMoved);
        _currentTick = boardTick;
        if ((_currentTick > _piece.lastMoved) != movable) {
            invalidate();
        }
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.white);
        gfx.fill(_bounds);

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

        if (_currentTick == _piece.lastMoved) {
            gfx.setColor(Color.blue);
            gfx.drawRect(_bounds.x, _bounds.y,
                         _bounds.width-1, _bounds.height-1);
        } else if (_selected) {
            gfx.setColor(Color.green);
            gfx.drawRect(_bounds.x, _bounds.y,
                         _bounds.width-1, _bounds.height-1);
        }
    }

    // TEMP: our current piece
    protected Piece _piece;

    protected int _pieceId;
    protected int _currentTick;
    protected boolean _selected;
}
