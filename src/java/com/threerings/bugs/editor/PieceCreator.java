//
// $Id$

package com.threerings.bugs.editor;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.CommandButton;
import com.samskivert.swing.VGroupLayout;

import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.bugs.data.BugsCodes;
import com.threerings.bugs.data.pieces.*;

/**
 * Provides buttons for adding pieces of various types to the board.
 */
public class PieceCreator extends JPanel
{
    public PieceCreator (ToyBoxContext ctx)
    {
        setLayout(new VGroupLayout());
        _ctx = ctx;

        add(new JLabel(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.pieces_fixed")));
        add(createPieceButton("leaf", new Leaf()));
        add(createPieceButton("tree", new Tree()));
        add(createPieceButton("flower", new Flower()));
        add(createPieceButton("anthill", new AntHill()));
        add(createPieceButton("soda_drop", new SodaDrop()));
        add(createPieceButton("honey_drop", new HoneyDrop()));

        add(new JLabel(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.pieces_predator")));
        add(createPieceButton("frog", new Frog()));

        add(new JLabel(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.pieces_player")));
        add(createPieceButton("ant", new Ant()));
        add(createPieceButton("bee", new Bee()));
    }

    protected CommandButton createPieceButton (String type, Piece piece)
    {
        String name = _ctx.xlate(BugsCodes.BUGS_MSGS, "m.piece_" + type);
        CommandButton button = new CommandButton();
        button.setText(name);
        button.setActionCommand(EditorController.CREATE_PIECE);
        button.setActionArgument(piece);
        button.addActionListener(EditorController.DISPATCHER);
        return button;
    }

    protected ToyBoxContext _ctx;
}
