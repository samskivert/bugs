//
// $Id$

package com.threerings.bugs.data.pieces;

import java.awt.Rectangle;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.presents.dobj.DSet;

import com.threerings.bugs.client.sprites.PieceSprite;
import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.PointSet;
import com.threerings.bugs.data.Terrain;

import static com.threerings.bugs.Log.log;

/**
 * Contains the basic state and interface for a piece that lives on the
 * board.
 */
public abstract class Piece extends SimpleStreamableObject
    implements Cloneable, DSet.Entry, PieceCodes
{
    /** Used by {@link #maybeInteract}. */
    public enum Interaction { CONSUMED, ENTERED, INTERACTED, NOTHING };

    /** Uniquely identifies each piece in the game. */
    public int pieceId;

    /** The current x location of this piece's segments. */
    public short[] x;

    /** The current y location of this piece's segments. */
    public short[] y;

    /** This piece's orientation. */
    public short orientation;

    /** The energy level of this piece. */
    public int energy;

    /** True if this piece is currently following a path. */
    public boolean hasPath;

    /**
     * Returns true if the specified coordinates intersect this piece.
     */
    public boolean intersects (int tx, int ty)
    {
        for (int ii = 0; ii < x.length; ii++) {
            if (x[ii] == tx && y[ii] == ty) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if these two pieces intersect at their current
     * coordinates.
     */
    public boolean intersects (Piece other)
    {
        for (int ii = 0; ii < x.length; ii++) {
            if (other.intersects(x[ii], y[ii])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Allows the piece to do any necessary initialization before the game
     * starts.
     */
    public void init ()
    {
        // set up our starting energy (only if it hasn't been otherwise
        // configured in the editor)
        if (energy == 0) {
            energy = startingEnergy();
        }
    }

    /**
     * Returns the new orientation for this piece were it to travel from
     * its current coordinates to the specified coordinates.
     */
    public int getOrientation (int nx, int ny)
    {
        int hx = x[0], hy = y[0];

        // if it is purely a horizontal or vertical move, simply orient
        // in the direction of the move
        if (nx == hx) {
            return (ny > hy) ? SOUTH : NORTH;
        } else if (ny == hy) {
            return (nx > hx) ? EAST : WEST;
        }

        // otherwise try to behave naturally: moving forward first if
        // possible and turning sensibly to reach locations behind us
        switch (orientation) {
        case NORTH: return (ny < hy) ? ((nx > hx) ? EAST : WEST) : SOUTH;
        case SOUTH: return (ny > hy) ? ((nx > hx) ? EAST : WEST) : NORTH;
        case EAST:  return (nx > hx) ? ((ny > hy) ? SOUTH : NORTH) : WEST;
        case WEST:  return (nx < hx) ? ((ny > hy) ? SOUTH : NORTH) : EAST;
        // erm, this shouldn't happen
        default: return NORTH;
        }
    }

    /**
     * Updates this pieces position and orientation.
     *
     * @return true if the piece's position changed, false if not.
     */
    public boolean position (int nx, int ny, int orient)
    {
        // handle our very first position
        if (x == null) {
            orientation = (short)orient;
            createSegments(nx, ny);
            return true;
        }

        // avoid NOOP
        if ((nx == x[0]) && (ny == y[0]) && (orient == orientation)) {
            return false;
        }

        // we assume that we move like a worm, our head occupies the new
        // position and all other segments move ahead one to catch up
        for (int ii = x.length-1; ii > 0; ii--) {
            x[ii] = x[ii-1];
            y[ii] = y[ii-1];
        }
        x[0] = (short)nx;
        y[0] = (short)ny;
        orientation = (short)orient;
        return true;
    }

    /**
     * Instructs the piece to rotate clockwise if direction is {@link Piece#CW}
     * and counter-clockwise if it is {@link Piece#CCW}.
     */
    public void rotate (int direction)
    {
        int norient = (direction == CW) ?
            (orientation + 1 % 4) : (orientation + 3 % 4);
        position(x[0], y[0], norient);
    }

    /**
     * Returns true if this piece is a flying piece, false if it is a
     * walking piece.
     */
    public boolean isFlyer ()
    {
        return false;
    }

    /**
     * Returns the energy consumed per step taken by this piece.
     */
    public int energyPerStep ()
    {
        return DEFAULT_ENERGY_PER_STEP;
    }

    /**
     * Instructs this piece to consume the energy needed to take the
     * specified number of steps.
     */
    public void consumeEnergy (int steps)
    {
        energy -= energyPerStep() * steps;
    }

    /**
     * Returns true if this piece has at least enough energy to take one
     * step, false if not.
     */
    public boolean canTakeStep ()
    {
        return energy >= energyPerStep();
    }

    /**
     * Returns true if this piece prevents other pieces from occupying the
     * same square, or false if it can colocate.
     */
    public boolean preventsOverlap (Piece lapper)
    {
        return true;
    }

    /**
     * Some pieces interact with other pieces (bees pollinating flowers),
     * which takes place via this method. Depending on the type of
     * interaction, the piece can indicate that it consumed the other
     * piece, was consumed by it (entered), simply interacted with it
     * resulting in both pieces being changed or did nothing at all.
     */
    public Interaction maybeInteract (Piece other)
    {
        if (other instanceof Food && energy < 3*maximumEnergy()/4) {
            Food nibbly = (Food)other;
            int taken = nibbly.takeEnergy(this);
            energy = Math.min(maximumEnergy(), energy + taken);
            return nibbly.energy > 0 ?
                Interaction.INTERACTED : Interaction.CONSUMED;
        }
        return Interaction.NOTHING;
    }

    /**
     * Verifies that a move to the specified location is within the
     * piece's capabilities (ie. not too far, doesn't turn illegally,
     * doesn't cross illegal tiles).
     */
    public boolean canMoveTo (BugsBoard board, int nx, int ny)
    {
        // by default, ensure that the location is exactly one unit away
        // from our current location
        if (Math.abs(x[0] - nx) + Math.abs(y[0] - ny) != 1) {
            return false;
        }

        // and make sure we can traverse our final location
        return canTraverse(board, nx, ny);
    }

    /**
     * Returns true if this bug can traverse the board at the specified
     * coordinates.
     */
    public boolean canTraverse (BugsBoard board, int tx, int ty)
    {
        // by default, we assume that our tail always follows our head and
        // moves to tiles we already occupied, so we only need to
        // determine whether our head is moving onto a traversable tile
        return canTraverse(board.getTile(tx, ty));
    }

    /**
     * Allows a piece to modify the board terrain as a result of landing
     * on it.
     *
     * @return {@link Terrain#NONE} if the piece does not wish to modify
     * the terrain, or the terrain code for the new terrain type if it
     * does.
     */
    public Terrain modifyBoard (BugsBoard board, int tx, int ty)
    {
        return Terrain.NONE;
    }

    /**
     * Allows this bug to react to the state of the board at the
     * termination of the previous turn. A frog might eat another bug in
     * its path or rotate to face a bug that caught its attention.
     *
     * <em>Note:<em> it is legal for a piece to remove another piece from
     * the board as a result of its reaction, or modify the piece. In
     * either case, it should effect the appropriate modification to the
     * supplied game object directly.
     *
     * @return true if the piece changed its internal state as a result of
     * this reaction, false otherwise.
     */
    public boolean react (BugsObject bugsobj, Piece[] pieces)
    {
        // nothing by default
        return false;
    }

    /**
     * Enumerates the coordinates of the legal moves for this piece, given
     * the specified starting location. These moves need not account for
     * terrain or other potential blockage.
     */
    public void enumerateLegalMoves (int tx, int ty, PointSet moves)
    {
        // the default piece can move one in any of the four cardinal
        // directions
        moves.add(tx+1, ty);
        moves.add(tx-1, ty);
        moves.add(tx, ty+1);
        moves.add(tx, ty-1);
    }

    /**
     * Enumerates the coordinates of the tiles that this piece can attack
     * from its current location.
     */
    public void enumerateAttacks (PointSet set)
    {
        // by default, none
    }

    /**
     * Enumerates the coordinates of the tiles to which this piece attends
     * and may response if another piece moves into one of those spaces.
     */
    public void enumerateAttention (PointSet set)
    {
        // by default, none
    }

    /**
     * Creates the appropriate derivation of {@link PieceSprite} to render
     * this piece.
     */
    public PieceSprite createSprite ()
    {
        return new PieceSprite();
    }

    /**
     * This is normally not needed, but is used by the editor to assign
     * piece IDs to new pieces.
     */
    public void assignPieceId ()
    {
        _key = null;
        pieceId = 0;
        getKey();
    }

    // documentation inherited from interface DSet.Entry
    public Comparable getKey ()
    {
        if (_key == null) {
            if (pieceId == 0) {
                pieceId = ++_nextPieceId;
            }
            _key = new Integer(pieceId);
        }
        return _key;
    }

    @Override // documentation inherited
    public int hashCode ()
    {
        return pieceId;
    }

    @Override // documentation inherited
    public boolean equals (Object other)
    {
        return pieceId == ((Piece)other).pieceId;
    }

    @Override // documentation inherited
    public Object clone ()
    {
        try {
            Piece p = (Piece)super.clone();
            p.x = new short[x.length];
            System.arraycopy(x, 0, p.x, 0, x.length);
            p.y = new short[y.length];
            System.arraycopy(y, 0, p.y, 0, y.length);
            return p;
        } catch (CloneNotSupportedException cnse) {
            return null;
        }
    }

    /**
     * Starts this bug out at the specified coordinates. Creates our
     * segment position arrays. We will have already been configured with
     * our starting orientation.
     */
    protected void createSegments (int sx, int sy)
    {
        x = new short[] { (short)sx };
        y = new short[] { (short)sy };
    }

    /**
     * Returns true if this piece can traverse the specified type of
     * terrain.
     */
    protected boolean canTraverse (Terrain terrain)
    {
        return (terrain == Terrain.DIRT ||
                terrain == Terrain.LEAF_BRIDGE);
    }

    /** Returns the starting energy for pieces of this type. */
    protected int startingEnergy ()
    {
        return DEFAULT_STARTING_ENERGY;
    }

    /** Returns the maximum energy this piece can possess. */
    protected int maximumEnergy ()
    {
        return DEFAULT_MAXIMUM_ENERGY;
    }

    protected transient Integer _key;
    protected static int _nextPieceId;

    /** The default quantity of energy consumed to take a step. */
    protected static final int DEFAULT_ENERGY_PER_STEP = 10;

    /** The default starting quantity of energy. */
    protected static final int DEFAULT_STARTING_ENERGY = 100;

    /** The default maximum quantity of energy. */
    protected static final int DEFAULT_MAXIMUM_ENERGY = 250;

    /** Used to move one tile forward from an orientation. */
    protected static final int[] FWD_X_MAP = { 0, 1, 0, -1 };

    /** Used to move one tile forward from an orientation. */
    protected static final int[] FWD_Y_MAP = { -1, 0, 1, 0 };

    /** Used to move one tile backward from an orientation. */
    protected static final int[] REV_X_MAP = { 0, -1, 0, 1 };

    /** Used to move one tile backward from an orientation. */
    protected static final int[] REV_Y_MAP = { 1, 0, -1, 0 };
}
