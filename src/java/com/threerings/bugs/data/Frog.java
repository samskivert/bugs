//
// $Id$

package com.threerings.bugs.data;

import com.threerings.util.DirectionUtil;

import com.threerings.bugs.client.FrogSprite;
import com.threerings.bugs.client.PieceSprite;

import static com.threerings.bugs.Log.log;

/**
 * Handles the state and behavior of the frog piece.
 */
public class Frog extends Piece
{
    // documentation inherited
    public boolean position (int nx, int ny, int orient, short boardTick)
    {
        if (!super.position(nx, ny, orient, boardTick)) {
            return false;
        }

        // recompute our front, left and right sets
        computeSets();
        return true;
    }

    // documentation inherited
    public boolean react (BugsObject bugsobj, Piece[] pieces)
    {
        Piece front = null, left = null, right = null;
        for (int ii = 0; ii < pieces.length; ii++) {
            // TODO: we need to do rectangle intersection
            if (_front.contains(pieces[ii].x, pieces[ii].y)) {
                front = pieces[ii];
            } else if (_left.contains(pieces[ii].x, pieces[ii].y)) {
                left = pieces[ii];
            } else if (_right.contains(pieces[ii].x, pieces[ii].y)) {
                right = pieces[ii];
            }
        }

        // if there's a bug in edible range, eat 'em!
        if (front != null) {
            log.info("yum! " + front);
        }

        // if there's a bug visible in the periphery, rotate to face 'em
        if (left != null) {
            orientation = (short)DirectionUtil.rotateCCW(orientation, 4);
            return true;
        }
        if (right != null) {
            orientation = (short)DirectionUtil.rotateCW(orientation, 4);
            return true;
        }

        return false;
    }

    // documentation inherited
    public void enumerateAttacks (PointSet set)
    {
        // lazily create our attention and attack sets
        if (_front == null) {
            computeSets();
        }
        set.addAll(_front);
    }

    // documentation inherited
    public void enumerateAttention (PointSet set)
    {
        // lazily create our attention and attack sets
        if (_front == null) {
            computeSets();
        }
        set.addAll(_left);
        set.addAll(_right);
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new FrogSprite();
    }

    // documentation inherited
    protected boolean canTraverse (int terrain)
    {
        if (terrain == BugsBoard.WATER) {
            return true;
        } else {
            return super.canTraverse(terrain);
        }
    }

    // documentation inherited
    protected int getWidth (int orient)
    {
        return 2;
    }

    // documentation inherited
    protected int getHeight (int orient)
    {
        return 2;
    }

    /** Computes our attack and attention sets. */
    protected void computeSets ()
    {
        if (_front == null) {
            _front = new PointSet();
            _left = new PointSet();
            _right = new PointSet();
        }
        _front.clear();
        _left.clear();
        _right.clear();
        PointSet left, right;

        if (orientation == NORTH || orientation == SOUTH) {
            int y1, y2, y3;
            if (orientation == NORTH) {
                y1 = y;
                y2 = y-1;
                y3 = y-2;
                left = _left;
                right = _right;
            } else {
                y1 = y+1;
                y2 = y+2;
                y3 = y+3;
                left = _right;
                right = _left;
            }

            _front.add(x, y2);
            _front.add(x+1, y2);
            _front.add(x-1, y3);
            _front.add(x, y3);
            _front.add(x+1, y3);
            _front.add(x+2, y3);

            left.add(x-1, y1);
            left.add(x-2, y1);
            left.add(x-2, y2);

            right.add(x+2, y1);
            right.add(x+3, y1);
            right.add(x+3, y2);

        } else {
            int x1, x2, x3;
            if (orientation == WEST) {
                x1 = x;
                x2 = x-1;
                x3 = x-2;
                left = _right;
                right = _left;
            } else {
                x1 = x+1;
                x2 = x+2;
                x3 = x+3;
                left = _left;
                right = _right;
            }

            _front.add(x2, y);
            _front.add(x2, y+1);
            _front.add(x3, y-1);
            _front.add(x3, y);
            _front.add(x3, y+1);
            _front.add(x3, y+2);

            _left.add(x1, y-1);
            _left.add(x1, y-2);
            _left.add(x2, y-2);

            _right.add(x1, y+2);
            _right.add(x1, y+3);
            _right.add(x2, y+3);
        }
    }

    protected transient PointSet _left, _right, _front;
}