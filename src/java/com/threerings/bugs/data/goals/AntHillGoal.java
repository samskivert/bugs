//
// $Id$

package com.threerings.bugs.data.goals;

import com.samskivert.text.MessageUtil;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.pieces.Ant;
import com.threerings.bugs.data.pieces.AntHill;
import com.threerings.bugs.data.pieces.Piece;

/**
 * A goal dictating that as many ants as possible should be returned to
 * their anthill.
 */
public class AntHillGoal extends Goal
{
    /** The total number of ants at the start of the game. */
    public int totalAnts;

    // documentation inherited
    public boolean isReachable (BugsBoard board, Piece[] pieces)
    {
        // if there is an ant hill and at least one ant, we're good to go
        int ants = 0, hills = 0;
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii] instanceof Ant) {
                ants++;
            } else if (pieces[ii] instanceof AntHill) {
                hills++;
            }
        }
        return (ants > 0) && (hills > 0);
    }

    // documentation inherited
    public void configure (BugsBoard board, Piece[] pieces)
    {
        // count up all the ants in the game at the start
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii] instanceof Ant) {
                totalAnts++;
            }
        }
    }

    // documentation inherited
    public String getDescription ()
    {
        return "m.goal_anthill";
    }

    // documentation inherited
    public String getState ()
    {
        return MessageUtil.tcompose(
            "m.gstate_anthill",
            Integer.toString(_enteredAnts),
            Integer.toString(totalAnts - _enteredAnts));
    }

    // documentation inherited
    public boolean isMet (BugsBoard board, Piece[] pieces)
    {
        // we are met when there are no free ants remaining on the board
        _freeAnts = 0;
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii] instanceof Ant) {
                _freeAnts++;
            }
        }
        return (_freeAnts == 0);
    }

    // documentation inherited
    public boolean isBotched (BugsBoard board, Piece[] pieces)
    {
        // we are botched if fewer than half the ants made it into hills
        _enteredAnts = 0;
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii] instanceof AntHill) {
                _enteredAnts += ((AntHill)pieces[ii]).enteredAnts;
            }
        }
        return (_freeAnts == 0) && (_enteredAnts < totalAnts/2);
    }

    protected transient int _freeAnts;
    protected transient int _enteredAnts;
}
