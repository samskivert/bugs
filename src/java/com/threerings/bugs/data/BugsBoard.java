//
// $Id$

package com.threerings.bugs.data;

import java.util.Arrays;

import com.threerings.io.SimpleStreamableObject;

/**
 * Describes the terrain of the Bugs game board.
 */
public class BugsBoard extends SimpleStreamableObject
{
    /** Creates a bugs board with the specified dimensions. */
    public BugsBoard (int width, int height, Terrain deftile)
    {
        _width = width;
        _height = height;
        _tiles = new int[width*height];
        Arrays.fill(_tiles, deftile.code);
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

    /**
     * Returns the tile value at the specified x and y coordinate.
     */
    public Terrain getTile (int xx, int yy)
    {
        return Terrain.map.get(_tiles[yy * _width + xx]);
    }

    /**
     * Updates the tile value at the specified x and y coordinate.
     */
    public void setTile (int xx, int yy, Terrain tile)
    {
        _tiles[yy * _width + xx] = tile.code;
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
