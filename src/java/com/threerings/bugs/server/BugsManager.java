//
// $Id$

package com.threerings.bugs.server;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.Interval;
import com.samskivert.util.Tuple;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.server.PresentsServer;

import com.threerings.crowd.chat.server.SpeakProvider;
import com.threerings.parlor.game.GameManager;

import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.server.ToyBoxServer;

import com.threerings.bugs.data.*;
import com.threerings.bugs.data.generate.ForestGenerator;
import com.threerings.bugs.data.goals.*;
import com.threerings.bugs.data.pieces.Piece;
import com.threerings.bugs.data.pieces.PlayerPiece;
import com.threerings.bugs.util.BoardUtil;

import static com.threerings.bugs.Log.log;

/**
 * Handles the server-side of a Bugs game.
 */
public class BugsManager extends GameManager
    implements BugsProvider
{
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
        ArrayList<Piece> pieces = new ArrayList<Piece>();
        _bugsobj.setBoard(createBoard(pieces));
        _bugsobj.setPieces(new DSet(pieces.iterator()));
        _bugsobj.setGoals(configureGoals());

        // initialize our pieces
        for (Iterator iter = _bugsobj.pieces.entries(); iter.hasNext(); ) {
            ((Piece)iter.next()).init();
        }

        // fire off messages for each of our goals
        for (Iterator iter = _bugsobj.goals.entries(); iter.hasNext(); ) {
            SpeakProvider.sendInfo(_bugsobj, BugsCodes.BUGS_MSGS,
                                   ((Goal)iter.next()).getDescription());
        }

        // queue up the board tick
        _ticker.schedule(2000L, true);
    }

    /**
     * Called when the board tick is incremented.
     */
    protected void tick (short tick)
    {
        log.fine("Ticking [tick=" + tick +
                 ", pcount=" + _bugsobj.pieces.size() + "].");

        Piece[] pieces = _bugsobj.getPieceArray();

        // first, check whether all of our goals have been met or botched
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
        boolean haveEnergy = false;
        for (int ii = 0; ii < pieces.length; ii++) {
            if ((pieces[ii] instanceof PlayerPiece) &&
                pieces[ii].canTakeStep()) {
                haveEnergy = true;
                break;
            }
        }

        // the game ends when none of our bugs have energy or we've
        // accomplished or botched all of our goals
        if (!haveEnergy || !goalsRemain) {
            // if the bugs ran out of energy, let the player know
            if (goalsRemain && !haveEnergy) {
                SpeakProvider.sendInfo(
                    _bugsobj, BugsCodes.BUGS_MSGS, "m.out_of_energy");
            }
            endGame();
            return;
        }

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

        // recreate our pieces array as pieces may have moved
        pieces = _bugsobj.getPieceArray();

        // then give any creature a chance to react to the state of the
        // board now that everyone has moved
        for (int ii = 0; ii < pieces.length; ii++) {
            Piece piece = pieces[ii];
            // skip pieces that were eaten
            if (!_bugsobj.pieces.containsKey(piece.pieceId)) {
                continue;
            }
            if (piece.react(_bugsobj, pieces)) {
                _bugsobj.updatePieces(piece);
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

        // make sure the piece has the energy to move that far
        int steps = Math.abs(piece.x[0]-nx) + Math.abs(piece.y[0]-ny);
        if (piece.energy < steps * piece.energyPerStep()) {
            log.info("Piece out of energy [piece=" + piece + "].");
            piece.hasPath = false;
            _bugsobj.updatePieces(piece);
            return true;
        }

        // try moving the piece
        Piece npiece = movePiece(piece, nx, ny);
        if (npiece == null) {
            return false;
        }

        // check to see if we've reached the end of our path
        boolean reachedGoal = path.reachedGoal();
        if (reachedGoal) {
            npiece.hasPath = false;
        }

        // finally broadcast our updated piece
        _bugsobj.updatePieces(npiece);
        return reachedGoal;
    }

    /**
     * Attempts to move the specified piece to the specified coordinates.
     * Various checks are made to ensure that it is a legal move.
     *
     * @return a new piece at the new location if the piece was moved,
     * null if it was not movable for some reason.
     */
    protected Piece movePiece (Piece piece, int x, int y)
    {
        // validate that the move is legal (proper length, can traverse
        // all tiles along the way, no pieces intervene, etc.)
        if (!piece.canMoveTo(_bugsobj.board, x, y)) {
            log.warning("Piece requested illegal move [piece=" + piece +
                        ", x=" + x + ", y=" + y + "].");
            return null;
        }

        // calculate the distance we're moving
        int steps = Math.abs(piece.x[0]-x) + Math.abs(piece.y[0]-y);

        // clone the piece so that we can investigate the hypothetical
        piece = (Piece)piece.clone();
        piece.position(x, y);

        // ensure that intervening pieces do not block this move; also
        // track any piece that we end up overlapping
        ArrayList<Piece> lappers = _bugsobj.getOverlappers(piece);
        Piece lapper = null;
        if (lappers != null) {
            for (Piece p : lappers) {
                if (p.preventsOverlap(piece)) {
                    return null;
                } else if (lapper != null) {
                    log.warning("Multiple overlapping pieces [mover=" + piece +
                                ", lap1=" + lapper + ", lap2=" + p + "].");
                } else {
                    lapper = p;
                }
            }
        }

        // consume the energy needed to make this move (we checked that
        // this was possible before we even called movePiece)
        piece.consumeEnergy(steps);

        // interact with any piece occupying our target space
        if (lapper != null) {
            switch (piece.maybeInteract(lapper)) {
            case CONSUMED:
                _bugsobj.removeFromPieces(lapper.getKey());
                break;

            case ENTERED:
                // update the piece we entered as we likely modified it in
                // doing so
                _bugsobj.updatePieces(lapper);
                // TODO: generate a special event indicating that the
                // piece entered so that we can animate it
                _bugsobj.removeFromPieces(piece.getKey());
                // short-circuit the remaining move processing
                return piece;

            case INTERACTED:
                // update the piece we interacted with, we'll update
                // ourselves momentarily
                _bugsobj.updatePieces(lapper);
                break;

            case NOTHING:
                break;
            }
        }

        // allow the piece to modify the board
        Terrain terrain = piece.modifyBoard(_bugsobj.board, x, y);
        if (terrain != Terrain.NONE) {
            // update the board immediately and then dispatch the event
            _bugsobj.board.setTile(x, y, terrain);
            ToyBoxServer.omgr.postEvent(
                new ModifyBoardEvent(_bugsobj.getOid(), x, y, terrain));
        }

        return piece;
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // cancel the board tick
        _ticker.cancel();

        // report the state of our goals
        Piece[] pieces = _bugsobj.getPieceArray();
        for (Iterator giter = _bugsobj.goals.entries(); giter.hasNext(); ) {
            Goal goal = (Goal)giter.next();
            String msg = "";
            if (goal.isMet(_bugsobj.board, pieces)) {
                msg = goal.getMetMessage();
            } else {
                msg = goal.getBotchedMessage();
            }
            SpeakProvider.sendInfo(_bugsobj, BugsCodes.BUGS_MSGS, msg);
        }
    }

    /**
     * Creates the bugs board based on the game config, filling in the
     * supplied pieces array with the starting pieces.
     */
    protected BugsBoard createBoard (ArrayList<Piece> pieces)
    {
        // first, try loading it from our game configuration
        ToyBoxGameConfig tconfig = (ToyBoxGameConfig)_gameconfig;
        byte[] bdata = (byte[])tconfig.params.get("board");
        if (bdata != null && bdata.length > 0) {
            try {
                Tuple tup = BoardUtil.loadBoard(bdata);
                BugsBoard board = (BugsBoard)tup.left;
                Piece[] pvec = (Piece[])tup.right;
                Collections.addAll(pieces, pvec);
                return board;
            } catch (IOException ioe) {
                log.log(Level.WARNING, "Failed to unserialize board.", ioe);
            }
        }

        // if that doesn't work, generate a random board
        BugsBoard board = new BugsBoard(25, 25);
        ForestGenerator gen = new ForestGenerator();
        gen.generate(50, board, pieces);
        return board;
    }

    /** Configures our goals for this game. */
    protected DSet configureGoals ()
    {
        ArrayList<Goal> goals = new ArrayList<Goal>();
        Piece[] pieces = _bugsobj.getPieceArray();

        // check our various goals to see which should be added
        Goal goal = new AntHillGoal();
        if (goal.isReachable(_bugsobj.board, pieces)) {
            goals.add(goal);
        }
        goal = new PollinateGoal();
        if (goal.isReachable(_bugsobj.board, pieces)) {
            goals.add(goal);
        }

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
}
