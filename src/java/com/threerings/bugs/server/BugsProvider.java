//
// $Id$

package com.threerings.bugs.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.bugs.client.BugsService;

/**
 * Defines the server side of the {@link BugsService}.
 */
public interface BugsProvider extends InvocationProvider
{
    /** Handles a {@link BugsService#movePiece} request. */
    public void movePiece (ClientObject caller, int pieceId, int x, int y);
}
