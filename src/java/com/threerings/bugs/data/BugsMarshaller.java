//
// $Id$

package com.threerings.bugs.data;

import com.threerings.bugs.client.BugsService;
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
    /** The method id used to dispatch {@link #movePiece} requests. */
    public static final int MOVE_PIECE = 1;

    // documentation inherited from interface
    public void movePiece (Client arg1, int arg2, int arg3, int arg4)
    {
        sendRequest(arg1, MOVE_PIECE, new Object[] {
            new Integer(arg2), new Integer(arg3), new Integer(arg4)
        });
    }

}
