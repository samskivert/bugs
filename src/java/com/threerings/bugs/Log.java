//
// $Id: Log.java 18 2004-11-27 18:18:47Z mdb $

package com.threerings.bugs;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A placeholder class that contains a reference to the log object used by
 * this library.
 */
public class Log
{
    /** We dispatch our log messages through this logger. */
    public static Logger log = Logger.getLogger("com.threerings.bugs");
}
