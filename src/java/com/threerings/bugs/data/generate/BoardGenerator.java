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
     */
    public abstract void generate (BugsBoard board, ArrayList<Piece> pieces);
}
