//
// $Id$

package com.threerings.bugs.data;

import java.util.Arrays;

import com.threerings.io.SimpleStreamableObject;

import static com.threerings.bugs.Log.log;

/**
 * Describes the terrain of the Bugs game board.
 */
public class BugsBoard extends SimpleStreamableObject
{
    /** Creates a bugs board with the specified dimensions. */
    public BugsBoard (int width, int height)
    {
        _width = width;
        _height = height;
        _tiles = new int[width*height];
        fill(Terrain.NONE);
    }

    /** A default contsructor for unserialization. */
    public BugsBoard ()
    {
    }

    /** Returns the width of the board. */
    public int getWidth ()
    {
        return _width;
    }

    /** Returns the height of the board. */
    public int getHeight ()
    {
        return _height;
    }

    /** Fills the board with the specified tile. */
    public void fill (Terrain tile)
    {
        Arrays.fill(_tiles, tile.code);
    }

    /**
     * Returns the tile value at the specified x and y coordinate.
     */
    public Terrain getTile (int xx, int yy)
    {
        int index = yy * _width + xx;
        if (index >= _tiles.length) {
            log.warning("Requested to get OOB tile " +
                        "[x=" + xx + ", y=" + yy + "].");
            Thread.dumpStack();
            return Terrain.NONE;
        } else {
            return Terrain.fromCode(_tiles[index]);
        }
    }

    /**
     * Updates the tile value at the specified x and y coordinate.
     */
    public void setTile (int xx, int yy, Terrain tile)
    {
        int index = yy * _width + xx;
        if (index >= _tiles.length) {
            log.warning("Requested to set OOB tile [x=" + xx + ", y=" + yy +
                        ", tile=" + tile + "].");
            Thread.dumpStack();
        } else {
            _tiles[index] = tile.code;
        }
    }

    /** Returns a string representation of this board. */
    public String toString ()
    {
        return "[" + _width + "x" + _height + "]";
    }

    /** The width and height of our board. */
    protected int _width, _height;

    /** Contains a 2D array of tiles, defining the terrain. */
    protected int[] _tiles;
}
