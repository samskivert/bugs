//
// $Id$

package com.threerings.bugs.server;

import com.threerings.bugs.client.BugsService;
import com.threerings.bugs.data.BugPath;
import com.threerings.bugs.data.BugsMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link BugsProvider}.
 */
public class BugsDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public BugsDispatcher (BugsProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new BugsMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case BugsMarshaller.SET_PATH:
            ((BugsProvider)provider).setPath(
                source,
                (BugPath)args[0]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
