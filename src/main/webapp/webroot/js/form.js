// Create a new class named "mynamespace.myClass"
dojo.require("dojox.string.sprintf");
dojo.declare("basex.Form", null, {
    // Custom properties and methods here
    oneOrMore: function(
    /*String*/
    uuid,
    /*String*/
    name) {
        var nod = dojo.byId(uuid + '-tmpl');
        var newNode = dojo.clone(nod);
        var newId = dojox.uuid.generateRandomUuid();
        var buttonId = dojox.uuid.generateRandomUuid();
        dojo.attr(newNode, "id", newId);
        dojo.attr(newNode, "class", "inner");
        dojo.place(newNode, uuid);
        dojo.create("button", {
            id: buttonId,
            iconClass: "dijitEditorIcon dijitEditorIconCut",
            showLabel: true,
            dojoType: "dijit.form.Button",
            type: "button",
            title: "remove"
        },
        newNode);
        var button = new dijit.form.Button({
            label: "-",
            onClick: function() {
                // Do something:
                new basex.Form().areYouSure({
                    title:"Are you sure you want do <strong>delete</strong> this %s?",
                    content:"Remove %s",
                    name: name,
                    uuid: newId
                });
            }
        },
        buttonId);
        dojo.parser.parse(newNode);
    },
    areYouSure: function(/*Object*/ options){
        var formatter = new dojox.string.sprintf.Formatter(options.title);
        var contentf = new dojox.string.sprintf.Formatter(options.content);
        var secondDlg = new dijit.Dialog({
            title: formatter.format(options.name),
            style: "width: 300px"
        });
        var ok = new dijit.form.Button({
            label: contentf.format(options.name),
            onClick: function() {
                // Do something:
                // dojox.fx.toggleClass(options.uuid,"hidden").play();

                dojo.destroy(options.uuid);
                secondDlg.hide();
                secondDlg.destroy();
            }
        });

        var cancel = new dijit.form.Button({
            label: options.content,
            onClick: function() {
                // Do something:
                secondDlg.hide();
            }
        });
        /** TODO */
        secondDlg.attr("content", ok);
        secondDlg.show();

    }
});
var bxForm = new basex.Form();
