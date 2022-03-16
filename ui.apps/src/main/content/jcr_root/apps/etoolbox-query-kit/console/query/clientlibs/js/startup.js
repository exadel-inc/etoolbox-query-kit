(function ($, ns) {
    'use strict';

    $(document).ready(function () {
        const queryText = getQueryText();

        ns.setEditorValue(queryText);
        ns.runAction('eqk.query.execute', this);

        /** Retrieves editor text from either the page URL or local storage */
        function getQueryText() {
            const storedValue = ns.DataStore.getLatestQueries().length ? ns.DataStore.getLatestQueries()[0] : '';
            const urlParams = new URLSearchParams(window.location.search);
            const queryParamValue = decodeURIComponent(urlParams.get('-query'));
            return queryParamValue && queryParamValue !== 'null' ? queryParamValue : storedValue;
        }
    });

})(Granite.$, Granite.Eqk = (Granite.Eqk || {}));
