//
// $Id$

package com.threerings.bugs.editor;

import java.util.logging.Level;

import com.samskivert.util.LoggingLogProvider;
import com.samskivert.util.OneLineLogFormatter;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.server.ClientResolutionListener;

import com.threerings.bugs.client.BugsApp;

import static com.threerings.bugs.Log.log;

/**
 * Sets up the necessary business for the Bugs! editor.
 */
public class EditorApp extends BugsApp
{
    public void logon ()
    {
        // create our client object
        ClientResolutionListener clr = new ClientResolutionListener() {
            public void clientResolved (Name username, ClientObject clobj) {
                // fake up a bootstrap; I need to expose the mechanisms in
                // Presents that create it in a network environment
                BootstrapData data = new BootstrapData();
                data.clientOid = clobj.getOid();
                data.services = EditorServer.invmgr.bootlist;

                // and configure the client to operate using the server's
                // distributed object manager
                _client.getContext().getClient().gotBootstrap(
                    data, EditorServer.omgr);
            }

            public void resolutionFailed (Name username, Exception reason) {
                log.log(Level.WARNING, "Failed to resolve client [who=" +
                        username + "].", reason);
                // TODO: display this error
            }
        };
        EditorServer.clmgr.resolveClientObject(new Name("editor"), clr);
    }

    public static void main (String[] args)
    {
        // set up the proper logging services
        com.samskivert.util.Log.setLogProvider(new LoggingLogProvider());
        OneLineLogFormatter.configureDefaultHandler();

        // create our editor server which we're going to run in the same
        // JVM with the client
        EditorServer server = new EditorServer();
        try {
            server.init();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to initialize server.", e);
        }

        // let the BugsClientController know we're in editor mode
        System.setProperty("editor", "true");

        // now we create the client: we aren't actually logging in so we
        // don't need or want a server or username or whatnot
        final EditorApp app = new EditorApp();
        start(app, "localhost", -1, null, null);

        // post a runnable that will get executed after everything is
        // initialized and happy
        EditorServer.omgr.postRunnable(new Runnable() {
            public void run () {
                app.logon();
            }
        });
    }
}
