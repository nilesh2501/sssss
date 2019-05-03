package org.basex.web.servlet.impl;

import static org.basex.data.DataText.*;
import static org.basex.io.MimeTypes.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.io.IO;
import org.basex.io.in.TextInput;
import org.basex.io.serial.SerializerProp;
import org.basex.server.Query;
import org.basex.util.Token;
import org.basex.web.servlet.PrepareParamsServlet;
import org.basex.web.xquery.BaseXContext;
import org.eclipse.jetty.http.HttpException;

import com.google.common.base.Objects;

/**
 * Handles all that fancy MVC stuff.
 *
 * @author Michael Seiferle, <ms@basex.org>
 */
public class Xails extends PrepareParamsServlet {

    /** The content marker. */
    private static final byte[] CONTENT_MARK = Token.token("{{$content}}");
    /** XQuery controllers/action.xq in charge. */
    private File view;
    /** XQuery controllers/action.xq in charge. */
    private File controller;
    /** The template file. */
    private String template;


    @Override
    public synchronized void get(final HttpServletResponse resp,
            final HttpServletRequest req, final File f, final String get,
            final String post) throws IOException {
        initMVC(req);
        initQuery(req, resp, get, post);
        initHeaders(BaseXContext.getQuery(), resp);

        final String ct = Objects.firstNonNull(resp.getContentType(), "");

        if (!renderSimpleTemplate(req, ct)) {
            fillTemplate(resp);
        } else {
            BaseXContext.exec();
        }
        if (!resp.containsHeader("Location")) {
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        resp.flushBuffer();
    }

    /**
     * Fills a specified template with the results from the Query.
     * @param resp response.
     * @throws IOException on error.
     */
    private void fillTemplate(final HttpServletResponse resp)
            throws IOException {
        final InputStream is = new FileInputStream(fPath + "/layouts/" + template);
        writeBefore(resp.getOutputStream(), is);
        BaseXContext.exec();
        writeAfter(resp.getOutputStream(), is);
        is.close();
    }

    /**
     * Writes the layout to the stream.
     *
     * @param s Target stream
     * @param ti Buffered TextInput
     * @throws IOException on error
     */
    private void writeBefore(final OutputStream s, final InputStream ti)
            throws IOException {
        int curChar;
        int pos = -1;
        byte[] search = Xails.CONTENT_MARK;
        while ((curChar = ti.read()) > 0) {
            if (curChar != search[++pos]) {
                pos = -1;
                s.write(curChar);
            }
            if (pos + 1 == search.length) return;
        }
    }

    /**
     * Writes the remaining layout to the output stream.
     *
     * @param s Target stream
     * @param ti Buffered TextInput
     * @throws IOException on error
     */
    private void writeAfter(final OutputStream s, final InputStream ti)
            throws IOException {
        int cur;
        while ((cur = ti.read()) > 0) s.write(cur);
    }

    /**
     * Decides whether or not to render a simple template.
     *
     * @param req request
     * @param ct Content Type
     * @return true if the response is AJAX / JSON / TEXT
     */
    private boolean renderSimpleTemplate(final HttpServletRequest req,
            final String ct) {
        return req.getHeader("X-Requested-With") != null
                || ct.startsWith("application/json")
                || ct.startsWith("text/plain")
                || ct.startsWith("image/")
                || ct.startsWith("application/oc")
                || ct.startsWith("application/xm");
    }

    /**
     * Builds the resulting XQuery file in memory and returns the Query object.
     *
     * @param req the request
     * @param resp the response
     * @param post parameters as string
     * @param get parameters as string
     * @throws IOException on error.
     */
    private void initQuery(final HttpServletRequest req,
            final HttpServletResponse resp, final String get, final String post)
            throws IOException {
        final StringBuilder qry = prepareQuery();

        final TextInput tio = new TextInput(IO.get(view.getCanonicalPath()));
        qry.append(Token.string(tio.content()));
        tio.close();
        BaseXContext.prepare(qry.toString(), resp, req, get, post);
    }

    /**
     * Adds the controller import to the view file.
     *
     * @return controller import String
     * @throws IOException if file not found.
     */
    private StringBuilder prepareQuery() throws IOException {
        final StringBuilder qry = new StringBuilder(128);
        if (controller == null) return qry;

        final String cname = dbname(controller.getName());

        qry.append(String.format("import module namespace "
                + "%s=\"http://www.basex.org/myapp/%s\" " + "at \"%s\";\n",
                cname, cname, controller.getCanonicalPath()));
        return qry;
    }

    /**
     * Gets only the filename without suffix.
     *
     * @param n filename
     * @return chopped filename.
     */
    public final String dbname(final String n) {
        final int i = n.lastIndexOf(".");
        return (i != -1 ? n.substring(0, i) : n).replaceAll("[^\\w-]", "");
    }

    /**
     * Sets the default output method and encoding. N.B. contrary do the default
     * behaviour basex-web uses M_XHTML if nothing else is specified. N.B.2. the
     * XHTML mime type is set according to rfc3236
     *
     * @param q the query object
     * @param resp the response object
     * @throws IOException on error
     */
    final void initHeaders(final Query q,
            final HttpServletResponse resp) throws IOException {
        final SerializerProp sprop = new SerializerProp(q.options());

        final String enc = sprop.get(SerializerProp.S_ENCODING);

        // determine template
        if(!sprop.get(SerializerProp.S_TEMPLATE).isEmpty())
            this.template = sprop.get(SerializerProp.S_TEMPLATE);
        // set content type
        String type = sprop.get(SerializerProp.S_MEDIA_TYPE);
        if (type.isEmpty()) {
            // determine content type dependent on output method
            final String method = sprop.get(SerializerProp.S_METHOD);
            if (method.equals(M_RAW)) {
                type = APP_OCTET;
            } else if (method.equals(M_TEXT)) {
                type = "text/plain";
            } else if (method.equals(M_XML)) {
                type = APP_XML;
            } else if (Token.eq(method, M_JSON, M_JSONML)) {
                type = APP_JSON;
            } else if (Token.eq(method, M_HTML)) {
                type = TEXT_HTML;
            } else if (Token.eq(method, M_XHTML)) {
                type = "application/xhtml+xml";
            }
            resp.setContentType(type +";charset=" + enc);

        } else {
        resp.setContentType(type);
        }
    }

    /**
     * Sets the controller and action based on the sent request. Initializes the
     * current Page Buffer.
     *
     * @param req request.
     * @throws HttpException on error
     */
    private void initMVC(final HttpServletRequest req) throws HttpException {
        this.template = "default.html";
        this.controller = null;
        final String cntr = Objects.firstNonNull(
                req.getAttribute("xails.controller"), "page").toString();
        assert null != cntr : "Error no controller set";

        final String ac = Objects.firstNonNull(
                req.getAttribute("xails.action"), "index").toString();
        assert null != ac : "Error no action set";

        final String vpath = String.format("views/%s/%s.xq", cntr, ac);
        final String cpath = String.format("controllers/%s.xq", cntr);

        try {
            controller = super.requestedFile(cpath);
        } catch (final HttpException e) {
            ; // don't import controller if it is not found.
        }
        view = super.requestedFile(vpath);
    }
}
