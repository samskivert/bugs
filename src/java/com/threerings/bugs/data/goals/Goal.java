//
// $Id$

package com.threerings.bugs.data.goals;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.pieces.Piece;

/**
 * Represents one of the various goals a player might have in a particular
 * game.
 */
public abstract class Goal extends SimpleStreamableObject
{
    /**
     * Provides this goal with an opportunity to automatically configure
     * itself based on board configuration and number and types of pieces.
     */
    public void configure (BugsBoard board, Piece[] pieces)
    {
    }

    /**
     * Returns a translatable string describing this goal.
     */
    public abstract String getDescription ();

    /**
     * Returns a translatable string describing whether we met or botched
     * this goal and to what extent.
     */
    public abstract String getState ();

    /**
     * Returns true if this goal is met given the current state of the
     * board and pieces.
     */
    public abstract boolean isMet (BugsBoard board, Piece[] pieces);

    /**
     * Returns true if this goal can no longer be met given the current
     * state of the board and pieces.
     */
    public boolean isBotched (BugsBoard board, Piece[] pieces)
    {
        return false;
    }
}
