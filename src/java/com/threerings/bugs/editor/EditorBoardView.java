//
// $Id$

package com.threerings.bugs.editor;

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
        // TODO
    }

    // documentation inherited from interface MouseListener
    public void mouseReleased (MouseEvent e)
    {
        // TODO
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
        if (mx != _mouse.x || my != _mouse.y) {
            invalidateTile(_mouse.x, _mouse.y);
            _mouse.x = mx;
            _mouse.y = my;
            invalidateTile(_mouse.x, _mouse.y);
        }
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseDragged (MouseEvent e)
    {
        mouseMoved(e);
    }
}
