package org.basex.web.session;

import org.basex.core.Context;


/**
 * Session singleton.
 * @author Michael Seiferle, <ms@basex.org>
 * @author BaseX Team
 *
 */
public final class SessionFactory {
  /** The Context Instance.*/
    private static final Context INSTANCE = new Context();

    /**
     * Singleton instance.
     * @return the singleton context
     */
    public static Context get() {
        return INSTANCE;
    }
    /** Private Constructor. */
    private SessionFactory() {
    }
}