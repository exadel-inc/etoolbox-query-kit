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
        $.ajax({
            url: QUERY_EXPORT_ENDPOINT + '.' + format,
            type: 'GET',
            data: { '-query': query, '-total': true },
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
                foundationUi.alert('EToolbox Query Console', 'Could not export results' + (error.responseText ? ': ' + error.responseText : ''), 'error');
            },
            complete: function () {
                foundationUi.clearWait();
            }
        });
    }

    function prepareData(data, format) {
        if (format === 'json') {
            return JSON.stringify(data, null, 2);
        }
        return data;
    }

    function getMimeType(format) {
        if (format === 'json') {
            return 'application/json';
        }
        if (format === 'csv') {
            return 'text/csv';
        }
        return 'text/html';
    }

    /* --------------------
       Actions registration
       -------------------- */

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.export',
        handler: performExport
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
