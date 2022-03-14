(function (document, $, ns) {
    'use strict';

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.share',
        handler: function () {
            const query = ns.getEditorValue();
            if (query) {
                const urlWithoutParams = window.location.origin + window.location.pathname;
                navigator.clipboard.writeText(urlWithoutParams + '?-query=' + encodeURIComponent(query));
                foundationUi.notify('Query URL copied to clipboard');
            }
        }
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
