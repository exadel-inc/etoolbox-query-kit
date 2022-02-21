(function (ns, $) {

    'use strict';

    const textArea = $('#textQuery')[0];
    const editor = CodeMirror.fromTextArea(textArea, {
        mode: 'text/sql2',
        matchBrackets: true,
        lineWrapping: true
    });
    editor.on('keyup', function (cm, event) {
        if (!cm.state.completionActive &&
            event.keyCode !== 13) {
            CodeMirror.commands.autocomplete(cm, null, { completeSingle: false });
        }
    });
    editor.on('blur', function() {
        ns.DataStore.setQuery(editor.getValue());
    });
    editor.setValue(ns.DataStore.getQuery())


})(Granite.Eqk = (Granite.Eqk || {}),  Granite.$);
