module namespace blog = "http://www.basex.org/myapp/blog";
import module namespace web="http://basex.org/lib/web";

declare function blog:entries(){
    for $entry in doc('blog')//entry
        order by $entry/@date descending
    return $entry
};
declare function blog:find($uuid as xs:string){
    doc('blog')/entries/entry[./@uuid eq $uuid]
};

declare function blog:comment-box($uuid){
  <form action="/app/blog/comment" method="post" accept-charset="utf-8">
    <label for="name">Your Name</label><input type="text" name="name" value="" id="name" /><br />
    <label for="your_comment">Your Comment</label><input type="text" name="your_comment" value="" id="your_comment" />
    <input type="hidden" name="uuid" value="{$uuid}" id="uuid" />
    <p><input type="submit" value="Comment →" /></p>
  </form>

};
declare updating function blog:comment($name, $message, $uuid){
    insert node
        <comment date="{fn:current-date()}" uuid="{util:uuid()}">
            <from>{$name}</from>
            <body>{$message}</body>
        </comment>
        as last into blog:find($uuid)/comments

};
declare updating function blog:err(){
    let     $i :=  web:redirect("/app/blog/","Your comment has not been saved")
    return   (insert node <r i="{$i}" /> as last into <a />,
              insert node <void a="{$i}"/> as last into <r />)
};
