//
// $Id$

package com.threerings.bugs.server;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.util.Interval;
import com.threerings.util.DirectionUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.server.PresentsServer;

import com.threerings.parlor.game.GameManager;

import com.threerings.toybox.server.ToyBoxServer;

import com.threerings.bugs.data.Ant;
import com.threerings.bugs.data.Bee;
import com.threerings.bugs.data.BugPath;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsMarshaller;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Frog;
import com.threerings.bugs.data.Leaf;
import com.threerings.bugs.data.ModifyBoardEvent;
import com.threerings.bugs.data.Piece;
import com.threerings.bugs.data.Tree;

import static com.threerings.bugs.Log.log;

/**
 * Handles the server-side of a Bugs game.
 */
public class BugsManager extends GameManager
    implements BugsProvider
{
    /**
     * Attempts to move the specified piece to the specified coordinates.
     * Various checks are made to ensure that it is a legal move.
     *
     * @return true if the piece was moved, false if it was not movable
     * for some reason.
     */
    public boolean movePiece (Piece piece, int x, int y)
    {
        // validate that the move is legal (proper length, can traverse
        // all tiles along the way, no pieces intervene, etc.)
        if (!piece.canMoveTo(_bugsobj.board, x, y)) {
            log.warning("Piece requested illegal move [piece=" + piece +
                        ", x=" + x + ", y=" + y + "].");
            return false;
        }

        // TODO: ensure that intervening pieces do not block this move

        // update the piece's location
        piece.position(x, y, DirectionUtil.getDirection(piece.x, piece.y, x, y),
                       _bugsobj.tick);

        // interact with any pieces occupying our target space
        for (Iterator iter = _bugsobj.pieces.entries(); iter.hasNext(); ) {
            Piece p = (Piece)iter.next();
            if (p != piece && p.intersects(piece)) {
                if (piece.maybeConsume(p)) {
                    _bugsobj.removeFromPieces(p.getKey());
                    // as we break here, we won't get a CME for removing
                    // an entry from a DSet over which we're iterating
                    break;
                }
            }
        }

        // allow the piece to modify the board
        int terrain = piece.modifyBoard(_bugsobj.board, x, y);
        if (terrain != BugsBoard.NONE) {
            // update the board immediately and then dispatch the event
            _bugsobj.board.setTile(x, y, terrain);
            ToyBoxServer.omgr.postEvent(
                new ModifyBoardEvent(_bugsobj.getOid(), x, y, terrain));
        }

        // finally broadcast our updated piece
        _bugsobj.updatePieces(piece);
        return true;
    }

    // documentation inherited from interface BugsProvider
    public void movePiece (ClientObject caller, int pieceId, int x, int y)
    {
        Piece piece = (Piece)_bugsobj.pieces.get(pieceId);

        // make sure the piece exists and wasn't moved too recently
        if (piece == null || piece.lastMoved >= _bugsobj.tick) {
            log.info("Not moving " + piece + "/" + _bugsobj.tick + ".");
            return;
        }

        movePiece(piece, x, y);
    }

    // documentation inherited from interface BugsProvider
    public void setPath (ClientObject caller, BugPath path)
    {
        Piece piece = (Piece)_bugsobj.pieces.get(path.pieceId);
        if (piece == null) {
            log.info("No such piece " + path.pieceId + ".");
            return;
        }

        // register the path in our table
        _paths.put(path.pieceId, path);

        // if this piece hasn't moved yet this turn, start them along
        // their path
        if (piece.lastMoved < _bugsobj.tick) {
            tickPath(piece, path);
        }
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        String name = event.getName();
        if (name.equals(BugsObject.TICK)) {
            tick(_bugsobj.tick);

        } else {
            super.attributeChanged(event);
        }
    }

    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return BugsObject.class;
    }

    // documentation inherited
    protected void didStartup ()
    {
        super.didStartup();
        _bugsobj = (BugsObject)_gameobj;
        _bugsobj.setService(
            (BugsMarshaller)PresentsServer.invmgr.registerDispatcher(
                new BugsDispatcher(this), false));
    }

    // documentation inherited
    protected void didShutdown ()
    {
        super.didShutdown();
        PresentsServer.invmgr.clearDispatcher(_bugsobj.service);
    }

    // documentation inherited
    protected void gameWillStart ()
    {
        super.gameWillStart();

        // set up the game object
        _bugsobj.setBoard(createBoard());
        _bugsobj.setPieces(createStartingPieces());

        // queue up the board tick
        _ticker.schedule(5000L, true);
    }

    /**
     * Called when the board tick is incremented.
     */
    protected void tick (short tick)
    {
        log.fine("Ticking [tick=" + tick +
                 ", pcount=" + _bugsobj.pieces.size() + "].");

        // first give any creature a chance to react to the state of the
        // board at the end of the previous tick
        Piece[] pieces = _bugsobj.getPieceArray();
        for (int ii = 0; ii < pieces.length; ii++) {
            Piece piece = pieces[ii];
            // skip pieces that were eaten or have already moved
            if (!_bugsobj.pieces.containsKey(piece.pieceId) ||
                piece.lastMoved >= tick) {
                continue;
            }
            if (pieces[ii].react(tick, _bugsobj, pieces)) {
                _bugsobj.updatePieces(pieces[ii]);
            }
        }

        // then move all of our bugs along any path they have configured
        Iterator<BugPath> iter = _paths.values().iterator();
        while (iter.hasNext()) {
            BugPath path = iter.next();
            Piece piece = (Piece)_bugsobj.pieces.get(path.pieceId);
            if (piece == null || tickPath(piece, path)) {
                log.fine("Finished " + path + ".");
                // if the piece has gone away, or if we complete our path,
                // remove it
                iter.remove();
            }
        }
    }

    /**
     * Moves the supplied piece further along its configured path.
     *
     * @return true if the bug reached the final goal on the path, false
     * if not.
     */
    protected boolean tickPath (Piece piece, BugPath path)
    {
        log.fine("Moving " + path + ".");
        int nx = path.getNextX(), ny = path.getNextY();
        if (movePiece(piece, nx, ny)) {
            return path.reachedGoal();
        }
        return false;
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // cancel the board tick
        _ticker.cancel();
    }

    /** Creates the bugs board based on the game config. */
    protected BugsBoard createBoard ()
    {
        BugsBoard board = new BugsBoard(10, 10, BugsBoard.DIRT);
        for (int xx = 0; xx < 10; xx++) {
            board.setTile(xx, 4, BugsBoard.WATER);
            board.setTile(xx, 5, BugsBoard.WATER);
        }
        return board;
    }

    /** Creates the set of starting pieces based on the game config. */
    protected DSet createStartingPieces ()
    {
        ArrayList<Piece> pieces = new ArrayList<Piece>();
        for (int ii = 0; ii < 2; ii++) {
            Ant ant = new Ant();
            ant.pieceId = _nextPieceId++;
            ant.position(ii+4, 8+(ii%2), Piece.NORTH, (short)-1);
            pieces.add(ant);
        }
        Bee bee = new Bee();
        bee.pieceId = _nextPieceId++;
        bee.position(7, 8, Piece.NORTH, (short)-1);
        pieces.add(bee);
        for (int ii = 0; ii < 2; ii++) {
            Leaf leaf = new Leaf();
            leaf.pieceId = _nextPieceId++;
            leaf.position(ii+3, 7, Piece.NORTH, (short)-1);
            pieces.add(leaf);
        }
        Frog frog = new Frog();
        frog.pieceId = _nextPieceId++;
        frog.position(0, 6, Piece.EAST, (short)-1);
        pieces.add(frog);
        Tree tree = new Tree();
        tree.pieceId = _nextPieceId++;
        tree.position(6, 1, Piece.NORTH, (short)-1);
        pieces.add(tree);
        return new DSet(pieces.iterator());
    }

    /** Triggers our board tick once every N seconds. */
    protected Interval _ticker = _ticker = new Interval(PresentsServer.omgr) {
        public void expired () {
            int nextTick = (_bugsobj.tick + 1) % Short.MAX_VALUE;
            _bugsobj.setTick((short)nextTick);
        }
    };

    /** A casted reference to our game object. */
    protected BugsObject _bugsobj;

    /** Maps pieceId to path for pieces that have a path configured. */
    protected HashMap<Integer,BugPath> _paths = new HashMap<Integer,BugPath>();

    /** Used to assign unique identifiers to pieces. */
    protected int _nextPieceId = 0;
}
