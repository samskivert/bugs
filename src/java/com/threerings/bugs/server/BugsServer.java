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

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.server.ParlorManager;

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

        // configure the client manager to use the appropriate client class
        // clmgr.setClientClass(BugsClient.class);

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

        BugsServer server = new BugsServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to initialize server.", e);
        }
    }
}
