package org.basex.web.servlet.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.core.Prop;
import org.basex.core.cmd.Set;
import org.basex.io.serial.SerializerProp;
import org.basex.server.LocalSession;
import org.basex.server.Query;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.web.session.SessionFactory;

/**
 * This class represents a complete result. Consisting of the HTTP-headers and
 * the body
 *
 * @author Michael Seiferle, ms@basex.org
 */
public final class ResultPage {
    /**
     * Default Output Method, set via Maven.
     */
    static final String DEFAULT_OUTPUT_M = System.getProperty(
            "org.basex.web.default.outputmethod") == null
           ? "xhtml" : System.getProperty("org.basex.web.default.outputmethod");

    /** Servlet Response. */
    private HttpServletResponse resp;
    /** Servlet Request. */
    private HttpServletRequest req;

    /** QuerySession. */
    private final LocalSession session = new LocalSession(SessionFactory.get());
    /** Query. **/
    private Query query;

    /**
     * Constructs the result.
     *
     * @param q Query object
     * @param rsp the response
     * @param rq request
     */
    public ResultPage(final Query q, final HttpServletResponse rsp,
            final HttpServletRequest rq) {
        query = q;
        resp = rsp;
        req = rq;
        final TokenBuilder tb = new TokenBuilder();
        tb.addExt(SerializerProp.S_METHOD[0]).add("=").add(DEFAULT_OUTPUT_M);
        try {
            session.execute(new Set(Prop.SERIALIZER, tb));
        } catch (IOException e) {
            Util.notexpected(e);
        }
    }

    /**
     * Returns the response.
     *
     * @return the resp
     */
    public HttpServletResponse response() {
        return resp;
    }

    /**
     * Returns an empty ResultPage Object.
     *
     * @return empty ResultPage
     */
    public static ResultPage getEmpty() {
        return new ResultPage(null, null, null);
    }

    /**
     * Returns the request.
     *
     * @return the req
     */
    public HttpServletRequest request() {
        return req;
    }

    /**
     * Sets the request.
     *
     * @param rq the req to set
     */
    public void setReq(final HttpServletRequest rq) {
        this.req = rq;
    }

    /**
     * Sets the response.
     *
     * @param rp the resp to set
     */
    public void setResp(final HttpServletResponse rp) {
        this.resp = rp;
    }

    /**
     * Sets a query object.
     * @param qu Query
     */
    public void setQuery(final Query qu) {
        query = qu;
    }

    /**
     * Returns a query object.
     * @return the query
     */
    public Query query() {
        return query;
    }

    /**
     * Returns the current LocalSession.
     * @return the session
     */
    public LocalSession session() {
        return session;
    }

}
