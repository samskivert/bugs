//
// $Id$

package com.threerings.bugs.editor;

import java.awt.EventQueue;

import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.server.LocalDObjectMgr;
import com.threerings.presents.server.PresentsDObjectMgr;

import com.threerings.crowd.server.CrowdServer;
import com.threerings.parlor.server.ParlorManager;

import static com.threerings.bugs.Log.log;

/**
 * Handles the server-side of the Bugs! editor.
 */
public class EditorServer extends CrowdServer
{
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

        // initialize our managers
        parmgr.init(invmgr, plreg);

        log.info("Bugs! Editor server initialized.");
    }

    // documentation inherited
    protected PresentsDObjectMgr createDObjectManager ()
    {
        return new LocalDObjectMgr();
    }
}
