//
// $Id$

package com.threerings.bugs.data.goals;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.presents.dobj.DSet;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.pieces.Piece;

/**
 * Represents one of the various goals a player might have in a particular
 * game.
 */
public abstract class Goal extends SimpleStreamableObject
    implements DSet.Entry
{
    /**
     * Returns true if this goal is appropriate given the supplied board
     * and piece configuration. This is used at the start of the game to
     * determine whether this goal will be included in the winning
     * conditions.
     */
    public boolean isReachable (BugsBoard board, Piece[] pieces)
    {
        return false;
    }

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
     * Returns a translatable string to report to the user when this goal
     * has been met.
     */
    public abstract String getMetMessage ();

    /**
     * Returns a translatable string to report to the user when this goal
     * has been botched.
     */
    public abstract String getBotchedMessage ();

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

    // documentation inherited from interface DSet.Entry
    public Comparable getKey ()
    {
        if (_key == null) {
            if (_goalId == 0) {
                _goalId = ++_nextGoalId;
            }
            _key = new Integer(_goalId);
        }
        return _key;
    }

    protected int _goalId;
    protected transient Integer _key;
    protected static int _nextGoalId;
}
