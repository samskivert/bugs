//
// $Id$

package com.threerings.bugs.data.generate;

import java.util.ArrayList;

import com.threerings.util.RandomUtil;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.pieces.Piece;

import static com.threerings.bugs.Log.log;

/**
 * A board generator that sprinkes pieces of a particular type around the
 * board.
 */
public class PieceSprinkler
{
    /**
     * Creates a piece sprinkler with the specified piece which will be
     * cloned and sprinkled around the board.
     *
     * @param density a number between 0 and 1000 indicating the number of
     * pieces to place (per thousand tiles on the board).
     */
    public PieceSprinkler (Piece piece, int density)
    {
        _piece = piece;
        _density = density;
    }

    // documentation inherited
    public void generate (BugsBoard board, ArrayList<Piece> pieces)
    {
        // figure out how many pieces to place
        int bwidth = board.getWidth(), bheight = board.getHeight();
        int pcount = (int)Math.round(bwidth * bheight * _density / 1000f);

        log.info("Placing " + pcount + " copies of " + _piece + ".");

        for (int ii = 0; ii < pcount; ii++) {
            Piece npiece = (Piece)_piece.clone();

            // figure out where to put the piece
            int px = RandomUtil.getInt(bwidth, 0);
            int py = RandomUtil.getInt(bheight, 0);

//             // we need to "walk" to that location from a distance at least
//             // as far away as the piece is long to ensure that all of our
//             // segments are in proper position
//             int sx =

            npiece.position(px, py);
            pieces.add(npiece);
        }
    }

    protected Piece _piece;
    protected int _density;
}
