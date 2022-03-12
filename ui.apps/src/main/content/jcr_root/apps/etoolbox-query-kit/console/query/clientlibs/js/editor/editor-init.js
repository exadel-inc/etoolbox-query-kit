(function (ns, $) {
    'use strict';
    const EDITOR_SELECTOR = '#queryEditor';
    const ENTER = 'Enter';

    $(document).ready(function () {
        const textArea = $(EDITOR_SELECTOR)[0];
        if (!textArea) {
            throw new Error(`[EQK]: can't find editor with id=${EDITOR_SELECTOR}`);
        }

        const editor = CodeMirror.fromTextArea(textArea, {
            mode: 'text/sql2',
            matchBrackets: true,
            lineWrapping: true
        });

        editor.on('keyup', function (cm, event) {
            if (!cm.state.completionActive && event.key !== ENTER) {
                CodeMirror.commands.autocomplete(cm, null, { completeSingle: false });
            }
        });

        editor.on('blur', function () {
            ns.DataStore.setQuery(editor.getValue());
        });

        editor.setValue(ns.DataStore.getQuery());

        /** Util to get query from editor */
        ns.getEditorValue = function () {
            return editor.getValue();
        };

        /** Util to set query to editor */
        ns.setEditorValue = function (query) {
            editor.setValue(query);
        };
    });
})(Granite.Eqk = (Granite.Eqk || {}), Granite.$);
