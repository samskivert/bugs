//
// $Id$

package com.threerings.bugs.client;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.GameController;

import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Handles the logic and flow of the client side of a Bugs game.
 */
public class BugsController extends GameController
{
    /** The name of the command posted by the "Back to lobby" button in
     * the side bar. */
    public static final String BACK_TO_LOBBY = "back_to_lobby";

    // documentation inherited
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        _ctx = (ToyBoxContext)ctx;
        _config = (ToyBoxGameConfig)config;
        super.init(ctx, config);
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        _panel = new BugsPanel(_ctx, this);
        return _panel;
    }

    /** A casted reference to our context. */
    protected ToyBoxContext _ctx;

    /** A casted reference to our game config. */
    protected ToyBoxGameConfig _config;

    /** Contains our main user interface. */
    protected BugsPanel _panel;
}
