//
// $Id$

package com.threerings.bugs.data.generate;

import java.util.ArrayList;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.goals.Goal;
import com.threerings.bugs.data.pieces.Piece;

/**
 * Provides a framework for generating the pieces and goals that make up a
 * scenario (generally done after an environment is generated).
 */
public abstract class ScenarioGenerator
{
    /**
     * Instructs the generator to perform its generation, modifying the
     * supplied board and adding any created pieces and goals to the
     * supplied lists.
     *
     * @param difficulty a number between 0 and 100 indicating the desired
     * difficulty of the generated scenario.
     */
    public abstract void generate (
        int difficulty, BugsBoard board, ArrayList<Piece> pieces,
        ArrayList<Goal> goals);
}
