//
// $Id$

package com.threerings.bugs.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Defines the requests that the client can make to the server.
 */
public interface BugsService extends InvocationService
{
    /** Requests that the specified piece be moved. */
    public void movePiece (Client client, int pieceId, int x, int y);
}
