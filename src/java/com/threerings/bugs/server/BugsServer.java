//
// $Id: BugsServer.java 68 2005-01-13 17:16:41Z mdb $

package com.threerings.bugs.server;

import java.io.File;
import java.util.logging.Level;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.LoggingLogProvider;
import com.samskivert.util.OneLineLogFormatter;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.ClientResolutionListener;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.LocalDObjectMgr;
import com.threerings.presents.server.PresentsDObjectMgr;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.server.ParlorManager;

import com.threerings.bugs.client.BugsApp;

import static com.threerings.bugs.Log.log;

/**
 * The main entry point and general organizer of everything that goes on
 * in the Bugs game server process.
 */
public class BugsServer extends CrowdServer
{
//     /** The connection provider used to obtain access to our JDBC
//      * databases. */
//     public static ConnectionProvider conprov;

    /** The parlor manager in operation on this server. */
    public static ParlorManager parmgr = new ParlorManager();

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the base server initialization
        super.init();

//         // configure the client manager to use the appropriate client class
//         clmgr.setClientClass(BugsClient.class);

//         // create our database connection provider
//         conprov = new StaticConnectionProvider(BugsConfig.getJDBCConfig());

//         // set up our authenticator
//         Authenticator auth = BugsConfig.getAuthenticator();
//         if (auth != null) {
//             conmgr.setAuthenticator(auth);
//         }

        // initialize our managers
        parmgr.init(invmgr, plreg);

        log.info("Bugs server initialized.");
    }

    /**
     * Called in standalone mode to cause the standalong clien to "logon".
     */
    public void startStandaloneClient (final BugsApp app)
    {
        // create our client object
        ClientResolutionListener clr = new ClientResolutionListener() {
            public void clientResolved (Name username, ClientObject clobj) {
                // fake up a bootstrap; I need to expose the mechanisms in
                // Presents that create it in a network environment
                BootstrapData data = new BootstrapData();
                data.clientOid = clobj.getOid();
                data.services = invmgr.bootlist;

                // and configure the client to operate using the server's
                // distributed object manager
                app.getClient().getContext().getClient().gotBootstrap(
                    data, omgr);
            }

            public void resolutionFailed (Name username, Exception reason) {
                log.log(Level.WARNING, "Failed to resolve client " +
                        "[who=" + username + "].", reason);
                // TODO: display this error
            }
        };
        clmgr.resolveClientObject(new Name("standalone"), clr);
    }

    // documentation inherited
    protected PresentsDObjectMgr createDObjectManager ()
    {
        if (standaloneMode()) {
            return new LocalDObjectMgr();
        } else {
            return super.createDObjectManager();
        }
    }

//     /**
//      * Returns the port on which the connection manager will listen for
//      * client connections.
//      */
//     protected int getListenPort ()
//     {
//         return BugsConfig.getServerPort();
//     }

    public static void main (String[] args)
    {
        // set up the proper logging services
        com.samskivert.util.Log.setLogProvider(new LoggingLogProvider());
        OneLineLogFormatter.configureDefaultHandler();

        final BugsServer server = new BugsServer();
        try {
            server.init();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to initialize server.", e);
        }

        if (standaloneMode()) {
            // create the client: we aren't actually logging in so we
            // don't need or want a server or username or whatnot
            final BugsApp app = new BugsApp();
            BugsApp.start(app, "localhost", -1, null, null);

            // post a runnable that will get executed after everything is
            // initialized and happy
            omgr.postRunnable(new Runnable() {
                public void run () {
                    server.startStandaloneClient(app);
                }
            });

        } else {
            server.run();
        }
    }

    /** Returns true if we are running in standalone (combined client and
     * server) mode. */
    protected static boolean standaloneMode ()
    {
        return "true".equalsIgnoreCase(System.getProperty("standalone", ""));
    }
}
