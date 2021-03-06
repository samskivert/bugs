//
// $Id$

package com.threerings.bugs.editor;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.CommandButton;
import com.samskivert.swing.VGroupLayout;

import com.threerings.bugs.data.BugsCodes;
import com.threerings.bugs.data.pieces.*;
import com.threerings.bugs.util.BugsContext;

/**
 * Provides buttons for adding pieces of various types to the board.
 */
public class PieceCreator extends JPanel
{
    public PieceCreator (BugsContext ctx)
    {
        setLayout(new VGroupLayout());
        _ctx = ctx;

        add(new JLabel(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.pieces_fixed")));
        add(createPieceButton("leaf", new Leaf()));
        add(createPieceButton("sapling", new Sapling()));
        add(createPieceButton("tree", new Tree()));
        add(createPieceButton("sequoia", new Sequoia()));
        add(createPieceButton("flower", new Flower()));
        add(createPieceButton("anthill", new AntHill()));
        add(createPieceButton("soda_drop", new SodaDrop()));
        add(createPieceButton("honey_drop", new HoneyDrop()));

        add(new JLabel(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.pieces_predator")));
        add(createPieceButton("frog", new Frog()));
        add(createPieceButton("fly", new Fly()));
        add(createPieceButton("lizard", new Lizard()));
        add(createPieceButton("spider", new Spider()));
        add(createPieceButton("flytrap", new FlyTrap()));
        add(createPieceButton("wasp", new Wasp()));

        add(new JLabel(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.pieces_player")));
        add(createPieceButton("ant", new Ant()));
        add(createPieceButton("bee", new Bee()));
        add(createPieceButton("beetle", new Beetle()));
        add(createPieceButton("caterpillar", new Caterpillar()));
        add(createPieceButton("termite", new Termite()));
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

    protected BugsContext _ctx;
}
