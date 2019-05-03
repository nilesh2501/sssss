module namespace form = "http://basex.org/web/lib/form";
import module namespace functx="http://www.functx.com";
import module namespace web="http://basex.org/lib/web";

(:-
    Renders an input element.
-:)
declare function form:input($name as xs:string,
        $label as xs:string,
        $value as xs:string,
        $options
    ) as element(div){
        let $defaults := map { "type" :="text", "name" := $name }
        let $options := map:new( ($defaults,$options) )
        return
    <div class="row">
        <label for="{$name}">{$label}</label>
        {
            element{"input"}{
            	for $m in map:keys($options)
            	return
            	attribute {$m}{$options($m)}

            }
        }

    </div>
};

declare function form:textarea($name as xs:string,
        $label as xs:string,
        $value as xs:string,
        $options
    ) as element(div){
            let $defaults := map { "type" :="text", "name" := $name }
            let $options := map:new( ($defaults,$options) )
            return
        <div class="row">
            <label for="{$name}">{$label}</label>
            {
                element{"textarea"}{
                	for $m in map:keys($options)
                	return
                	attribute {$m}{$options($m)},
                	" "
                }
            }

        </div>
};

declare function form:list($name as xs:string,
        $label as xs:string,
        $values as xs:string*
    ) as element(div){
    <div>
        <label for="{$name}">{$label}</label>
        <textarea id="{$name}" name="{$name}" dojoType="dijit.form.Textarea" rows="8" cols="40">
{$values}
</textarea>
    </div>
};
(:-
 Renders a form
-:)
declare function form:form($d, $model, $action, $method) {
    <form action="/app/{$model}/{$action}" method="{$method}" accept-charset="utf-8">
        {form:start($d)}
      <p><input type="submit" value="Continue →" /></p>
    </form>
};
declare function form:input($elem){

    let $nam := string($elem/@name)
    let $pat := fn:concat(functx:path-to-node($elem/@name),"[@name='",$nam,"']")
    let $options := form:inspect($elem)
    return form:input($pat,$nam,$nam, $options)
};
declare function form:textarea($elem){

    let $nam := string($elem/@name)
    let $pat := fn:concat(functx:path-to-node($elem/@name),"[@name='",$nam,"']")
    let $options := form:inspect($elem)
    return form:textarea($pat,$nam,$nam, $options)
};
declare function form:inspect($elem){
    let $def := if($elem/*:text) then map {'dojoType':='dijit.form.Textarea','style':='width:400px'} else map {"dojoType":="dijit.form.TextBox"}
    return
        if($elem/*:data) then
            switch($elem/*:data/@*:type)
                case "date" return
                    map {"dojoType":="dijit.form.DateTextBox"}
                case "string" return
                    if(map:size(form:hasPattern($elem/*:data))>0) then
                        map:new( (form:hasPattern($elem/*:data), map:entry("dojoType","dijit.form.ValidationTextBox")))
                    else $def
                default return $def
        else $def
};
declare function form:hasPattern($type){
    let $toDojo := map {"pattern" := "regExp"}
    let $map := map:new(
      for $param in $type//*:param
        let $n := string($param/@name)
        return if( $toDojo($n) ) then map:entry($toDojo($n), $param/text())
               else map:entry($n, $param/text())
      )
  	return $map

};
declare function form:start($d) {
for $elem in $d/*
        let $uuid := web:uuid()
 return
 switch( name($elem))
  case "element"
  	return <div class="element">
  	    {
  	        switch($elem)
  	            case $elem/*:text return form:textarea($elem)
  	            case $elem/*:data return form:input($elem)
  	            default return ""}
  	        {form:start($elem)}</div>
  case "attribute"
  	return <div class="attr">
  	{
  	   form:input($elem)
  	}</div>
  case "ref"
  	return form:start(form:get-ref(root($elem), string($elem/@name)))
  case "oneOrMore"
  	return <div class="one-or-more" id="{$uuid}">{form:start($elem)}
  	                                      {form:add-one-or-more($elem, $uuid)}</div>
  case "optional"
  	return <div class="optional inner" title="{string($elem/@name)}">{form:add-optional($elem, $uuid)}<div id="t-{$uuid}" class="hidden inner">{form:start($elem)}</div></div>
  default return ""
  };
  declare function form:add-optional ($elem, $uuid) {
     <button dojoType="dijit.form.Button" type="button"> + Optional {string($elem/*:element/@*:name)}
      <script type="dojo/method" event="onClick" args="evt">
          dojox.fx.toggleClass("t-{$uuid}","hidden").play();
      </script>

      </button>

  };

declare function form:add-one-or-more ($elem, $uuid) {

        <button dojoType="dijit.form.Button" type="button"> + {string($elem/*:element/@*:name)}
        <div  id="{$uuid}-tmpl" class="hidden">
        <div>
           {form:start($elem)}
           </div>
        </div>
    <script type="dojo/method" event="onClick" args="evt">
        bxForm.oneOrMore('{$uuid}','{string($elem/*:element/@*:name)}');
    </script>

    </button>
};


declare function form:get-ref($root, $name as xs:string){
 $root//*:define[@name = $name]
};
(:
import module namespace form="http://basex.org/web/lib/form" at "/Users/michael/Code/basex-web/src/main/webapp/lib/form.xqm";
import module namespace functx="http://www.functx.com";

form:start(doc('blog-schem')//*:element[@name = "entry"])
:)