//
// $Id$

package com.threerings.bugs.editor;

import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.bugs.client.BoardView;

import static com.threerings.bugs.client.BugsMetrics.*;

/**
 * Displays the board when in editor mode.
 */
public class EditorBoardView extends BoardView
    implements MouseListener, MouseMotionListener
{
    public EditorBoardView (ToyBoxContext ctx)
    {
        super(ctx);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // documentation inherited from interface MouseListener
    public void mouseClicked (MouseEvent e)
    {
        // nothing doing, we handle this ourselves
    }

    // documentation inherited from interface MouseListener
    public void mousePressed (MouseEvent e)
    {
        int tx = e.getX() / SQUARE, ty = e.getY() / SQUARE;

        // if there's a piece under the mouse, generate a ROTATE_PIECE

        // otherwise generate a PAINT_TERRAIN or CLEAR_TERRAIN
        _dragCommand = (e.getButton() == MouseEvent.BUTTON3) ?
            EditorController.CLEAR_TERRAIN : EditorController.PAINT_TERRAIN;
        EditorController.postAction(this, _dragCommand, new Point(tx, ty));
    }

    // documentation inherited from interface MouseListener
    public void mouseReleased (MouseEvent e)
    {
        _dragCommand = null;
    }

    // documentation inherited from interface MouseListener
    public void mouseEntered (MouseEvent e)
    {
        // nada
    }

    // documentation inherited from interface MouseListener
    public void mouseExited (MouseEvent e)
    {
        // nada
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseMoved (MouseEvent e)
    {
        int mx = e.getX() / SQUARE, my = e.getY() / SQUARE;
        updateMouseTile(mx, my);
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseDragged (MouseEvent e)
    {
        int mx = e.getX() / SQUARE, my = e.getY() / SQUARE;
        if (updateMouseTile(mx, my)) {
            // if we have a drag command and the mouse coordinates
            // changed, fire off another instance of the same command
            if (_dragCommand != null) {
                EditorController.postAction(
                    this, _dragCommand, new Point(mx, my));
            }
        }
    }

    /** The command we generate if we're dragging the mouse. */
    protected String _dragCommand;
}
