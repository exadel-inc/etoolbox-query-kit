(function (ns) {
    'use strict';

    const foundationUi = $(window).adaptTo('foundation-ui');

    ns.alert = function (text, details) {
        foundationUi.alert('EToolbox Query Kit Console', text + (details ? ': ' + details : ''), 'error');
    };

    ns.notify = function (text) {
        foundationUi.notify(text);
    };
})(Granite.Eqk = (Granite.Eqk || {}));
