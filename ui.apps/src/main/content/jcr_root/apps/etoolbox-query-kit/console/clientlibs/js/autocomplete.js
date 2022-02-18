"use strict"

$(document).ready(function () {
    var textArea = document.querySelector('#queryTextArea'),
        editor = CodeMirror.fromTextArea(textArea, {
            lineNumbers: true,
            mode: "text/sql2",
            matchBrackets: true,
            lineWrapping: true
        });
    editor.on("keyup", function (cm, event) {
        if (!cm.state.completionActive &&
            event.keyCode !== 13) {
            CodeMirror.commands.autocomplete(cm, null, {completeSingle: false});
        }
    });
});