//
// $Id$

package com.threerings.bugs.client;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;
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
     * Returns true if this sprite can be clicked and selected, false if
     * not.
     */
    public boolean isSelectable ()
    {
        return false;
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
        // remove ourselves from the sprite manager and go away
        ((SpriteManager)_mgr).removeSprite(this);
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

    // TEMP: our current piece
    protected Piece _piece;

    protected int _pieceId;
    protected int _currentTick;
    protected boolean _selected;
}
