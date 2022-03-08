/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(document).ready(function () {
    'use strict';

    const $shareButton = $('#btnShare');
    const foundationUi = $(window).adaptTo('foundation-ui');

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
    }

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
            foundationUi.notify('URL copied to clipboard');
            const urlWithoutParams = window.location.origin + window.location.pathname;
            navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
        }
    });

    setTimeout(function () {
        updateQueryFromUrl();
    }, 0);
});
