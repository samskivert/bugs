//
// $Id$

package com.threerings.bugs.server;

import java.util.ArrayList;

import com.samskivert.util.Interval;
import com.threerings.util.DirectionUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.server.PresentsServer;

import com.threerings.parlor.game.GameManager;

import com.threerings.bugs.data.Ant;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsMarshaller;
import com.threerings.bugs.data.BugsObject;
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
        log.info("moving? " + piece + "/" + _bugsobj.tick);
        if (piece != null && piece.lastMoved < _bugsobj.tick) {
            piece.orientation = (short)
                DirectionUtil.getDirection(piece.x, piece.y, x, y);
            piece.x = (short)x;
            piece.y = (short)y;
            piece.lastMoved = _bugsobj.tick;
            _bugsobj.updatePieces(piece);
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
            ant.position(ii+3, 9, Piece.NORTH, (short)-1);
            pieces.add(ant);
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
