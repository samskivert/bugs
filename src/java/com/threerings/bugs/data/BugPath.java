//
// $Id$

package com.threerings.bugs.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Contains a set of waypoints that will be traveled toward by a
 * particular bug.
 */
public class BugPath extends SimpleStreamableObject
{
    /** The id of the piece that will move along this path. */
    public int pieceId;

    /** Constructor used for serialization. */
    public BugPath ()
    {
    }

    /** Creates a bug path with the specified starting goal. */
    public BugPath (int pieceId, int x, int y)
    {
        this.pieceId = pieceId;
        _coords = new int[] { x, y };
    }

    /** Appends the supplied coordinates to this path. */
    public BugPath append (int x, int y)
    {
        BugPath path = new BugPath();
        path.pieceId = pieceId;
        path._coords = new int[_coords.length+2];
        System.arraycopy(_coords, 0, path._coords, 0, _coords.length);
        path._coords[_coords.length] = x;
        path._coords[_coords.length+1] = y;
        return path;
    }

    /** Returns the length of this path. */
    public int getLength ()
    {
        return _coords.length / 2;
    }

    /** Returns the x coordinate of the specified position. */
    public int getX (int position)
    {
        return _coords[position*2];
    }

    /** Returns the y coordinate of the specified position. */
    public int getY (int position)
    {
        return _coords[position*2+1];
    }

    /** Returns the x coordinate of our next goal. */
    public int getNextX ()
    {
        return getX(Math.min(_position, getLength()-1));
    }

    /** Returns the y coordinate of our next goal. */
    public int getNextY ()
    {
        return getY(Math.min(_position, getLength()-1));
    }

    /**
     * Returns true if the specified node is the tail of this path.
     */
    public boolean isTail (int x, int y)
    {
        return (x == _coords[_coords.length-2] &&
                y == _coords[_coords.length-1]);
    }

    /**
     * Called to indicate that our bug reached the current goal.
     *
     * @return false if there are yet more goals to reach, true if the
     * path is completed.
     */
    public boolean reachedGoal ()
    {
        return (++_position >= getLength());
    }

    // documentation inherited
    public String toString ()
    {
        return "[pid=" + pieceId + ", n=(" + getNextX() + ", " +
            getNextY() + "), l=" + getLength() + ", pos=" + _position + "]";
    }

    /** Contains the coordinates of our various goals. */
    protected int[] _coords;

    /** Records the position of the current coordinates being targeted by
     * the bug following this path. */
    protected transient int _position;
}
