(function (document, $, ns) {
    'use strict';

    const foundationUi = $(window).adaptTo('foundation-ui');

    $(document).ready(function () {
        $('#shareQueryButton').on('click', function () {
            const query = ns.getEditorValue();
            if (query) {
                foundationUi.notify('URL copied to clipboard');
                const urlWithoutParams = window.location.origin + window.location.pathname;
                navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
            }
        });
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
