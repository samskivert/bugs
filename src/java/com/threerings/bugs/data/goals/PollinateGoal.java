//
// $Id$

package com.threerings.bugs.data.goals;

import java.util.Iterator;

import com.samskivert.util.IntIntMap;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.pieces.Bee;
import com.threerings.bugs.data.pieces.Flower;
import com.threerings.bugs.data.pieces.Piece;

/**
 * A goal dictating that all flowers on the board should be pollinated.
 */
public class PollinateGoal extends Goal
{
    // documentation inherited
    public boolean isReachable (BugsBoard board, Piece[] pieces)
    {
        // count up our bees and the number of each type of flower
        int bees = 0;
        IntIntMap fcount = new IntIntMap();
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii] instanceof Bee) {
                bees++;
            } else if (pieces[ii] instanceof Flower) {
                fcount.increment(((Flower)pieces[ii]).orientation, 1);
            }
        }

        // make sure we don't have any lonely flowers
        for (Iterator iter = fcount.values(); iter.hasNext(); ) {
            if ((Integer)iter.next() < 2) {
                return false;
            }
        }

        // there must also be at least one bee and at least one kind of flower
        return (bees > 0) && (fcount.size() > 0);
    }

    // documentation inherited
    public void configure (BugsBoard board, Piece[] pieces)
    {
    }

    // documentation inherited
    public String getDescription ()
    {
        return "m.goal_pollinate";
    }

    // documentation inherited
    public String getState ()
    {
        return "m.gstate_pollinate";
    }

    // documentation inherited
    public boolean isMet (BugsBoard board, Piece[] pieces)
    {
        // we are met when all flowers on the board are pollinated
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii] instanceof Flower &&
                !((Flower)pieces[ii]).pollinated) {
                return false;
            }
        }
        return true;
    }

    // documentation inherited
    public boolean isBotched (BugsBoard board, Piece[] pieces)
    {
        // if there are no bees remaining, we are botched
        for (int ii = 0; ii < pieces.length; ii++) {
            if (pieces[ii] instanceof Bee) {
                return true;
            }
        }
        return false;
    }
}
