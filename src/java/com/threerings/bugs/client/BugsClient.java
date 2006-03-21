//
// $Id: BugsClient.java 102 2005-01-17 14:15:54Z mdb $

package com.threerings.bugs.client;

import java.io.File;
import java.io.IOException;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import com.samskivert.util.Config;
import com.samskivert.util.RunQueue;
import com.samskivert.util.StringUtil;
import com.threerings.media.FrameManager;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.MessageManager;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;
import com.threerings.toybox.client.ToyBoxDirector;

import com.threerings.bugs.data.BugsCodes;
import com.threerings.bugs.util.BugsContext;

/**
 * The Bugs client takes care of instantiating all of the proper
 * managers and loading up all of the necessary configuration and getting
 * the client bootstrapped.
 */
public class BugsClient
    implements RunQueue
{
    /**
     * Initializes a new client and provides it with a frame in which to
     * display everything.
     */
    public void init (BugsFrame frame)
        throws IOException
    {
        // create our context
        _ctx = createContextImpl();

        // create the directors/managers/etc. provided by the context
        createContextServices();

        // for test purposes, hardcode the server info
        _client.setServer("localhost", Client.DEFAULT_SERVER_PORT);

        // keep this for later
        _frame = frame;
        // TODO: set the title from a translated string
        _frame.setTitle(_ctx.xlate(BugsCodes.BUGS_MSGS, "m.game_title"));
        _keydisp = new KeyDispatcher(frame);

        // log off when they close the window
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (_client.isLoggedOn()) {
                    _client.logoff(true);
                }
                // and then get the heck out
                System.exit(0);
            }
        });

        // create our client controller and stick it in the frame
        _frame.setController(new BugsClientController(_ctx, _frame));
    }

    /**
     * Returns a reference to the context in effect for this client. This
     * reference is valid for the lifetime of the application.
     */
    public BugsContext getContext ()
    {
        return _ctx;
    }

    /**
     * Creates the {@link BugsContext} implementation that will be
     * passed around to all of the client code. Derived classes may wish
     * to override this and create some extended context implementation.
     */
    protected BugsContext createContextImpl ()
    {
        return new BugsContextImpl();
    }

    /**
     * Creates and initializes the various services that are provided by
     * the context. Derived classes that provide an extended context
     * should override this method and create their own extended
     * services. They should be sure to call
     * <code>super.createContextServices</code>.
     */
    protected void createContextServices ()
        throws IOException
    {
        // create the handles on our various services
        _client = new Client(null, this);

        // we use this to handle i18n
        _msgmgr = new MessageManager(MESSAGE_MANAGER_PREFIX);

        // create our managers and directors
        _locdir = new LocationDirector(_ctx);
        _occdir = new OccupantDirector(_ctx);
        _chatdir = new ChatDirector(_ctx, _msgmgr, "global");
        _pardtr = new ParlorDirector(_ctx);
    }

    // documentation inherited from interface RunQueue
    public void postRunnable (Runnable run)
    {
        // queue it on up on the awt thread
        EventQueue.invokeLater(run);
    }

    // documentation inherited from interface RunQueue
    public boolean isDispatchThread ()
    {
        return EventQueue.isDispatchThread();
    }

    /**
     * Given a subdirectory name (that should correspond to the calling
     * service), returns a file path that can be used to store local data.
     */
    public static String localDataDir (String subdir)
    {
        String appdir = System.getProperty("appdir");
        if (StringUtil.isBlank(appdir)) {
            appdir = ".bugs";
            String home = System.getProperty("user.home");
            if (!StringUtil.isBlank(home)) {
                appdir = home + File.separator + appdir;
            }
        }
        return appdir + File.separator + subdir;
    }

    /**
     * The context implementation. This provides access to all of the
     * objects and services that are needed by the operating client.
     */
    protected class BugsContextImpl extends BugsContext
    {
        /**
         * Apparently the default constructor has default access, rather
         * than protected access, even though this class is declared to be
         * protected. Why, I don't know, but we need to be able to extend
         * this class elsewhere, so we need this.
         */
        protected BugsContextImpl ()
        {
        }

        public Client getClient ()
        {
            return _client;
        }

        public DObjectManager getDObjectManager ()
        {
            return _client.getDObjectManager();
        }

        public Config getConfig ()
        {
            return _config;
        }

        public LocationDirector getLocationDirector ()
        {
            return _locdir;
        }

        public OccupantDirector getOccupantDirector ()
        {
            return _occdir;
        }

        public ChatDirector getChatDirector ()
        {
            return _chatdir;
        }

        public ParlorDirector getParlorDirector ()
        {
            return _pardtr;
        }

        public void setPlaceView (PlaceView view)
        {
            // stick the place view into our frame
            _frame.setPanel((JPanel)view);
        }

        public void clearPlaceView (PlaceView view)
        {
            // we'll just let the next place view replace our old one
        }

        public MessageManager getMessageManager ()
        {
            return _msgmgr;
        }

        public ToyBoxDirector getToyBoxDirector ()
        {
            return null; // not used
        }

        public FrameManager getFrameManager ()
        {
            return _frame.getFrameManager();
        }

        public KeyDispatcher getKeyDispatcher ()
        {
            return _keydisp;
        }
    }

    protected BugsContext _ctx;
    protected BugsFrame _frame;
    protected Config _config = new Config("bugs");

    protected Client _client;
    protected MessageManager _msgmgr;
    protected KeyDispatcher _keydisp;

    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected ChatDirector _chatdir;
    protected ParlorDirector _pardtr;

    /** The prefix prepended to localization bundle names before looking
     * them up in the classpath. */
    protected static final String MESSAGE_MANAGER_PREFIX = "rsrc.i18n";
}
