'use strict';

$(document).ready(function () {
    const textArea = document.querySelector('#textQuery');
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
});
