//
// $Id$

package com.threerings.bugs.editor;

import java.awt.Point;
import java.awt.event.ActionEvent;

import com.samskivert.swing.event.CommandEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.GameController;

import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.bugs.data.BugsObject;
import com.threerings.bugs.data.ModifyBoardEvent;
import com.threerings.bugs.data.Terrain;

/**
 * Handles the logic and flow for the Bugs! board editor.
 */
public class EditorController extends GameController
{
    /** The name of the command posted by the "Back to lobby" button in
     * the side bar. */
    public static final String BACK_TO_LOBBY = "back_to_lobby";

    /** Instructs us to paint the specified coordinate with the currently
     * selected terrain type. */
    public static final String PAINT_TERRAIN = "paint_terrain";

    /** Instructs us to clear the specified terrain coordinate. */
    public static final String CLEAR_TERRAIN = "clear_terrain";

    /** Instructs us to "roll" the terrain selection up or down. */
    public static final String ROLL_TERRAIN_SELECTION = "roll_terrain";

    // documentation inherited
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        _ctx = (ToyBoxContext)ctx;
        _config = (ToyBoxGameConfig)config;
        super.init(ctx, config);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);
        _bugsobj = (BugsObject)plobj;

        // we may be returning to an already started game
        if (_bugsobj.isInPlay()) {
            _panel.view.startGame(_bugsobj);
        }
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        String cmd = action.getActionCommand();
        Object arg = (action instanceof CommandEvent) ?
            ((CommandEvent)action).getArgument() : null;

        if (cmd.equals(PAINT_TERRAIN)) {
            Point p = (Point)arg;
            _bugsobj.postEvent(
                new ModifyBoardEvent(_bugsobj.getOid(), p.x, p.y,
                                     _panel.terrain.getSelectedTerrain()));

        } else if (cmd.equals(CLEAR_TERRAIN)) {
            Point p = (Point)arg;
            _bugsobj.postEvent(
                new ModifyBoardEvent(
                    _bugsobj.getOid(), p.x, p.y, Terrain.NONE));

        } else if (cmd.equals(ROLL_TERRAIN_SELECTION)) {
            _panel.terrain.rollSelection((Integer)arg);

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        _panel = new EditorPanel(_ctx, this);
        return _panel;
    }

    // documentation inherited
    protected void gameDidStart ()
    {
        super.gameDidStart();

        // we may be returning to an already started game
        _panel.view.startGame(_bugsobj);
    }

    // documentation inherited
    protected void gameWillReset ()
    {
        super.gameWillReset();
        _panel.view.endGame();
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();
        _panel.view.endGame();
    }

    /** A casted reference to our context. */
    protected ToyBoxContext _ctx;

    /** A casted reference to our game config. */
    protected ToyBoxGameConfig _config;

    /** Contains our main user interface. */
    protected EditorPanel _panel;

    /** A casted reference to our game object. */
    protected BugsObject _bugsobj;
}
