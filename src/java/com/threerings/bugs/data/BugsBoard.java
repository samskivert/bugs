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
    /** A board terrain constant. */
    public static final int DIRT = 0;

    /** A board terrain constant. */
    public static final int MOSS = 1;

    /** A board terrain constant. */
    public static final int TALL_GRASS = 2;

    /** A board terrain constant. */
    public static final int WATER = 3;

    /** Creates a bugs board with the specified dimensions. */
    public BugsBoard (int width, int height, int deftile)
    {
        _tiles = new int[width*height];
        Arrays.fill(_tiles, deftile);
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
    public int getTile (int xx, int yy)
    {
        return _tiles[yy * _width + xx];
    }

    /**
     * Updates the tile value at the specified x and y coordinate.
     */
    public void setTile (int xx, int yy, int tile)
    {
        _tiles[yy * _width + xx] = tile;
    }

    /** The width and height of our board. */
    protected int _width, _height;

    /** Contains a 2D array of tiles, defining the terrain. */
    protected int[] _tiles;
}
