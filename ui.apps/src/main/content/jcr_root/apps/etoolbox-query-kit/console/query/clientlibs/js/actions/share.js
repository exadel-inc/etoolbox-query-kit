(function (document, $, ns) {
    'use strict';

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.shareQuery',
        handler: function () {
            const query = ns.getEditorValue();
            if (query) {
                foundationUi.notify('URL copied to clipboard');
                const urlWithoutParams = window.location.origin + window.location.pathname;
                navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
            }
        }
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
