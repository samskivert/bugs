//
// $Id$

package com.threerings.bugs.editor;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.bugs.data.BugsCodes;
import com.threerings.bugs.data.Terrain;

/**
 * Displays a drop-down of terrain types.
 */
public class TerrainSelector extends JPanel
{
    public TerrainSelector (ToyBoxContext ctx)
    {
        _ctx = ctx;
        setLayout(new HGroupLayout(HGroupLayout.STRETCH));

        add(new JLabel(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.terrain_select")),
            HGroupLayout.FIXED);

        TerrainSelection[] choices =
            new TerrainSelection[Terrain.STARTERS.size()];
        int ii = 0;
        for (Terrain terrain : Terrain.STARTERS) {
            choices[ii++] = new TerrainSelection(terrain);
        }
        add(_selector = new JComboBox(choices));
    }

    public Terrain getSelectedTerrain ()
    {
        return ((TerrainSelection)_selector.getSelectedItem()).terrain;
    }

    protected class TerrainSelection
    {
        public Terrain terrain;

        public TerrainSelection (Terrain terrain)
        {
            this.terrain = terrain;
        }

        public String toString ()
        {
            String msg = "m.terrain_" + terrain.toString().toLowerCase();
            return _ctx.xlate(BugsCodes.BUGS_MSGS, msg);
        }
    }

    protected ToyBoxContext _ctx;
    protected JComboBox _selector;
}
