//
// $Id$

package com.threerings.bugs.data;

import com.threerings.bugs.client.BugsService;
import com.threerings.bugs.data.BugPath;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link BugsService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class BugsMarshaller extends InvocationMarshaller
    implements BugsService
{
    /** The method id used to dispatch {@link #setPath} requests. */
    public static final int SET_PATH = 1;

    // documentation inherited from interface
    public void setPath (Client arg1, BugPath arg2)
    {
        sendRequest(arg1, SET_PATH, new Object[] {
            arg2
        });
    }

}
