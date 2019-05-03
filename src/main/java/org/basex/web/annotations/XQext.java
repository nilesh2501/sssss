package org.basex.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface for automatic creation of XQuery wrappers used in BaseX.
 * @author BaseX Team 2005-11, BSD License
 * @author Michael Seiferle <ms@basex.org>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface XQext {

  /**
   * Name of the XQuery function
   * @return the name
   */
  String name();

  /**
   * Parameter names.
   * @return names
   */
  String[] params();

  /**
   * Help per parameter.
   * @return help Texts
   */
  String[] paramhelp();

  /**
   * General function Help
   * @return help
   */
  String help();
}
