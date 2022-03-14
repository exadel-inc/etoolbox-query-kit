(function ($, ns) {
    'use strict';

    const EDITOR_SELECTOR = '#queryEditor';
    const KEY_ENTER = 'Enter';

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

        editor.setValue(getEditorText());

        editor.on('keyup', function (cm, event) {
            if (!cm.state.completionActive && event.key !== KEY_ENTER) {
                CodeMirror.commands.autocomplete(cm, null, { completeSingle: false });
            }
        });

        /** Retrieves editor text from either the page URL or local storage */
        function getEditorText() {
            const storedValue = ns.DataStore.getLatestQueries().length ? ns.DataStore.getLatestQueries()[0] : '';
            const urlParams = new URLSearchParams(window.location.search);
            const queryParamValue = decodeURIComponent(urlParams.get('-query'));
            return queryParamValue && queryParamValue !== 'null' ? queryParamValue : storedValue;
        }

        /** Gets query string from the editor */
        ns.getEditorValue = function () {
            return editor.getValue().trim();
        };

        /** Assigns query string to the editor */
        ns.setEditorValue = function (query) {
            editor.setValue(query);
        };
    });
})(Granite.$, Granite.Eqk = (Granite.Eqk || {}));
