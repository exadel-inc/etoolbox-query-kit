'use strict';

$(document).ready(function () {
    const $shareButton = $('#shareButton');
    const $queryForm = $('#queryForm');
    const $executeButton = $('#executeButton');

    const successTooltip = new Coral.Tooltip().set({
        content: {
            innerHTML: 'URL copied to clipboard'
        },
        variant: 'success',
        target: '#shareButton',
        placement: 'bottom',
        interaction: 'off'
    });

    $shareButton.append(successTooltip);

    function updateQueryFromUrl() {
        const urlParams = decodeQueryUrlParams();
        const editor = document.querySelector('.CodeMirror').CodeMirror;
        if (urlParams.query && urlParams.query.trim().length > 0) {
            editor.setValue(urlParams.query);
        }
    }

    function decodeQueryUrlParams() {
        const urlParameters = getUrlParameters();
        return {
            query: urlParameters.query
        };
    };

    function getUrlParameters() {
        const urlParameters = {};
        const query = window.location.search.substring(1);
        const vars = query.split('&');
        for (let i = 0; i < vars.length; i++) {
            const pair = vars[i].split('=');
            try {
                urlParameters[pair[0]] = decodeURIComponent(pair[1]);
            } catch (e) {
                console.log('Param of url is not correct.');
                urlParameters[pair[0]] = '';
            }
        }
        return urlParameters;
    }

    $shareButton.on('click', function () {
        const editor = document.querySelector('.CodeMirror').CodeMirror;
        const query = editor.getValue();
        if (query) {
            successTooltip.open = true;
            const urlWithoutParams = window.location.origin + window.location.pathname;
            navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
            setTimeout(function () {
                successTooltip.open = false;
            }, 2000);
        }
    });

    setTimeout(function () {
        updateQueryFromUrl();
    }, 0);
});
