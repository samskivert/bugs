//
// $Id$

package com.threerings.bugs.data.generate;

import java.util.ArrayList;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.pieces.Piece;

/**
 * Provides a framework for various routines to be combined to generate
 * random Bugs! boards.
 */
public abstract class BoardGenerator
{
    /**
     * Instructs the generator to perform its generation, modifying the
     * supplied board and adding any created pieces to the supplied list.
     *
     * @param difficulty a number between 0 and 100 indicating the desired
     * difficulty of the generated board.
     */
    public abstract void generate (
        int difficulty, BugsBoard board, ArrayList<Piece> pieces);
}
