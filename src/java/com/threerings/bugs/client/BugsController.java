//
// $Id$

package com.threerings.bugs.client;

import java.awt.event.ActionEvent;

import com.samskivert.swing.event.CommandEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.GameController;

import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.bugs.data.BugPath;
import com.threerings.bugs.data.BugsObject;

import static com.threerings.bugs.Log.log;

/**
 * Handles the logic and flow of the client side of a Bugs game.
 */
public class BugsController extends GameController
{
    /** The name of the command posted by the "Back to lobby" button in
     * the side bar. */
    public static final String BACK_TO_LOBBY = "back_to_lobby";

    /** A command that requests to move a piece. */
    public static final String MOVE_PIECE = "move_piece";

    /** A command that requests to set a path on a piece. */
    public static final String SET_PATH = "set_path";

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
        if (cmd.equals(SET_PATH)) {
            // ship off the set path request
            BugPath path = (BugPath)((CommandEvent)action).getArgument();
            log.info("Requesting " + path);
            _bugsobj.service.setPath(_ctx.getClient(), path);

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        _panel = new BugsPanel(_ctx, this);
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
    protected BugsPanel _panel;

    /** A casted reference to our game object. */
    protected BugsObject _bugsobj;
}
