//
// $Id$

package com.threerings.bugs.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

import com.samskivert.util.Tuple;

import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.game.GameManager;

import com.threerings.toybox.data.ToyBoxGameConfig;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Terrain;
import com.threerings.bugs.data.generate.*;
import com.threerings.bugs.data.pieces.*;
import com.threerings.bugs.util.BoardUtil;

import static com.threerings.bugs.Log.log;

/**
 * Handles the server side of the "editor" mode of the game.
 */
public class EditorManager extends GameManager
{
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
    }

    // documentation inherited
    protected void gameWillStart ()
    {
        super.gameWillStart();

        ToyBoxGameConfig tconfig = (ToyBoxGameConfig)_gameconfig;

        // set up the game object
        ArrayList<Piece> pieces = new ArrayList<Piece>();
        _bugsobj.setBoard(createBoard(pieces));
        _bugsobj.setPieces(new DSet(pieces.iterator()));
        _bugsobj.setGoals(new DSet());

        // initialize our pieces
        for (Iterator iter = _bugsobj.pieces.entries(); iter.hasNext(); ) {
            ((Piece)iter.next()).init();
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
        int size = (Integer)tconfig.params.get("size");
        BugsBoard board = new BugsBoard(size, size, Terrain.DIRT);

        PieceSprinkler sprinkler = new PieceSprinkler(new Ant(), 50);
        sprinkler.generate(board, pieces);
        sprinkler = new PieceSprinkler(new Beetle(), 10);
        sprinkler.generate(board, pieces);
        sprinkler = new PieceSprinkler(new Frog(), 10);
        sprinkler.generate(board, pieces);
        sprinkler = new PieceSprinkler(new Bee(), 25);
        sprinkler.generate(board, pieces);
        sprinkler = new PieceSprinkler(new Flower(), 75);
        sprinkler.generate(board, pieces);
        sprinkler = new PieceSprinkler(new AntHill(), 10);
        sprinkler.generate(board, pieces);
        sprinkler = new PieceSprinkler(new SodaDrop(), 25);
        sprinkler.generate(board, pieces);
        sprinkler = new PieceSprinkler(new Leaf(), 50);
        sprinkler.generate(board, pieces);

//         TestGenerator testgen = new TestGenerator();
//         testgen.generate(board, pieces);

        return board;
    }

    /** A casted reference to our game object. */
    protected BugsObject _bugsobj;
}
