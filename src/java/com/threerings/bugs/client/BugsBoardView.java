//
// $Id$

package com.threerings.bugs.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.ArrayList;
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

import com.threerings.bugs.data.BugPath;
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
        int tx = e.getX() / SQUARE, ty = e.getY() / SQUARE;

        // button 3 (right button) creates or extends a path
        if (e.getButton() == MouseEvent.BUTTON3) {
            // TODO: make sure this is a legal move
            log.info("right click! " + tx + "/" + ty);

            if (_pendingPath != null) {
                // add the specified node to the path (if it's legal)
                if (isLegalMove(tx, ty) && !_pendingPath.isTail(tx, ty)) {
                    _pendingPath = _pendingPath.append(tx, ty);
                    dirtyTile(tx, ty);
                    updatePossibleMoves(_selection, tx, ty);
                }

            } else if (_selection != null) {
                // start a new path
                _pendingPath = new BugPath(_selection.pieceId, tx, ty);
                dirtyPath(_pendingPath);
                updatePossibleMoves(_selection, tx, ty);
            }

        } else if (e.getButton() == MouseEvent.BUTTON1) {
            // check for a selectable piece under the mouse
            PieceSprite sprite = null;
            Sprite s = _spritemgr.getHighestHitSprite(e.getX(), e.getY());
            if (s instanceof PieceSprite) {
                sprite = (PieceSprite)s;
                if (!sprite.isSelectable()) {
                    sprite = null;
                }
            }

            if (_pendingPath != null) {
                // if their final click is a legal move...
                boolean tail = _pendingPath.isTail(tx, ty);
                if (isLegalMove(tx, ty) || tail) {
                    // ...add the final node to the path...
                    if (!tail) {
                        _pendingPath = _pendingPath.append(tx, ty);
                    }
                    // ...and ship it off for processing
                    BugsController.postAction(
                        this, BugsController.SET_PATH, _pendingPath);
                    clearSelection();

                } else if (sprite != null &&
                           sprite.getPieceId() == _selection.pieceId) {
                    // if they clicked in an illegal position, allow a
                    // click on the original selected piece to reset the
                    // path, other clicks we will ignore
                    selectPiece(_selection);
                }

            } else if (sprite != null) {
                Piece piece = (Piece)_bugsobj.pieces.get(sprite.getPieceId());
                if (piece != null) {
                    selectPiece(piece);
                } else {
                    log.warning("PieceSprite with no piece!? " +
                                "[sprite=" + sprite +
                                ", pieceId=" + sprite.getPieceId() + "].");
                }

            } else if (_selection != null) {
                if (isLegalMove(tx, ty)) {
                    // request to move the selected piece in that direction
                    MoveData data = new MoveData();
                    data.pieceId = _selection.pieceId;
                    data.x = tx;
                    data.y = ty;
                    BugsController.postAction(
                        this, BugsController.MOVE_PIECE, data);
                    // and clear the selection to debounce double clicking, etc.
                    clearSelection();
                }
            }
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

    // documentation inherited
    protected void paintInFront (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintInFront(gfx, dirtyRect);

        Rectangle r = null;
        if (_possibleMoves.size() > 0 || _pendingPath != null) {
            r = new Rectangle(0, 0, SQUARE, SQUARE);
        }

        // render our possible moves
        if (_possibleMoves.size() > 0) {
            Composite ocomp = gfx.getComposite();
            gfx.setComposite(POSS_MOVE_ALPHA);
            for (Point p : _possibleMoves) {
                r.x = p.x * SQUARE;
                r.y = p.y * SQUARE;
                if (dirtyRect.intersects(r)) {
                    gfx.setColor(Color.white);
                    gfx.fillRoundRect(
                        r.x+2, r.y+2, r.width-4, r.height-4, 8, 8);
                }
            }
            gfx.setComposite(ocomp);
        }

        // render any currently active path
        if (_pendingPath != null) {
            for (int ii = 0, ll = _pendingPath.getLength(); ii < ll; ii++) {
                int px = _pendingPath.getX(ii), py = _pendingPath.getY(ii);
                r.x = px * SQUARE;
                r.y = py * SQUARE;
                if (dirtyRect.intersects(r)) {
                    gfx.setColor(Color.pink);
                    gfx.fillOval(r.x+2, r.y+2, r.width-4, r.height-4);
                }
            }
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

    protected void selectPiece (Piece piece)
    {
        clearSelection();
        _selection = piece;
        getPieceSprite(_selection).setSelected(true);
        updatePossibleMoves(_selection, _selection.x, _selection.y);
    }

    protected void clearSelection ()
    {
        if (_pendingPath != null) {
            dirtyPath(_pendingPath);
            _pendingPath = null;
        }
        if (_selection != null) {
            getPieceSprite(_selection).setSelected(false);
            _selection = null;
            dirtyPossibleMoves();
            _possibleMoves.clear();
        }
    }

    protected boolean isLegalMove (int x, int y)
    {
        for (Point p : _possibleMoves) {
            if (p.x == x && p.y == y) {
                return true;
            }
        }
        return false;
    }

    protected void updatePossibleMoves (Piece piece, int x, int y)
    {
        dirtyPossibleMoves();
        _possibleMoves.clear();
        piece.enumerateLegalMoves(x, y, _possibleMoves);
        dirtyPossibleMoves();
    }

    protected void dirtyPath (BugPath path)
    {
        for (int ii = 0, ll = path.getLength(); ii < ll; ii++) {
            dirtyTile(path.getX(ii), path.getY(ii));
        }
    }

    protected void dirtyPossibleMoves ()
    {
        for (Point p : _possibleMoves) {
            dirtyTile(p.x, p.y);
        }
    }

    protected void dirtyTile (int tx, int ty)
    {
        _remgr.invalidateRegion(tx*SQUARE, ty*SQUARE, SQUARE, SQUARE);
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

    protected Piece _selection;
    protected BugPath _pendingPath;
    protected ArrayList<Point> _possibleMoves = new ArrayList<Point>();

    /** The current tile coordinates of the mouse. */
    protected Point _mouse = new Point(-1, -1);

    protected HashMap<Integer,PieceSprite> _pieces =
        new HashMap<Integer,PieceSprite>();

    /** The alpha level used to render possible moves. */
    protected static final Composite POSS_MOVE_ALPHA =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f);
}
