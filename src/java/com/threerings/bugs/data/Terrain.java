//
// $Id$

package com.threerings.bugs.data;

import java.util.HashMap;

/**
 * Models the different types of terrain that can be found on a board.
 */
public enum Terrain
{
    NONE        (-1, " "),

    // normal terrain types
    DIRT        (0, "."),
    MOSS        (1, "*"),
    TALL_GRASS  (2, "|"),
    WATER       (3, "^"),

    // "constructed" terrain types
    LEAF_BRIDGE (4, "$");

    /** The code used when encoding terrain types in the {@link BugsBoard}. */
    public int code;

    /** A character that can be used to display this terrain type when
     * dumping a board to the console. */
    public String glyph;

    /** Maps the enumeration's code back to the enumeration itself. */
    public static HashMap<Integer,Terrain> map = new HashMap<Integer,Terrain>();

    Terrain (int code, String glyph)
    {
        this.code = code;
        this.glyph = glyph;
        map(this);
    }

    protected static void map (Terrain terrain)
    {
        map.put(terrain.code, terrain);
    }
}
