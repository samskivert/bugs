//
// $Id$

package com.threerings.bugs.client;

/**
 * Contains information on a desired move for a particular piece.
 */
public class MoveData
{
    /** The id of the piece to be moved. */
    public int pieceId;

    /** The desired x position to which to move the piece. */
    public int x;

    /** The desired y position to which to move the piece. */
    public int y;
}
