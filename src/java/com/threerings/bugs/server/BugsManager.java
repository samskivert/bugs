//
// $Id$

package com.threerings.bugs.server;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import com.samskivert.util.Interval;
import com.threerings.util.DirectionUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.server.PresentsServer;

import com.threerings.crowd.chat.server.SpeakProvider;
import com.threerings.parlor.game.GameManager;

import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.server.ToyBoxServer;

import com.threerings.bugs.data.*;
import com.threerings.bugs.data.goals.*;
import com.threerings.bugs.data.pieces.*;
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
        _bugsobj.setBoard(createBoard());
        _bugsobj.setPieces(createStartingPieces());
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
        boolean haveEnergy = false;
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii].canTakeStep()) {
                haveEnergy = true;
                break;
            }
        }

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

        // make sure the piece has the energy to move that far
        int steps = Math.abs(piece.x-nx) + Math.abs(piece.y-ny);
        if (piece.energy < steps * piece.energyPerStep()) {
            log.info("Piece out of energy [piece=" + piece + "].");
            piece.hasPath = false;
            _bugsobj.updatePieces(piece);
            return true;
        }

        if (movePiece(piece, nx, ny) && path.reachedGoal()) {
            piece.hasPath = false;
            return true;
        }

        return false;
    }

    /**
     * Attempts to move the specified piece to the specified coordinates.
     * Various checks are made to ensure that it is a legal move.
     *
     * @return true if the piece was moved, false if it was not movable
     * for some reason.
     */
    protected boolean movePiece (Piece piece, int x, int y)
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

        // consume the energy needed to make this move (we checked that
        // this was possible before we even called movePiece)
        piece.consumeEnergy(Math.abs(ox-x) + Math.abs(oy-y));

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

            // or perhaps we just interact with it
            } else if (piece.maybeInteract(lapper)) {
                // update the piece we interacted with, we'll update
                // ourselves momentarily
                _bugsobj.updatePieces(lapper);
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

        // finally broadcast our updated piece
        _bugsobj.updatePieces(piece);
        return true;
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
        // first, try loading it from our game configuration
        ToyBoxGameConfig tconfig = (ToyBoxGameConfig)_gameconfig;
        byte[] bdata = (byte[])tconfig.params.get("board");
        if (bdata != null && bdata.length > 0) {
            try {
                return (BugsBoard)BoardUtil.loadBoard(bdata).left;
            } catch (IOException ioe) {
                log.log(Level.WARNING, "Failed to unserialize board.", ioe);
            }
        }

        // then fall back to the default board
        BugsBoard board = new BugsBoard(10, 10, Terrain.DIRT);
        for (int xx = 0; xx < 10; xx++) {
            board.setTile(xx, 4, Terrain.WATER);
            board.setTile(xx, 5, Terrain.WATER);
        }
        return board;
    }

    /** Creates the set of starting pieces based on the game config. */
    protected DSet createStartingPieces ()
    {
        // first try loading them from our game configuration
        ToyBoxGameConfig tconfig = (ToyBoxGameConfig)_gameconfig;
        byte[] bdata = (byte[])tconfig.params.get("board");
        if (bdata != null && bdata.length > 0) {
            try {
                return new DSet((Piece[])BoardUtil.loadBoard(bdata).right);
            } catch (IOException ioe) {
                log.log(Level.WARNING, "Failed to unserialize board.", ioe);
            }
        }

        // then fall back to the default board
        ArrayList<Piece> pieces = new ArrayList<Piece>();

        for (int ii = 0; ii < 2; ii++) {
            Ant ant = new Ant();
            ant.position(ii+4, 8+(ii%2), Piece.NORTH);
            pieces.add(ant);
        }

        Bee bee = new Bee();
        bee.position(7, 8, Piece.NORTH);
        pieces.add(bee);

        for (int ii = 0; ii < 2; ii++) {
            Leaf leaf = new Leaf();
            leaf.position(ii+3, 7, Piece.NORTH);
            pieces.add(leaf);
        }

        Frog frog = new Frog();
        frog.position(0, 6, Piece.EAST);
        pieces.add(frog);

        Tree tree = new Tree();
        tree.position(6, 1, Piece.NORTH);
        pieces.add(tree);

        SodaDrop food = new SodaDrop();
        food.position(4, 2, Piece.NORTH);
        pieces.add(food);
        food = new SodaDrop();
        food.position(3, 3, Piece.NORTH);
        pieces.add(food);

        AntHill hill = new AntHill();
        hill.position(0, 0, Piece.NORTH);
        pieces.add(hill);

        return new DSet(pieces.iterator());
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
