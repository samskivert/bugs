//
// $Id$

package com.threerings.bugs.editor;

import java.io.IOException;
import java.util.logging.Level;

import com.samskivert.util.Tuple;

import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.game.GameManager;

import com.threerings.toybox.data.ToyBoxGameConfig;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Terrain;
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

        ToyBoxGameConfig tconfig = (ToyBoxGameConfig)_gameconfig;

        // set up the game object
        BugsBoard board = null;
        byte[] bdata = (byte[])tconfig.params.get("board");
        if (bdata != null && bdata.length > 0) {
            try {
                log.info("Loading board from " + bdata.length + " bytes.");
                Tuple tup = BoardUtil.loadBoard(bdata);
                board = (BugsBoard)tup.left;
                _bugsobj.setBoard(board);
                _bugsobj.setPieces(new DSet((Piece[])tup.right));
                _bugsobj.setGoals(new DSet());
            } catch (IOException ioe) {
                log.log(Level.WARNING, "Failed to unserialize board.", ioe);
            }
        }

        if (board == null) {
            int size = (Integer)tconfig.params.get("size");
            _bugsobj.setBoard(new BugsBoard(size, size, Terrain.DIRT));
            _bugsobj.setPieces(new DSet());
            _bugsobj.setGoals(new DSet());
        }
    }

    /** A casted reference to our game object. */
    protected BugsObject _bugsobj;
}
