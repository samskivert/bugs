//
// $Id$

package com.threerings.bugs.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.bugs.data.BugPath;

/**
 * Defines the requests that the client can make to the server.
 */
public interface BugsService extends InvocationService
{
    /**
     * Requests that the specified piece be moved to the specified location.
     */
    public void movePiece (Client client, int pieceId, int x, int y);

    /**
     * Requests that the specified piece be moved along the specified path.
     */
    public void setPath (Client client, BugPath path);
}
