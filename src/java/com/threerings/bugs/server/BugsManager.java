//
// $Id$

package com.threerings.bugs.server;

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

import com.threerings.bugs.data.*;
import com.threerings.bugs.data.pieces.*;
import com.threerings.bugs.data.goals.*;

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

        // save the old position and update the piece with the new one
        int ox = piece.x, oy = piece.y, oorient = piece.orientation;
        piece.position(
            x, y, DirectionUtil.getDirection(piece.x, piece.y, x, y));

        // ensure that intervening pieces do not block this move; also
        // track any piece that we end up overlapping
        Piece lapper = null;
        for (Iterator iter = _bugsobj.pieces.entries(); iter.hasNext(); ) {
            Piece p = (Piece)iter.next();
            if (p != piece && p.intersects(piece)) {
                if (p.preventsOverlap(piece)) {
                    piece.position(ox, oy, oorient);
                    return false;
                } else if (lapper != null) {
                    log.warning("Multiple overlapping pieces [mover=" + piece +
                                ", lap1=" + lapper + ", lap2=" + p + "].");
                } else {
                    lapper = p;
                }
            }
        }

        // interact with any pieces occupying our target space
        if (lapper != null) {
            // perhaps we consume this piece
            if (piece.maybeConsume(lapper)) {
                _bugsobj.removeFromPieces(lapper.getKey());

            // or perhaps we enter it (ie. ant into anthill)
            } else if (piece.maybeEnter(lapper)) {
                // update the piece we entered as we likely modified it in
                // doing so
                _bugsobj.updatePieces(lapper);
                // TODO: generate a special event indicating that the
                // piece entered so that we can animate it
                _bugsobj.removeFromPieces(piece.getKey());
                // short-circuit the remaining move processing
                return true;
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
    public void setPath (ClientObject caller, BugPath path)
    {
        Piece piece = (Piece)_bugsobj.pieces.get(path.pieceId);
        if (piece == null) {
            log.info("No such piece " + path.pieceId + ".");
            return;
        }

        // register the path in our table
        _paths.put(path.pieceId, path);

        // if the piece did not have a path prior, update it
        if (!piece.hasPath) {
            piece.hasPath = true;
            _bugsobj.updatePieces(piece);
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
        _bugsobj.setGoals(configureGoals());

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

        // move all of our bugs along any path they have configured
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

        // then give any creature a chance to react to the state of the
        // board now that everyone has moved
        Piece[] pieces = _bugsobj.getPieceArray();
        for (int ii = 0; ii < pieces.length; ii++) {
            Piece piece = pieces[ii];
            // skip pieces that were eaten
            if (!_bugsobj.pieces.containsKey(piece.pieceId)) {
                continue;
            }
            if (pieces[ii].react(_bugsobj, pieces)) {
                _bugsobj.updatePieces(pieces[ii]);
            }
        }

        // obtain a new pieces array containing the pieces left over after
        // all the moving, eating, and whatnot have taken place
        pieces = _bugsobj.getPieceArray();

        // check whether all of our goals have been met or botched
        boolean goalsRemain = false;
        for (Iterator giter = _bugsobj.goals.entries(); giter.hasNext(); ) {
            Goal goal = (Goal)giter.next();
            if (!goal.isMet(_bugsobj.board, pieces) &&
                !goal.isBotched(_bugsobj.board, pieces)) {
                // if we have at least one unmet goal we can stop checking
                goalsRemain = true;
                break;
            }
        }

        // next check to see whether any of our bugs have energy remaining
        boolean haveEnergy = true;
        // TODO

        // the game ends when none of our bugs have energy or we've
        // accomplished or botched all of our goals
        if (!haveEnergy || !goalsRemain) {
            endGame();
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
        if (movePiece(piece, nx, ny) && path.reachedGoal()) {
            piece.hasPath = false;
            return true;
        }
        return false;
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // cancel the board tick
        _ticker.cancel();

        log.info("Game over!");
        for (Iterator giter = _bugsobj.goals.entries(); giter.hasNext(); ) {
            Goal goal = (Goal)giter.next();
            log.info("Goal " + goal.getDescription() + ": " + goal.getState());
        }
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
            ant.position(ii+4, 8+(ii%2), Piece.NORTH);
            pieces.add(ant);
        }

        Bee bee = new Bee();
        bee.pieceId = _nextPieceId++;
        bee.position(7, 8, Piece.NORTH);
        pieces.add(bee);

        for (int ii = 0; ii < 2; ii++) {
            Leaf leaf = new Leaf();
            leaf.pieceId = _nextPieceId++;
            leaf.position(ii+3, 7, Piece.NORTH);
            pieces.add(leaf);
        }

        Frog frog = new Frog();
        frog.pieceId = _nextPieceId++;
        frog.position(0, 6, Piece.EAST);
        pieces.add(frog);

        Tree tree = new Tree();
        tree.pieceId = _nextPieceId++;
        tree.position(6, 1, Piece.NORTH);
        pieces.add(tree);

        AntHill hill = new AntHill();
        hill.pieceId = _nextPieceId++;
        hill.position(0, 0, Piece.NORTH);
        pieces.add(hill);

        return new DSet(pieces.iterator());
    }

    /** Configures our goals for this game. */
    protected DSet configureGoals ()
    {
        ArrayList<Goal> goals = new ArrayList<Goal>();

        Piece[] pieces = _bugsobj.getPieceArray();

        AntHillGoal ahgoal = new AntHillGoal();
        ahgoal.configure (_bugsobj.board, pieces);
        goals.add(ahgoal);

        return new DSet(goals.iterator());
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
