module namespace gb = "http://www.basex.org/myapp/guestbook";
import module namespace web="http://basex.org/lib/web";
declare function gb:list() as element(div)* {
    for $entry at $pos in doc('guestbook')//entry
    return
    <div class="entry" id="entry-{$pos}">
        <small>(#{$pos})</small> <h4>{$entry/name/text()}</h4>
        <p>{$entry/text/text()}</p>
    </div>
};

declare updating function gb:add($name, $message) {
    insert node
        <entry>
            <name>{$name}</name>
            <text>{$message}</text>
        </entry>
        as last into doc('guestbook')/entries
};
declare updating function gb:err(){
    let     $i :=  web:redirect("/app/guestbook/","Your Message has not been saved")
    return   (insert node <r i="{$i}" /> as last into <a />, insert node <void a="{$i}"/> as last into <r />)
};
