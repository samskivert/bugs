//
// $Id$

package com.threerings.bugs.editor;

import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.game.GameManager;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.Terrain;

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
        _bugsobj.setBoard(new BugsBoard(10, 10, Terrain.DIRT));
        _bugsobj.setPieces(new DSet());
        _bugsobj.setGoals(new DSet());
    }

    /** A casted reference to our game object. */
    protected BugsObject _bugsobj;
}
