//
// $Id: ClientController.java 18 2004-11-27 18:18:47Z mdb $

package com.threerings.bugs.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.SessionObserver;

import com.threerings.crowd.data.BodyObject;

import com.threerings.bugs.data.BugsConfig;
import com.threerings.bugs.editor.EditorConfig;
import com.threerings.bugs.util.BugsContext;

import static com.threerings.bugs.Log.log;

/**
 * Responsible for top-level control of the client user interface.
 */
public class BugsClientController extends Controller
    implements SessionObserver
{
    /**
     * Creates a new client controller. The controller will set everything
     * up in preparation for logging on.
     */
    public BugsClientController (BugsContext ctx, BugsFrame frame)
    {
        // we'll want to keep these around
        _ctx = ctx;
        _frame = frame;

        // we want to know about logon/logoff
        _ctx.getClient().addClientObserver(this);

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx);
        _frame.setPanel(_logonPanel);
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
	String cmd = action.getActionCommand();

        if (cmd.equals("logoff")) {
            // request that we logoff
            _ctx.getClient().logoff(true);
            return true;
        }

        log.info("Unhandled action: " + action);
        return false;
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        log.info("Client did logon [client=" + client + "].");

        // keep the body object around for stuff
        _body = (BodyObject)client.getClientObject();

        if (_body.location != -1) {
            // if we were already in a location, go there (this probably
            // means we are reconnecting to an existing session)
            _ctx.getLocationDirector().moveTo(_body.location);

        } else {
            // for now request to start a bugs game
            _ctx.getParlorDirector().startSolitaire(
                new EditorConfig(), new InvocationService.ConfirmListener() {
                public void requestProcessed () {
                    // yay! nothing to do here
                }
                public void requestFailed (String cause) {
                    log.warning("Failed to create bugs game: " + cause);
                }
            });
        }
    }

    // documentation inherited
    public void clientObjectDidChange (Client client)
    {
        // regrab our body object
        _body = (BodyObject)client.getClientObject();
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        log.info("Client did logoff [client=" + client + "].");

        // reinstate the logon panel
        _frame.setPanel(_logonPanel);
    }

    protected BugsContext _ctx;
    protected BugsFrame _frame;
    protected BodyObject _body;

    // our panels
    protected LogonPanel _logonPanel;
}
