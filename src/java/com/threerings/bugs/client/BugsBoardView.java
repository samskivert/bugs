//
// $Id$

package com.threerings.bugs.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

import com.threerings.bugs.client.sprites.PieceSprite;

import com.threerings.bugs.data.BugPath;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.PointSet;
import com.threerings.bugs.data.pieces.Piece;

import static com.threerings.bugs.Log.log;
import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays the main bugs game board.
 */
public class BugsBoardView extends VirtualMediaPanel
    implements MouseListener, MouseMotionListener, KeyListener
{
    public BugsBoardView (ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
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
            // make sure this is a legal move
            if (!_moveSet.contains(tx, ty)) {
                // nada

            } else if (_pendingPath != null) {
                // potentiall extend our existing path
                if (!_pendingPath.isTail(tx, ty)) {
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
                if (_moveSet.contains(tx, ty) || tail) {
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
                if (_moveSet.contains(tx, ty)) {
                    // create a one move path and send that off
                    BugsController.postAction(
                        this, BugsController.SET_PATH,
                        new BugPath(_selection.pieceId, tx, ty));
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
        int mx = e.getX() / SQUARE, my = e.getY() / SQUARE;
        if (mx != _mouse.x || my != _mouse.y) {
            invalidateTile(_mouse.x, _mouse.y);
            _mouse.x = mx;
            _mouse.y = my;
            invalidateTile(_mouse.x, _mouse.y);
        }
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseDragged (MouseEvent e)
    {
        mouseMoved(e);
    }

    // documentation inherited from interface KeyListener
    public void keyTyped (KeyEvent e)
    {
        // nothing doing
    }

    // documentation inherited from interface KeyListener
    public void keyPressed (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            showAttackSet();
        }
    }

    // documentation inherited from interface KeyListener
    public void keyReleased (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            clearAttackSet();
        }
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();
        _ctx.getKeyDispatcher().addGlobalKeyListener(this);
    }

    // documentation inherited
    public void removeNotify ()
    {
        super.removeNotify();
        _ctx.getKeyDispatcher().removeGlobalKeyListener(this);
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

        // render our attack or attention sets
        if (_attackSet != null) {
            renderSet(gfx, dirtyRect, _attackSet, Color.blue);
        }
        if (_attentionSet != null) {
            renderSet(gfx, dirtyRect, _attentionSet, Color.green);
        }

        // render our possible moves
        if (_moveSet.size() > 0) {
            renderSet(gfx, dirtyRect, _moveSet, Color.white);
        }

        // render any currently active path
        if (_pendingPath != null) {
            Rectangle r = new Rectangle(0, 0, SQUARE, SQUARE);
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

    /** Highlights a set of tiles in the specified color. */
    protected void renderSet (Graphics2D gfx, Rectangle dirtyRect,
                              PointSet set, Color color)
    {
        Rectangle r = new Rectangle(0, 0, SQUARE, SQUARE);
        Composite ocomp = gfx.getComposite();
        gfx.setComposite(POSS_MOVE_ALPHA);
        for (int ii = 0, ll = set.size(); ii < ll; ii++) {
            r.x = set.getX(ii) * SQUARE;
            r.y = set.getY(ii) * SQUARE;
            if (dirtyRect.intersects(r)) {
                gfx.setColor(color);
                gfx.fillRoundRect(
                    r.x+2, r.y+2, r.width-4, r.height-4, 8, 8);
            }
        }
        gfx.setComposite(ocomp);
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
            sprite.init(piece);
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
            dirtySet(_moveSet);
            _moveSet.clear();
        }
    }

    protected void updatePossibleMoves (Piece piece, int x, int y)
    {
        dirtySet(_moveSet);
        _moveSet.clear();
        piece.enumerateLegalMoves(x, y, _moveSet);
        dirtySet(_moveSet);
    }

    protected void showAttackSet ()
    {
        _attackSet = new PointSet();
        _attentionSet = new PointSet();
        for (Iterator iter = _bugsobj.pieces.entries(); iter.hasNext(); ) {
            Piece piece = (Piece)iter.next();
            piece.enumerateAttacks(_attackSet);
            piece.enumerateAttention(_attentionSet);
        }
        _remgr.invalidateRegion(_vbounds);
    }

    protected void clearAttackSet ()
    {
        _attackSet = null;
        _attentionSet = null;
        _remgr.invalidateRegion(_vbounds);
    }

    protected void dirtyPath (BugPath path)
    {
        for (int ii = 0, ll = path.getLength(); ii < ll; ii++) {
            dirtyTile(path.getX(ii), path.getY(ii));
        }
    }

    protected void dirtySet (PointSet set)
    {
        for (int ii = 0, ll = set.size(); ii < ll; ii++) {
            dirtyTile(set.getX(ii), set.getY(ii));
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
//             if (event.getName().equals(BugsObject.TICK)) {
//                 // propagate the board tick to the sprites
//                 for (PieceSprite sprite : _pieces.values()) {
//                     sprite.tick(_bugsobj.tick);
//                 }
//             }
        }
    };

    protected ToyBoxContext _ctx;
    protected BugsObject _bugsobj;
    protected BugsBoard _board;
    protected Piecer _piecer = new Piecer();

    protected Piece _selection;
    protected BugPath _pendingPath;
    protected PointSet _moveSet = new PointSet();
    protected PointSet _attackSet, _attentionSet;

    /** The current tile coordinates of the mouse. */
    protected Point _mouse = new Point(-1, -1);

    protected HashMap<Integer,PieceSprite> _pieces =
        new HashMap<Integer,PieceSprite>();

    /** The alpha level used to render possible moves. */
    protected static final Composite POSS_MOVE_ALPHA =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f);
}
