//
// $Id$

package com.threerings.bugs.data;

import java.util.ArrayList;
import java.util.Iterator;

import com.threerings.parlor.game.GameObject;
import com.threerings.presents.dobj.DSet;

import com.threerings.bugs.data.pieces.Piece;

/**
 * Contains all distributed information for a Bugs game.
 */
public class BugsObject extends GameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>service</code> field. */
    public static final String SERVICE = "service";

    /** The field name of the <code>tick</code> field. */
    public static final String TICK = "tick";

    /** The field name of the <code>board</code> field. */
    public static final String BOARD = "board";

    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";

    /** The field name of the <code>goals</code> field. */
    public static final String GOALS = "goals";
    // AUTO-GENERATED: FIELDS END

    /** The invocation service via which the client communicates with the
     * server. */
    public BugsMarshaller service;

    /** The current board tick count. */
    public short tick;

    /** Contains the representation of the game board. */
    public BugsBoard board;

    /** Contains information on all pieces on the board. */
    public DSet pieces;

    /** Contains information on all goals for this game. */
    public DSet goals;

    /** Returns the {@link #pieces} set as an array to allow for
     * simultaneous iteration and removal. */
    public Piece[] getPieceArray ()
    {
        return (Piece[])pieces.toArray(new Piece[pieces.size()]);
    }

    /**
     * Returns a list of pieces that overlap the specified piece given its
     * (hypothetical) current coordinates. If no pieces overlap, null will
     * be returned.
     */
    public ArrayList<Piece> getOverlappers (Piece piece)
    {
        ArrayList<Piece> lappers = null;
        for (Iterator iter = pieces.entries(); iter.hasNext(); ) {
            Piece p = (Piece)iter.next();
            if (p.pieceId != piece.pieceId && p.intersects(piece)) {
                if (lappers == null) {
                    lappers = new ArrayList<Piece>();
                }
                lappers.add(p);
            }
        }
        return lappers;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>service</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setService (BugsMarshaller value)
    {
        BugsMarshaller ovalue = this.service;
        requestAttributeChange(
            SERVICE, value, ovalue);
        this.service = value;
    }

    /**
     * Requests that the <code>tick</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTick (short value)
    {
        short ovalue = this.tick;
        requestAttributeChange(
            TICK, new Short(value), new Short(ovalue));
        this.tick = value;
    }

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
    public void setPieces (DSet value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        this.pieces = (value == null) ? null : (DSet)value.clone();
    }

    /**
     * Requests that the specified entry be added to the
     * <code>goals</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToGoals (DSet.Entry elem)
    {
        requestEntryAdd(GOALS, goals, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>goals</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromGoals (Comparable key)
    {
        requestEntryRemove(GOALS, goals, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>goals</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateGoals (DSet.Entry elem)
    {
        requestEntryUpdate(GOALS, goals, elem);
    }

    /**
     * Requests that the <code>goals</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setGoals (DSet value)
    {
        requestAttributeChange(GOALS, value, this.goals);
        this.goals = (value == null) ? null : (DSet)value.clone();
    }
    // AUTO-GENERATED: METHODS END
}
