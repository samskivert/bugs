//
// $Id: BugsFrame.java 98 2005-01-15 00:59:35Z mdb $

package com.threerings.bugs.client;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.ManagedJFrame;

/**
 * Contains the user interface for the Bugs client.
 */
public class BugsFrame extends ManagedJFrame
    implements ControllerProvider
{
    /**
     * Constructs the top-level Bugs frame with the specified window
     * title.
     */
    public BugsFrame (String username)
    {
        super("..."); // the real title will be set later

        // we use these to record our frame position and dimensions
        _username = username;

        // we'll handle shutting things down ourselves
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // set up our initial frame size and position
//         Rectangle bounds = BugsPrefs.getClientBounds(gameId, username);
//         if (bounds != null) {
//             setBounds(bounds);
//         } else {
            setSize(1024, 768);
            SwingUtil.centerWindow(this);
//         }
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

//         // listen for changes in size and position and record them
//         addComponentListener(new ComponentAdapter() {
//             public void componentResized (ComponentEvent e) {
//                 BugsPrefs.setClientBounds(_gameId, _username, getBounds());
//             }
//             public void componentMoved (ComponentEvent e) {
//                 BugsPrefs.setClientBounds(_gameId, _username, getBounds());
//             }
//         });
    }

    /**
     * Sets the panel that makes up the entire client display.
     */
    public void setPanel (JPanel panel)
    {
        // remove the old panel
        getContentPane().removeAll();
	// add the new one
	getContentPane().add(panel, BorderLayout.CENTER);
        // swing doesn't properly repaint after adding/removing children
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Sets the controller for the outermost scope. This controller will
     * handle all actions that aren't handled by controllers of tigher
     * scope.
     */
    public void setController (Controller controller)
    {
        _controller = controller;
    }

    // documentation inherited
    public Controller getController ()
    {
        return _controller;
    }

    protected Controller _controller;
    protected String _username;
}
