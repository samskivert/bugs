//
// $Id$

package com.threerings.bugs.editor;

import com.threerings.parlor.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

import com.threerings.bugs.data.BugsCodes;

/**
 * Defines the configuration for a bugs game.
 */
public class EditorConfig extends GameConfig
{
    /** The serialized data from a board file. */
    public byte[] board;

    /** The difficulty settings for our randomly generated board. */
    public int difficulty = 50;

    /** The size of our randomly generated board. */
    public int size = 25;

    // documentation inherited
    public Class getControllerClass ()
    {
        return EditorController.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.bugs.editor.EditorManager";
    }

    // documentation inherited
    public String getBundleName ()
    {
        return BugsCodes.BUGS_MSGS;
    }

    // documentation inherited
    public GameConfigurator createConfigurator ()
    {
        return null; // TODO
    }
}
