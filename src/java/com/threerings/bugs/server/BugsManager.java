//
// $Id$

package com.threerings.bugs.server;

import com.threerings.parlor.game.GameManager;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;

/**
 * Handles the server-side of a Bugs game.
 */
public class BugsManager extends GameManager
{
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
        _bugsobj.setBoard(createBoard());
    }

    /** Creates a new bugs board based on the provided configuration. */
    protected BugsBoard createBoard ()
    {
        BugsBoard board = new BugsBoard(10, 10, BugsBoard.DIRT);
        for (int xx = 0; xx < 10; xx++) {
            board.setTile(xx, 4, BugsBoard.WATER);
            board.setTile(xx, 5, BugsBoard.WATER);
        }
        return board;
    }

    protected BugsObject _bugsobj;
}
