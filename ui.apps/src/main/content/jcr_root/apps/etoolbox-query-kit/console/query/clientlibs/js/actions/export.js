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
(function (document, $, ns) {
    'use strict';

    const QUERY_EXPORT_ENDPOINT = '/apps/etoolbox-query-kit/services/query';
    const DEFAULT_FORMAT = 'json';

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    function performExport(name, el, config) {
        $(el).closest('coral-popover')[0].hide();
        const query = ns.DataStore.getCurrentQuery();
        if (!query) {
            return;
        }
        const format = config.data.format || DEFAULT_FORMAT;
        const mimeType = getMimeType(format);
        if (format !== 'xlsx') {
            downloadString(query, format, mimeType);
        } else {
            downloadBlob(query, format);
        }
    }

    function getMimeType(format) {
        if (format === 'json') {
            return 'application/json';
        }
        if (format === 'csv') {
            return 'text/csv';
        }
        if (format === 'xlsx') {
            return 'application/octet-stream';
        }
        return 'text/html';
    }

    function downloadString(query, format, mimeType) {
        $.ajax({
            url: QUERY_EXPORT_ENDPOINT + '.' + format,
            type: 'GET',
            data: {'-query': query, '-total': true},
            traditional: true,
            beforeSend: function () {
                foundationUi.wait();
            },
            success: function (data) {
                const blob = new Blob([prepareData(data, format)], {type: mimeType});
                const blobUrl = URL.createObjectURL(blob);
                $(`<a href="${blobUrl}" download="query-result.${format}"></a>`)[0].click();
            },
            error: function (error) {
                ns.alert('Could not export results', error.responseText);
            },
            complete: function () {
                foundationUi.clearWait();
            }
        });
    }

    function prepareData(data, format) {
        return format === 'json' ? JSON.stringify(data, null, 2) : data;
    }

    function downloadBlob(query, format) {
        // Not using jQuery here because of the "Uncaught DOMException: Failed to read the 'responseText' property from
        // 'XMLHttpRequest'" known issue
        foundationUi.wait();
        const xhr = new XMLHttpRequest();
        xhr.open('GET', `${QUERY_EXPORT_ENDPOINT}.${format}?-query=${encodeURIComponent(query)}&-total=true`);
        xhr.responseType = 'blob';
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    const blobUrl = window.URL.createObjectURL(this.response);
                    $(`<a href="${blobUrl}" download="query-result.${format}"></a>`)[0].click();
                } else {
                    ns.alert('Could not export results', xhr.statusText);
                }
                foundationUi.clearWait();
            }
        };
        xhr.send();
    }

    /* --------------------
       Actions registration
       -------------------- */

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.export',
        handler: performExport
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
