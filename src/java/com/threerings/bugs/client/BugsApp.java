//
// $Id: BugsApp.java 56 2005-01-05 01:07:40Z mdb $

package com.threerings.bugs.client;

import java.io.IOException;
import java.util.logging.Level;

import com.samskivert.util.LoggingLogProvider;
import com.samskivert.util.OneLineLogFormatter;

import com.threerings.media.FrameManager;

import com.threerings.presents.client.Client;

import static com.threerings.bugs.Log.log;

/**
 * The launcher application for the Bugs! client.
 */
public class BugsApp
{
    public void init (String username)
        throws IOException
    {
        // create a frame
        _frame = new BugsFrame(username);
        _framemgr = FrameManager.newInstance(_frame);

        // create and initialize our client instance
        _client = new BugsClient();
        _client.init(_frame);
    }

    public void run (String server, int port, String username, String password)
    {
        // show the frame
        _frame.setVisible(true);

        Client client = _client.getContext().getClient();

        // pass them on to the client
        log.info("Using [server=" + server + ", port=" + port + "].");
        client.setServer(server, port);

        // configure the client with some credentials and logon
        if (username != null && password != null) {
            // create and set our credentials
            client.setCredentials(
                LogonPanel.createCredentials(username, password));
            client.logon();
        }

        _framemgr.start();
    }

    public BugsClient getClient ()
    {
        return _client;
    }

    /**
     * Performs the standard setup and starts the Bugs client application.
     */
    public static void start (BugsApp app, String server, int port,
                              String username, String password)
    {
        // set up the proper logging services
        com.samskivert.util.Log.setLogProvider(new LoggingLogProvider());
        OneLineLogFormatter.configureDefaultHandler();

        try {
            // initialize the app
            app.init(username);
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Error initializing application.", ioe);
        }

        // and run it
        app.run(server, port, username, password);
    }

    public static void main (String[] args)
    {
        String server = "localhost";
        if (args.length > 0) {
            server = args[0];
        }

        int port = Client.DEFAULT_SERVER_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.err.println(
                    "Invalid port specification '" + args[1] + "'.");
            }
        }

        String username = (args.length > 2) ? args[2] : null;
        String password = (args.length > 3) ? args[3] : null;

        start(new BugsApp(), server, port, username, password);
    }

    protected BugsClient _client;
    protected BugsFrame _frame;
    protected FrameManager _framemgr;
}
