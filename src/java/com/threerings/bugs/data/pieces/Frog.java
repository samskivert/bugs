//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.util.DirectionUtil;

import com.threerings.bugs.client.sprites.FrogSprite;
import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.PointSet;
import com.threerings.bugs.data.Terrain;

import static com.threerings.bugs.Log.log;

/**
 * Handles the state and behavior of the frog piece.
 */
public class Frog extends BigPiece
{
    public Frog ()
    {
        super(2, 2);
    }

    @Override // documentation inherited
    public void init ()
    {
        super.init();
        computeSets();
    }

    @Override // documentation inherited
    public boolean react (BugsObject bugsobj, Piece[] pieces)
    {
        Piece front = checkSet(_front, pieces);
        Piece left = checkSet(_left, pieces);
        Piece right = checkSet(_right, pieces);

        // if there's a bug in edible range, eat 'em!
        if (front != null) {
            // TODO: send an event so we can animate the eating
            bugsobj.removeFromPieces(front.getKey());
            log.info("Ate " + front);
        }

        // if there's a bug visible in the periphery, rotate to face 'em
        if (right != null) {
            log.info("Saw right " + right);
            rotate(CW);
            return true;
        }
        if (left != null) {
            log.info("Saw left " + left);
            rotate(CCW);
            return true;
        }

        return false;
    }

    @Override // documentation inherited
    public void enumerateAttacks (PointSet set)
    {
        // lazily create our attention and attack sets
        if (_front == null) {
            computeSets();
        }
        set.addAll(_front);
    }

    @Override // documentation inherited
    public void enumerateAttention (PointSet set)
    {
        // lazily create our attention and attack sets
        if (_front == null) {
            computeSets();
        }
        set.addAll(_left);
        set.addAll(_right);
    }

    @Override // documentation inherited
    public PieceSprite createSprite ()
    {
        return new FrogSprite();
    }

    @Override // documentation inherited
    protected void pieceMoved ()
    {
        super.pieceMoved();
        // recompute our front, left and right sets
        computeSets();
    }

    @Override // documentation inherited
    protected boolean canTraverse (Terrain terrain)
    {
        if (terrain == Terrain.WATER) {
            return true;
        } else {
            return super.canTraverse(terrain);
        }
    }

    /**
     * Returns true if the frog eats this sort of pieces.
     */
    protected boolean edible (Piece piece)
    {
        return (piece instanceof Ant ||
                piece instanceof Bee);
    }

    /**
     * Returns the first piece in the supplied pieces array that
     * intersects any point in the supplied point set, or null if none do.
     */
    protected Piece checkSet (PointSet set, Piece[] pieces)
    {
        for (int pp = 0; pp < pieces.length; pp++) {
            if (!edible(pieces[pp])) {
                continue;
            }
            for (int ii = 0, ll = set.size(); ii < ll; ii++) {
                int tx = set.getX(ii), ty = set.getY(ii);
                if (pieces[pp].intersects(tx, ty)) {
                    return pieces[pp];
                }
            }
        }
        return null;
    }

    /**
     * Computes our attack and attention sets.
     */
    protected void computeSets ()
    {
        log.info("Computing sets " + x[0] + "/" + y[0] + "/" + orientation);
        if (_front == null) {
            _front = new PointSet();
            _left = new PointSet();
            _right = new PointSet();
        } else {
            _front.clear();
            _left.clear();
            _right.clear();
        }

        PointSet left, right;
        if (orientation == NORTH || orientation == SOUTH) {
            int y1, y2, y3;
            if (orientation == NORTH) {
                y1 = y[0];
                y2 = y[0]-1;
                y3 = y[0]-2;
                left = _left;
                right = _right;
            } else {
                y1 = y[0]+1;
                y2 = y[0]+2;
                y3 = y[0]+3;
                left = _right;
                right = _left;
            }

            _front.add(x[0], y2);
            _front.add(x[0]+1, y2);
            _front.add(x[0]-1, y3);
            _front.add(x[0], y3);
            _front.add(x[0]+1, y3);
            _front.add(x[0]+2, y3);

            left.add(x[0]-1, y1);
            left.add(x[0]-2, y1);
            left.add(x[0]-2, y2);

            right.add(x[0]+2, y1);
            right.add(x[0]+3, y1);
            right.add(x[0]+3, y2);

        } else {
            int x1, x2, x3;
            if (orientation == WEST) {
                x1 = x[0];
                x2 = x[0]-1;
                x3 = x[0]-2;
                left = _right;
                right = _left;
            } else {
                x1 = x[0]+1;
                x2 = x[0]+2;
                x3 = x[0]+3;
                left = _left;
                right = _right;
            }

            _front.add(x2, y[0]);
            _front.add(x2, y[0]+1);
            _front.add(x3, y[0]-1);
            _front.add(x3, y[0]);
            _front.add(x3, y[0]+1);
            _front.add(x3, y[0]+2);

            left.add(x1, y[0]-1);
            left.add(x1, y[0]-2);
            left.add(x2, y[0]-2);

            right.add(x1, y[0]+2);
            right.add(x1, y[0]+3);
            right.add(x2, y[0]+3);
        }
    }

    protected transient PointSet _left, _right, _front;
}
