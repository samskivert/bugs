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

import com.threerings.parlor.game.server.GameManager;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.generate.ForestGenerator;
import com.threerings.bugs.data.pieces.Piece;
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
        EditorConfig bconfig = (EditorConfig)_gameconfig;
        if (bconfig.board != null && bconfig.board.length > 0) {
            try {
                Tuple tup = BoardUtil.loadBoard(bconfig.board);
                BugsBoard board = (BugsBoard)tup.left;
                Piece[] pvec = (Piece[])tup.right;
                Collections.addAll(pieces, pvec);
                return board;
            } catch (IOException ioe) {
                log.log(Level.WARNING, "Failed to unserialize board.", ioe);
            }
        }

        // if that doesn't work, generate a random board
        BugsBoard board = new BugsBoard(bconfig.size, bconfig.size);
        ForestGenerator gen = new ForestGenerator();
        gen.generate(bconfig.difficulty, board, pieces);
        return board;
    }

    /** A casted reference to our game object. */
    protected BugsObject _bugsobj;
}
