//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.data.PointSet;

/**
 * Handles the state and behavior of the wasp piece.
 */
public class Wasp extends Piece
{
    @Override // documentation inherited
    public void init ()
    {
        super.init();
        computeSets();
    }

//     @Override // documentation inherited
//     public boolean react (BugsObject bugsobj, Piece[] pieces)
//     {
//         Piece front = checkSet(_front, pieces);
//         Piece left = checkSet(_left, pieces);
//         Piece right = checkSet(_right, pieces);

//         // if there's a bug in edible range, eat 'em!
//         if (front != null) {
//             // TODO: send an event so we can animate the eating
//             bugsobj.removeFromPieces(front.getKey());
//             log.info("Ate " + front);
//         }

//         // if there's a bug visible in the periphery, rotate to face 'em
//         if (right != null) {
//             log.info("Saw right " + right);
//             rotate(CW);
//             return true;
//         }
//         if (left != null) {
//             log.info("Saw left " + left);
//             rotate(CCW);
//             return true;
//         }

//         return false;
//     }

    @Override // documentation inherited
    public void enumerateAttacks (PointSet set)
    {
        // lazily create our attention and attack sets
        if (_attack == null) {
            computeSets();
        }
        set.addAll(_attack);
    }

    @Override // documentation inherited
    public void enumerateAttention (PointSet set)
    {
        // lazily create our attention and attack sets
        if (_attack == null) {
            computeSets();
        }
        set.addAll(_attend);
    }

//     @Override // documentation inherited
//     public PieceSprite createSprite ()
//     {
//         return new FrogSprite();
//     }

    @Override // documentation inherited
    protected void pieceMoved ()
    {
        super.pieceMoved();
        // recompute our front, left and right sets
        computeSets();
    }

//     @Override // documentation inherited
//     protected boolean canTraverse (Terrain terrain)
//     {
//         if (terrain == Terrain.WATER) {
//             return true;
//         } else {
//             return super.canTraverse(terrain);
//         }
//     }

//     /**
//      * Returns true if the frog eats this sort of pieces.
//      */
//     protected boolean edible (Piece piece)
//     {
//         return (piece instanceof Ant ||
//                 piece instanceof Bee);
//     }

//     /**
//      * Returns the first piece in the supplied pieces array that
//      * intersects any point in the supplied point set, or null if none do.
//      */
//     protected Piece checkSet (PointSet set, Piece[] pieces)
//     {
//         for (int pp = 0; pp < pieces.length; pp++) {
//             if (!edible(pieces[pp])) {
//                 continue;
//             }
//             for (int ii = 0, ll = set.size(); ii < ll; ii++) {
//                 int tx = set.getX(ii), ty = set.getY(ii);
//                 if (pieces[pp].intersects(tx, ty)) {
//                     return pieces[pp];
//                 }
//             }
//         }
//         return null;
//     }

    /**
     * Computes our attack and attention sets.
     */
    protected void computeSets ()
    {
        if (_attack == null) {
            _attack = new PointSet();
            _attend = new PointSet();
        } else {
            _attack.clear();
            _attend.clear();
        }
        computeSets(SETS, SET_SIZE, 1, _attack, _attend);
    }

    protected transient PointSet _attend, _attack;

    /** A north facing wasp's attention and attack sets. */
    protected static final int[] SETS = new int[] {
        0, 0, 0, 0, 1, 0, 0, 0, 0,
        0, 0, 0, 1, 2, 1, 0, 0, 0,
        0, 0, 1, 2, 2, 2, 1, 0, 0,
        0, 1, 2, 2, 2, 2, 2, 1, 0,
        0, 2, 2, 2, 9, 2, 2, 2, 0,
        0, 0, 0, 2, 2, 2, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0,
    };

    /** The length of one dimension of our set grid. */
    protected static final int SET_SIZE = 9;
}
