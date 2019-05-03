import module namespace form="http://basex.org/web/lib/form" at "/Users/michael/Code/basex-web/src/main/webapp/lib/form.xqy";
import module namespace functx="http://www.functx.com";

form:start(doc('src/main/webapp/models/blog.rng')//*:element[@name = "entry"])
