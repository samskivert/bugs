//
// $Id$

package com.threerings.bugs.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.HashMap;
import java.util.Iterator;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.media.VirtualMediaPanel;
import com.threerings.media.sprite.Sprite;

import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Piece;

import static com.threerings.bugs.Log.log;
import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays the main bugs game board.
 */
public class BugsBoardView extends VirtualMediaPanel
    implements MouseListener, MouseMotionListener
{
    public BugsBoardView (ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Sets up the board view with all the necessary bits. This is called
     * by the controller when we enter an already started game or the game
     * in which we're involved gets started.
     */
    public void startGame (BugsObject bugsobj)
    {
        _bugsobj = bugsobj;
        _board = bugsobj.board;
        dirtyScreenRect(new Rectangle(0, 0, getWidth(), getHeight()));

        // create sprites for all of the pieces
        for (Iterator iter = bugsobj.pieces.entries(); iter.hasNext(); ) {
            // this will trigger the creation, initialization and whatnot
            getPieceSprite((Piece)iter.next());
        }

        // add the listener that will move and update the piece sprites
        bugsobj.addListener(_piecer);
    }

    /**
     * Called by the controller when our game has ended.
     */
    public void endGame ()
    {
        // clear out our piece sprites
        Iterator<PieceSprite> iter = _pieces.values().iterator();
        while (iter.hasNext()) {
            removeSprite(iter.next());
            iter.remove();
        }

        // remove our piece moving listener
        _bugsobj.removeListener(_piecer);
    }

    // documentation inherited from interface MouseListener
    public void mouseClicked (MouseEvent e)
    {
        // nothing doing, we handle this ourselves
    }

    // documentation inherited from interface MouseListener
    public void mousePressed (MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // check for a selectable piece under the mouse
            PieceSprite sprite = null;
            Sprite s = _spritemgr.getHighestHitSprite(e.getX(), e.getY());
            if (s instanceof PieceSprite) {
                sprite = (PieceSprite)s;
                if (!sprite.isSelectable()) {
                    sprite = null;
                }
            }

            if (sprite != null) {
                selectSprite(sprite);

            } else if (_selectedSprite != null) {
                // request to move the selected sprite in that direction
                MoveData data = new MoveData();
                data.pieceId = _selectedSprite.getPieceId();
                data.x = e.getX() / SQUARE;
                data.y = e.getY() / SQUARE;
                BugsController.postAction(
                    this, BugsController.MOVE_PIECE, data);

                // and clear the selection to debounce double clicking, etc.
                clearSpriteSelection();
            }

        } else if (e.getButton() == MouseEvent.BUTTON2) {
            clearSpriteSelection();
        }
    }

    // documentation inherited from interface MouseListener
    public void mouseReleased (MouseEvent e)
    {
    }

    // documentation inherited from interface MouseListener
    public void mouseEntered (MouseEvent e)
    {
        // nada
    }

    // documentation inherited from interface MouseListener
    public void mouseExited (MouseEvent e)
    {
        // nada
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseMoved (MouseEvent e)
    {
        // highlight the square under the mouse
        invalidateTile(_mouse.x, _mouse.y);
        _mouse.x = e.getX() / SQUARE;
        _mouse.y = e.getY() / SQUARE;
        invalidateTile(_mouse.x, _mouse.y);
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseDragged (MouseEvent e)
    {
        mouseMoved(e);
    }

    // documentation inherited
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);

        // wait until we have some sort of board
        if (_board == null) {
            return;
        }

        // start with a black background
        gfx.setColor(Color.black);
        gfx.fill(dirtyRect);

        Rectangle r = new Rectangle(0, 0, SQUARE, SQUARE);
        for (int yy = 0, hh = _board.getHeight(); yy < hh; yy++) {
            r.x = 0;
            for (int xx = 0, ww = _board.getWidth(); xx < ww; xx++) {
                if (dirtyRect.intersects(r)) {
                    Color color = getColor(_board.getTile(xx, yy));
                    if (xx == _mouse.x && yy == _mouse.y) {
                        color = color.brighter();
                    }
                    gfx.setColor(color);
                    gfx.fill(r);
                    gfx.setColor(Color.black);
                    gfx.draw(r);
                }
                r.x += SQUARE;
            }
            r.y += SQUARE;
        }
    }

    /** Invalidates a tile, causing it to be redrawn on the next tick. */
    protected void invalidateTile (int xx, int yy)
    {
        _remgr.invalidateRegion(xx * SQUARE, yy * SQUARE, SQUARE, SQUARE);
    }

    protected Color getColor (int tile)
    {
        Color color = null;
        switch (tile) {
        case BugsBoard.DIRT: color = Color.orange.darker(); break;
        case BugsBoard.MOSS: color = Color.green.darker(); break;
        case BugsBoard.TALL_GRASS: color = Color.green; break;
        case BugsBoard.WATER: color = Color.blue; break;
        case BugsBoard.LEAF_BRIDGE: color = Color.lightGray; break;
        default: color = Color.black; break;
        }
        return color;
    }

    /**
     * Returns (creating if necessary) the piece sprite associated with
     * the supplied piece. A newly created sprite will automatically be
     * initialized with the supplied piece and added to the board view.
     */
    protected PieceSprite getPieceSprite (Piece piece)
    {
        PieceSprite sprite = _pieces.get(piece.pieceId);
        if (sprite == null) {
            sprite = piece.createSprite();
            sprite.init(piece, _bugsobj.tick);
            _pieces.put((int)piece.pieceId, sprite);
            addSprite(sprite);
        }
        return sprite;
    }

    protected void selectSprite (PieceSprite sprite)
    {
        clearSpriteSelection();
        _selectedSprite = sprite;
        _selectedSprite.setSelected(true);
    }

    protected void clearSpriteSelection ()
    {
        if (_selectedSprite != null) {
            _selectedSprite.setSelected(false);
            _selectedSprite = null;
        }
    }

    /** Listens for updates to the pieces and instructs their associated
     * piece sprites to move accordingly. */
    protected class Piecer implements SetListener, AttributeChangeListener
    {
        public void entryAdded (EntryAddedEvent event) {
            getPieceSprite((Piece)event.getEntry());
        }

        public void entryUpdated (EntryUpdatedEvent event) {
            Piece piece = (Piece)event.getEntry();
            getPieceSprite(piece).updated(piece);
        }

        public void entryRemoved (EntryRemovedEvent event) {
            PieceSprite sprite = _pieces.remove((Integer)event.getKey());
            if (sprite != null) {
                sprite.removed();
            } else {
                log.warning("No sprite for removed piece " +
                            "[id=" + event.getKey() + "].");
            }
        }

        public void attributeChanged (AttributeChangedEvent event) {
            if (event.getName().equals(BugsObject.TICK)) {
                // propagate the board tick to the sprites
                for (PieceSprite sprite : _pieces.values()) {
                    sprite.tick(_bugsobj.tick);
                }
            }
        }
    };

    protected BugsObject _bugsobj;
    protected BugsBoard _board;
    protected Piecer _piecer = new Piecer();

    protected PieceSprite _selectedSprite;

    /** The current tile coordinates of the mouse. */
    protected Point _mouse = new Point(-1, -1);

    protected HashMap<Integer,PieceSprite> _pieces =
        new HashMap<Integer,PieceSprite>();
}
