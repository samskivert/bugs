//
// $Id$

package com.threerings.bugs.data.pieces;

import com.threerings.bugs.client.sprites.FoodSprite;
import com.threerings.bugs.client.sprites.PieceSprite;

/**
 * Handles the state and behavior of a basic food piece.
 */
public abstract class Food extends Piece
{
    /**
     * Returns the amount of energy the specified piece will get from
     * eating this food.
     */
    public abstract int getEnergy (Piece eater);

    @Override // documentation inherited
    public boolean preventsOverlap (Piece lapper)
    {
        return false;
    }

    @Override // documentation inherited
    public PieceSprite createSprite ()
    {
        return new FoodSprite();
    }
}
