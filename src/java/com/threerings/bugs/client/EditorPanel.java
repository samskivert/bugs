//
// $Id$

package com.threerings.bugs.client;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.toybox.util.ToyBoxContext;

/**
 * Contains the primary user interface for the editor mode.
 */
public class EditorPanel extends JPanel
    implements ControllerProvider, PlaceView
{
    /** Displays our board. */
    public BugsBoardView view;

    /** Creates the main panel and its sub-interfaces. */
    public EditorPanel (ToyBoxContext ctx, EditorController ctrl)
    {
        _ctrl = ctrl;

	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	HGroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
	gl.setOffAxisPolicy(HGroupLayout.STRETCH);
	setLayout(gl);

        // create the board view
        add(view = new BugsBoardView(ctx));

        // create our side panel
        VGroupLayout sgl = new VGroupLayout(VGroupLayout.STRETCH);
        sgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        sgl.setJustification(VGroupLayout.TOP);
        JPanel sidePanel = new JPanel(sgl);

        JLabel vlabel = new JLabel("Bugs! Editor");
        vlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
        vlabel.setForeground(Color.black);
        sidePanel.add(vlabel, VGroupLayout.FIXED);

        // TODO: add the piece palette, etc.
        sidePanel.add(new JLabel("Piece palette"));

        // add a "back" button
        JButton back = new JButton("Back to lobby");
        back.setActionCommand(BugsController.BACK_TO_LOBBY);
        back.addActionListener(Controller.DISPATCHER);
        sidePanel.add(back, VGroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, HGroupLayout.FIXED);
    }

    // documentation inherited from interface
    public Controller getController ()
    {
        return _ctrl;
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /** Our game controller. */
    protected EditorController _ctrl;
}
