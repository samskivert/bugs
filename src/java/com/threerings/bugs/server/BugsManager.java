//
// $Id$

package com.threerings.bugs.server;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.Interval;
import com.threerings.util.DirectionUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.server.PresentsServer;

import com.threerings.parlor.game.GameManager;

import com.threerings.toybox.server.ToyBoxServer;

import com.threerings.bugs.data.Ant;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsMarshaller;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Leaf;
import com.threerings.bugs.data.ModifyBoardEvent;
import com.threerings.bugs.data.Piece;

import static com.threerings.bugs.Log.log;

/**
 * Handles the server-side of a Bugs game.
 */
public class BugsManager extends GameManager
    implements BugsProvider
{
    // documentation inherited from interface BugsProvider
    public void movePiece (ClientObject caller, int pieceId, int x, int y)
    {
        Piece piece = (Piece)_bugsobj.pieces.get(pieceId);

        // make sure the piece exists and wasn't moved too recently
        if (piece == null || piece.lastMoved >= _bugsobj.tick) {
            log.info("not moving " + piece + "/" + _bugsobj.tick);
            return;
        }

        // validate that the move is legal (proper length, can traverse
        // all tiles along the way, no pieces intervene, etc.)
        if (!piece.canMoveTo(_bugsobj.board, x, y)) {
            log.warning("Piece requested illegal move [piece=" + piece +
                        ", x=" + x + ", y=" + y + "].");
            return;
        }

        // TODO: ensure that intervening pieces do not block this move

        // update the piece's location
        piece.position(x, y, DirectionUtil.getDirection(piece.x, piece.y, x, y),
                       _bugsobj.tick);

        // interact with any pieces occupying our target space
        Rectangle pb = piece.getBounds();
        for (Iterator iter = _bugsobj.pieces.entries(); iter.hasNext(); ) {
            Piece p = (Piece)iter.next();
            if (p != piece && p.getBounds().intersects(pb)) {
                log.info("Matched " + pb + " against " + p + ".");
                if (piece.maybeConsume(p)) {
                    _bugsobj.removeFromPieces(p.getKey());
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
        for (int ii = 0; ii < 4; ii++) {
            Ant ant = new Ant();
            ant.pieceId = _nextPieceId++;
            ant.position(ii+3, 8+(ii%2), Piece.NORTH, (short)-1);
            pieces.add(ant);

            Leaf leaf = new Leaf();
            leaf.pieceId = _nextPieceId++;
            leaf.position(ii+3, 7, Piece.NORTH, (short)-1);
            pieces.add(leaf);
        }
        return new DSet(pieces.iterator());
    }

    /** Triggers our board tick once every N seconds. */
    protected Interval _ticker = _ticker = new Interval(PresentsServer.omgr) {
        public void expired () {
            int nextTick = (_bugsobj.tick + 1) % Short.MAX_VALUE;
            _bugsobj.setTick((short)nextTick);
        }
    };

    protected BugsObject _bugsobj;

    /** Used to assign unique identifiers to pieces. */
    protected int _nextPieceId = 0;
}
