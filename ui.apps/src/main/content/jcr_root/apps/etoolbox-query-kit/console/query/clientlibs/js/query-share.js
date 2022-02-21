(function (document, S) {
    'use strict';

    const foundationUi = $(window).adaptTo('foundation-ui');
    const $shareButton = $('#btnShare');

    // TODO: move this to actions.js, then delete this file

    $shareButton.on('click', function () {
        const editor = document.querySelector('.CodeMirror').CodeMirror;
        const query = editor.getValue();
        if (query) {
            foundationUi.notify('URL copied to clipboard');
            const urlWithoutParams = window.location.origin + window.location.pathname;
            navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
        }
    });


})(document, Granite.S);
