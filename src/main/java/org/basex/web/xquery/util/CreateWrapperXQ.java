package org.basex.web.xquery.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.basex.web.annotations.XQext;

/**
 * Creates an XQuery wrapper for the given functions.
 * @author Michael Seiferle, ms@basex.org
 *
 */
final class CreateWrapperXQ {
  /** Declare function template. */
  private static final String XQ_DECLARE_FUNC =
    "%s\ndeclare function %s(%s) %s {\n\t%s};\n\n";
/** Java Types @see #JavaFunc.java. **/
  private static final Map<Class<?>, String> JAVA =
    new HashMap<Class<?>, String>();

  /** Resulting function string. */
  private StringBuilder result = new StringBuilder(128);

  static {
    JAVA.put(String.class, "xs:string");
    JAVA.put(boolean.class, "xs:boolean");
    JAVA.put(Boolean.class, "xs:boolean");
    JAVA.put(byte.class, "xs:byte");
    JAVA.put(Byte.class, "xs:byte");
    JAVA.put(short.class, "xs:short");
    JAVA.put(Short.class, "xs:short");
    JAVA.put(int.class, "xs:integer");
    JAVA.put(Integer.class, "xs:integer");
    JAVA.put(long.class, "xs:long");
    JAVA.put(Long.class, "xs:long");
    JAVA.put(float.class, "xs:float");
    JAVA.put(Float.class, "xs:float");
    JAVA.put(double.class, "xs:double");
    JAVA.put(Double.class, "xs:double");
    JAVA.put(BigDecimal.class, "xs:decimal");
    JAVA.put(BigInteger.class, "xs:integer");
    JAVA.put(QName.class, "xs:string");
  }

  /** The class under consideration. */
  private Class<?> clz;
  /** The classes namespace. */
  private String namespace = "${cnspc}";

  /**
   * Outputs an annotated class with wrapper functions.
   * @param args classname
   */
  public static void main(final String... args) {
    if(args.length != 1) {
      usage();
      return;
    }
    try {
      CreateWrapperXQ wrp = new CreateWrapperXQ(args[0]);
      System.out.println(wrp.result.toString());
    } catch(ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Usage.
   */
  private static void usage() {
    System.err.println("Usage is <prog> class namespace");
  }

  /**
   * Initializes the wrapper creator.
   * @param classname the class to create a wrapper for
   * @throws ClassNotFoundException aah!
   */
  private CreateWrapperXQ(final String classname)
      throws ClassNotFoundException {
    getClazz(classname);

    for(Method m : clz.getMethods()) {
      if(m.isAnnotationPresent(XQext.class)) {
        translateToXQuery(m);
      }
    }
  }

/**
 * Translates a method to XQuery.
 * @param m Method
 */
private void translateToXQuery(final Method m) {
    XQext annotation = m.getAnnotation(XQext.class);
    assert Modifier.isStatic(m.getModifiers()) : "Methods must be static";
    final String xqN = annotation.name();
    final StringBuilder args = new StringBuilder();
    final StringBuilder jargs = new StringBuilder(namespace + ":");
    final StringBuilder xqDoc = new StringBuilder();
    final String ret = getXQReturnType(m);
    final String xQFunctionName = "${nsabbr}:" + xqN;
    getArgs(m, annotation.params(), annotation.paramhelp(), args, xqDoc,
                jargs);

    result.append(String.format(XQ_DECLARE_FUNC,
        xqDoc.toString(), xQFunctionName, args.toString(), ret,
        jargs.toString()));
}

/**
 * Return type.
 * @param m the method.
 * @return the XQuery type
 */
private String getXQReturnType(final Method m) {
    return JAVA.containsKey(m.getReturnType()) ? " as "
        + JAVA.get(m.getReturnType()) : "";
}

  /**
   * Returns XQuery type arguments.
   * @param m Java method
   * @param names parameter names
   * @param helps parameter help
   * @param xqDoc xQDoc String
   * @param args argument String
   * @param jargs java call arguments
   */
  private void getArgs(final Method m, final String[] names,
      final String[] helps, final StringBuilder args,
      final StringBuilder xqDoc, final StringBuilder jargs) {
    assert names.length == helps.length;
    assert names.length == m.getParameterTypes().length;
    jargs.append(m.getName());
    jargs.append("(");
    xqDoc.append("(:~\n");
    xqDoc.append(" : ");
    xqDoc.append(m.getAnnotation(XQext.class).help());
    xqDoc.append("\n");
    int i = 0;
    for(Class<?> par : m.getParameterTypes()) {
      args.append("$");
      args.append(names[i]);
      args.append(" as ");
      final String type = JAVA.containsKey(par) ? JAVA.get(par) : "element()";
      args.append(type);
      jargs.append("$");
      xqDoc.append(String.format(" : @param $%s %s\n", names[i], helps[i]));
      jargs.append(names[i]);
      if(i + 1 < names.length) {
        args.append(", ");
        jargs.append(", ");
      }
      i++;
    }
    jargs.append(")\n");
    xqDoc.append(":)");

    return;
  }

  /**
   * Sets the class object.
   * @param classname name
   * @throws ClassNotFoundException aah!
   */
  private void getClazz(final String classname) throws ClassNotFoundException {
    clz = Class.forName(classname);
  }
}
