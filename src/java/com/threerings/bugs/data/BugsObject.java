//
// $Id$

package com.threerings.bugs.data;

import com.threerings.parlor.game.GameObject;
import com.threerings.presents.dobj.DSet;

/**
 * Contains all distributed information for a Bugs game.
 */
public class BugsObject extends GameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>board</code> field. */
    public static final String BOARD = "board";

    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";
    // AUTO-GENERATED: FIELDS END

    /** Contains the representation of the game board. */
    public BugsBoard board;

    /** Contains information on all pieces on the board. */
    public DSet pieces;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>board</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBoard (BugsBoard value)
    {
        BugsBoard ovalue = this.board;
        requestAttributeChange(
            BOARD, value, ovalue);
        this.board = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToPieces (DSet.Entry elem)
    {
        requestEntryAdd(PIECES, pieces, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>pieces</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromPieces (Comparable key)
    {
        requestEntryRemove(PIECES, pieces, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updatePieces (DSet.Entry elem)
    {
        requestEntryUpdate(PIECES, pieces, elem);
    }

    /**
     * Requests that the <code>pieces</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setPieces (DSet pieces)
    {
        requestAttributeChange(PIECES, pieces, this.pieces);
        this.pieces = pieces;
    }
    // AUTO-GENERATED: METHODS END
}
