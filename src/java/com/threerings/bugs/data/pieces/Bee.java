//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.BeeSprite;
import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.PointSet;
import com.threerings.bugs.data.Terrain;

/**
 * Handles the state and behavior of the bee piece.
 */
public class Bee extends Piece
    implements PlayerPiece
{
    /** Contain the piece id of the flower that pollenated us or zero.
     * Used to ensure we don't pollenate our pollenator. */
    public int pollenator;

    /** If the bee is carrying pollen, this will contain pieceId of the
     * flower whose pollen it is carrying. */
    public int pollen;

    // documentation inherited
    public boolean isFlyer ()
    {
        return true;
    }

    // documentation inherited
    public Interaction maybeInteract (Piece other)
    {
        // bees interact with flowers
        if (other instanceof Flower) {
            Flower flower = (Flower)other;

            // we get a certain amount of energy by eating pollen
            energy = Math.min(maximumEnergy(), energy + RAW_POLLEN_ENERGY);

            // if we are carrying pollen, it is of the appropriate type,
            // it is not from this flower, and this flower is not already
            // pollinated, do the deed!
            if (pollenator != 0 && pollenator != flower.pieceId &&
                flower.orientation == pollen && !flower.pollinated) {
                // we also inherit this flower's pollenator id
                pollenator = flower.pieceId;
                flower.pollinated = true;
                return Interaction.INTERACTED;

            } else if (pollen != flower.orientation) {
                // otherwise, we become pollinated with the pollen from
                // this flower
                pollenator = flower.pieceId;
                pollen = flower.orientation;
                return Interaction.INTERACTED;
            }
        }

        return super.maybeInteract(other);
    }

    // documentation inherited
    public void enumerateLegalMoves (int tx, int ty, PointSet moves)
    {
        moves.add(tx, ty-2);
        moves.add(tx-1, ty-1);
        moves.add(tx, ty-1);
        moves.add(tx+1, ty-1);

        moves.add(tx+2, ty);
        moves.add(tx+1, ty);
        moves.add(tx-1, ty);
        moves.add(tx-2, ty);

        moves.add(tx-1, ty+1);
        moves.add(tx, ty+1);
        moves.add(tx+1, ty+1);
        moves.add(tx, ty+2);
    }

    // documentation inherited
    public boolean canMoveTo (BugsBoard board, int nx, int ny)
    {
        // we can move up to two squares in a turn
        if (Math.abs(x[0] - nx) + Math.abs(y[0] - ny) > 2) {
            return false;
        }

        // and make sure we can traverse our final location
        return canTraverse(board, nx, ny);
    }

    // documentation inherited
    public PieceSprite createSprite ()
    {
        return new BeeSprite();
    }

    // documentation inherited
    protected boolean canTraverse (Terrain terrain)
    {
        return true;
    }

    /** The quantity of energy gained by "feeding" at a flower. */
    protected static final int RAW_POLLEN_ENERGY = 50;
}
