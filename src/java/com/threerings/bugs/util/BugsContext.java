//
// $Id: BugsContext.java 102 2005-01-17 14:15:54Z mdb $

package com.threerings.bugs.util;

import com.threerings.media.FrameManager;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;

import com.threerings.parlor.util.ParlorContext;

/**
 * Aggregates the various bits that are needed on the Bugs client.
 */
public abstract class BugsContext implements ParlorContext
{
    /**
     * Returns a reference to the message manager used by the client to
     * generate localized messages.
     */
    public abstract MessageManager getMessageManager ();

    /**
     * Returns a reference to our frame manager (used for media services).
     */
    public abstract FrameManager getFrameManager ();

    /**
     * Returns a reference to our key dispatcher.
     */
    public abstract KeyDispatcher getKeyDispatcher ();

    /**
     * Translates the specified message using the specified message bundle.
     */
    public String xlate (String bundle, String message)
    {
        MessageBundle mb = getMessageManager().getBundle(bundle);
        return (mb == null) ? message : mb.xlate(message);
    }
}
