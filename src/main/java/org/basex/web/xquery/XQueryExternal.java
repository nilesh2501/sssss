package org.basex.web.xquery;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.web.annotations.XQext;

/**
 * Sloppy Interface for communicating with Jetty from XQuery.
 * Will need a rewrite sometime soon.
 * @author BaseX Team 2005-11, BSD License
 * @author Michael Seiferle <ms@basex.org>
 */
public final class XQueryExternal {

  /** Name of the Session flash Cookie. */
  private static final String INFOCOOKIE = "__info__";

  /**
   * Sets the content type.
   * @param ct content type
   */
  @XQext(name = "content-type", params = { "type"},
      paramhelp = { "the content type"}, help = "")
  public static void contentType(final String ct) {
    BaseXContext.getResp().setContentType(ct);
  }


  /**
   * Disables caching for the current response.
   */
  @XQext(name = "no-cache", params = { }, paramhelp = { },
      help = "Disable caching for this page")
  public static void disableCache() {
    final HttpServletResponse resp = BaseXContext.getResp();
    if(resp.containsHeader("Cache-Control")) {
      resp.addHeader("Cache-Control", "no-cache");
      return;
    }
    resp.setHeader("Cache-Control", "no-cache");

  }

  /**
   * Redirects to a given {@code location}.
   * Additionally sets a cookie containing an informational {@code message}
   * @param location location to redirect to
   * @param message the message
   */
  @XQext(name = "redirect", params = { "location", "message"}, paramhelp = {
      "URI to redirect to", "system flash message"},
      help = "redirects the user to a given location, "
          + "saves a message that is retrievable via web:flash()")
  public static void redirect(final String location, final String message) {
    final HttpServletResponse resp = BaseXContext.getResp();
    resp.setHeader("Location", String.format("%s", location));
    resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
//    final Cookie c = new Cookie(XQueryExternal.INFOCOOKIE, message);
//    c.
//    resp.addCookie(c);
    // cookies have to be set manually here, as Chrome
    // otherwise fiddless with invalid date (c.setmaxAge(0)
    resp.setHeader("SET-COOKIE", XQueryExternal.INFOCOOKIE + "=" + message
        + ";path=/ "
        + "; HttpOnly");

  }
  /**
   * Retrieves the message saved in the cookie {@code __info}.
   * This message is set via {@link #redirect(String, String)}
   * @return Value of the cookie, iff it is set
   */
  @XQext(name = "flash", params = { }, paramhelp = { },
      help = "Retrieves the message saved in the current session flash cookie "
          + "and deletes the cookie.")
  public static String flash() {
    final Cookie[] cc = BaseXContext.getReq().getCookies();
    if(null == cc) return "";
    for(Cookie c : cc) {
      if(!XQueryExternal.INFOCOOKIE.equals(c.getName())) continue;
      final String ret = c.getValue();
      BaseXContext.getResp().setHeader(
          "SET-COOKIE",
          XQueryExternal.INFOCOOKIE + "=" + "deleted" + ";path=/ "
              + ";expires=Thu, 01-Jan-1970 00:00:01 GMT " + "; HttpOnly");
      return ret;
    }
    return "";
  }
  /**
   * Sets a cookie with the specified parameters.
   * @param name name
   * @param value value
   * @param expires expires in seconds
   * @param path path
   */
  @XQext(name = "set-cookie", params = { "name", "value", "expires", "path"},
      paramhelp = { "the cookie name", "the cookie value",
          "expires in seconds", "the cookie path"},
          help = "Sets a cookie with the specified parameters.")
  public static void setCookie(final String name, final String value,
      final int expires, final String path) {
    final Cookie c = new Cookie(name, value);
    c.setPath(path);
    c.setMaxAge(expires);
    BaseXContext.getResp().addCookie(c);
  }
  /**
   * Returns the cookie value.
   * @param name cookie name
   * @return value for Cookie 'name'
   */
  @XQext(name = "get-cookie", paramhelp = { "name of the cookie"},
      params = { "name"}, help = "")
  public static String getCookieValue(final String name) {
    HttpServletRequest req = BaseXContext.getReq();
    if(req != null) {
      Cookie[] cookies = req.getCookies();
      if(cookies == null) return "";
      for(Cookie cookie : cookies) {
        if(name.equals(cookie.getName())) return cookie.getValue();
      }
    }
    return "";
  }

  /** Ninja constructor. */
  private XQueryExternal() { }
}
