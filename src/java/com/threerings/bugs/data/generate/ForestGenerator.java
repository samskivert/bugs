//
// $Id$

package com.threerings.bugs.data.generate;

import java.awt.Rectangle;
import java.util.ArrayList;

import com.samskivert.util.IntTuple;
import com.threerings.util.RandomUtil;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.Terrain;
import com.threerings.bugs.data.pieces.BigPiece;
import com.threerings.bugs.data.pieces.Piece;
import com.threerings.bugs.data.pieces.Sapling;
import com.threerings.bugs.data.pieces.Sequoia;
import com.threerings.bugs.data.pieces.Tree;
import com.threerings.bugs.util.PieceUtil;

import static com.threerings.bugs.Log.log;

/**
 * Generates boards in a forest setting.
 */
public class ForestGenerator extends BoardGenerator
{
    // documentation inherited
    public void generate (
        int difficulty, BugsBoard board, ArrayList<Piece> pieces)
    {
        int width = board.getWidth(), height = board.getHeight();

        // first fill the board with dirt
        board.fill(Terrain.DIRT);

        // pick a reasonable number for a board of this size (1/2 of the
        // average of the width and height)
        int count = (width+height)/4;

        // TODO: maybe more trees at higher difficulty

        // now scatter some trees around; first place them randomly
        BigPiece[] trees = new BigPiece[count];
        for (int ii = 0; ii < count; ii++) {
            int rando = RandomUtil.getInt(100);
            if (rando > 90) {
                trees[ii] = new Sequoia();
            } else if (rando > 65) {
                trees[ii] = new Tree();
            } else {
                trees[ii] = new Sapling();
            }
            BigPiece tree = trees[ii];
            tree.assignPieceId();
            int tx = RandomUtil.getInt(width-tree.getBounds().width, 0);
            int ty = RandomUtil.getInt(height-tree.getBounds().height, 0);
            tree.position(tx, ty);
        }

        // then move them around a bit to resolve overlaps and ensure
        // sufficient space between each tree (at least room for an ant)
        for (int cc = 0; cc < 10*count; cc++) {
            int adjusted = 0;
            for (int ii = 0; ii < count; ii++) {
                BigPiece tree = trees[ii];
                BigPiece[] neighbors = new BigPiece[Piece.DIRECTIONS.length];

                // see how close we are to our neighbors
                for (int dd = 0; dd < neighbors.length; dd++) {
                    int direction = Piece.DIRECTIONS[dd];
                    BigPiece neigh = getClosest(trees, tree, direction);
                    if (neigh == null || !tooClose(tree, neigh)) {
                        continue;
                    }

                    // if we're bigger than our neighbor, adjust them
                    // rather than us
                    BigPiece atree = tree;
                    if (tree.getBounds().width > neigh.getBounds().width) {
                        atree = neigh;
                        neigh = tree;
                    }

                    // try moving the piece away from its neighbor
                    int dist = (1 - getDistance(direction, atree, neigh));
                    int dx = dist * Piece.DX[direction];
                    int dy = dist * Piece.DY[direction];
//                     log.info(tree.pieceId + " is too close to " +
//                              neigh.pieceId + ". Adjusting [dx=" + dx +
//                              ", dy=" + dy + "].");
                    atree.position(atree.x[0] + dx, atree.y[0] + dy);
                    adjusted++;
                }
            }

            if (adjusted == 0) {
                break;
            }
        }

        // finally add all the trees that are still fully on the board
        Rectangle rect = new Rectangle(0, 0, width, height);
        for (int ii = 0; ii < count; ii++) {
            if (rect.contains(trees[ii].getBounds())) {
                pieces.add(trees[ii]);
            } else {
//                 log.info("A " + trees[ii].getClass().getName() +
//                          " ended up off the board.");
                trees[ii] = null;
            }
        }

        // TODO: add lizards; 100 difficulty = 100% of the trees, 0
        // difficulty = 0% of the trees

        // create a grid that we'll use to determine where not to grow grass
        int[] state = new int[width*height];

        // mark the location of the trees
        for (int ii = 0; ii < count; ii++) {
            if (trees[ii] == null) {
                continue;
            }
            Rectangle tb = trees[ii].getBounds();
            int top = Math.max(tb.y-1, 0), left = Math.max(tb.x-1, 0);
            int bottom = Math.min(tb.y + tb.height + 1, height) - 1;
            int right = Math.min(tb.x + tb.width + 1, width) - 1;
            for (int yy = top; yy <= bottom; yy++) {
                for (int xx = left; xx <= right; xx++) {
                    state[yy*width+xx] = 1;
                }
            }
        }

        // now pick some random spots and plant some grass there
        int spots = 0, targetSpots = 3; // TODO: vary based on difficulty
        for (int ii = 0; ii < 50 && spots < targetSpots; ii++) {
            int gx = RandomUtil.getInt(width), gy = RandomUtil.getInt(height);
            int gidx = gy*width+gx;
            if (state[gidx] == 0) {
                state[gidx] = 2;
                spots++;
            }
        }

        // if we planted any, grow it for a few generations
        if (spots > 0) {
            // TODO: vary based on board size and difficulty
            int generations = 5;
            for (int ii = 0; ii < generations; ii++) {
                for (int yy = 0; yy < height; yy++) {
                    for (int xx = 0; xx < width; xx++) {
                        int tidx = yy*width+xx;
                        if (state[tidx] > 0) {
                            continue;
                        }

                        // grow grass based on our neighbors
                        int iprob = 100;
                        switch (neighbors(state, width, height, tidx)) {
                        case 0: continue;
                        case 1: iprob = 49; break;
                        case 2: iprob = 49; break;
                        case 3: iprob = 49; break;
                        case 4: iprob = 0; break;
                        }

                        if (RandomUtil.getInt(100) > iprob) {
                            state[tidx] = 2;
                        }
                    }
                }
            }

            // make a final pass filling in single holes
            for (int yy = 0; yy < height; yy++) {
                for (int xx = 0; xx < width; xx++) {
                    int tidx = yy*width+xx;
                    if (state[tidx] > 0) {
                        continue;
                    }
                    if (neighbors(state, width, height, tidx) == 4) {
                        state[tidx] = 2;
                    }
                }
            }
        }

        // actually put the grass on the board based on our calculations
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                if (state[yy*width+xx] == 2) {
                    board.setTile(xx, yy, Terrain.TALL_GRASS);
                }
            }
        }
    }

    /**
     * Returns the piece closest to the specified piece in the specified
     * direction assuming that a piece "overlaps" if its bounds plus one
     * tile overlap with the specified piece's in that direction.
     */
    protected BigPiece getClosest (
        BigPiece[] pieces, BigPiece piece, int direction)
    {
        IntTuple range = new IntTuple(), crange = new IntTuple();
        getRange(piece, direction, range);
        // expand the range around the target piece by one to cause pieces
        // that don't overlay but have no space between them to "match"
        range.left -= 1;
        range.right += 1;

        BigPiece closest = null;
        int cdist = Integer.MAX_VALUE;
        for (int ii = 0; ii < pieces.length; ii++) {
            BigPiece cpiece = pieces[ii];
            if (cpiece == piece) {
                continue;
            }
            getRange(cpiece, direction, crange);
            if (!rangesOverlap(range, crange)) {
                continue;
            }

            int dist = Integer.MAX_VALUE, tdist;
            switch (direction) {
            case Piece.NORTH:
                dist = piece.getBounds().y - cpiece.getBounds().y;
                break;
            case Piece.SOUTH:
                dist = cpiece.getBounds().y - piece.getBounds().y;
                break;
            case Piece.EAST:
                dist = cpiece.getBounds().x - piece.getBounds().x;
                break;
            case Piece.WEST:
                dist = piece.getBounds().x - cpiece.getBounds().x;
                break;
            }

            if (dist >= 0 && dist < cdist) {
                closest = cpiece;
                cdist = dist;
            }
        }

        return closest;
    }

    /**
     * Returns true if the two pieces in question do not have a gap of at
     * least one tile between them.
     */
    protected boolean tooClose (BigPiece one, BigPiece two)
    {
        Rectangle rect = new Rectangle(one.getBounds());
        rect.grow(1, 1);
        return rect.intersects(two.getBounds());
    }

    /**
     * Returns the distance between the two pieces in the specified
     * direction.
     */
    protected int getDistance (int direction, BigPiece source, BigPiece target)
    {
        switch (direction) {
        case Piece.NORTH:
            return getDistance(Piece.SOUTH, target, source);
        case Piece.EAST:
            return (target.getBounds().x - (source.getBounds().x +
                                            source.getBounds().width));
        case Piece.SOUTH:
            return (target.getBounds().y - (source.getBounds().y +
                                            source.getBounds().height));
        case Piece.WEST:
            return getDistance(Piece.EAST, target, source);
        }
        return 0;
    }

    protected void getRange (BigPiece piece, int direction, IntTuple range)
    {
        Rectangle tb = piece.getBounds();
        switch (direction) {
        case Piece.NORTH:
        case Piece.SOUTH:
            range.left = tb.x;
            range.right = range.left + tb.width - 1;
            break;
        case Piece.WEST:
        case Piece.EAST:
            range.left = tb.y;
            range.right = range.left + tb.height - 1;
            break;
        }
    }

    protected boolean rangesOverlap (IntTuple range1, IntTuple range2)
    {
        return (range1.right >=  range2.left) && (range1.left <= range2.right);
    }

    protected final int neighbors (int[] state, int width, int height, int tidx)
    {
        int ncount = 0;
        int lidx = (tidx+state.length-1)%state.length;
        int ridx = (tidx+1)%state.length;
        int uidx = (tidx+state.length-width)%state.length;
        int didx = (tidx+width)%state.length;
        if (state[lidx] == 2) {
            ncount++;
        }
        if (state[ridx] == 2) {
            ncount++;
        }
        if (state[uidx] == 2) {
            ncount++;
        }
        if (state[didx] == 2) {
            ncount++;
        }
        return ncount;
    }
}
