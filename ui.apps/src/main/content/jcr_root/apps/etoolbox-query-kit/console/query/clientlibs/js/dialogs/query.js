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
(function (window, $, ns) {
    'use strict';

    const DATASOURCE_ENDPOINT = '/apps/etoolbox-query-kit/datasources/itemlist.json';
    const PARSE_ENDPOINT = '/apps/etoolbox-query-kit/services/parse';

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    function collectQueryParams($dialog) {
        const result = {};
        $dialog.find('input[name]').each(function (index, namedInput) {
            const $namedInput = $(namedInput);
            const value = $namedInput.val();
            if (!value) {
                return;
            }
            const paramName = $namedInput.attr('name');
            let paramValue = result[paramName];
            if (Array.isArray(paramValue)) {
                paramValue.push(value);
            } else if (paramValue) {
                paramValue = [paramValue];
                paramValue.push(value);
            } else {
                paramValue = value;
            }
            result[paramName] = paramValue;
        });
        return result;
    }

    function populateItemSources($dialog, $dsInput) {
        if ($dsInput.data('items-loaded') || !$dsInput.data('datasource-query')) {
            return;
        }

        const queryParams = collectQueryParams($dialog);
        queryParams['-query'] = $dsInput.data('datasource-query');

        $.ajax({
            url: DATASOURCE_ENDPOINT,
            dataType: 'json',
            data: queryParams,
            traditional: true,
            success: function (response) {
                $dsInput.data('items-loaded', true);
                $dsInput[0].items.clear();
                Array.isArray(response) && $.each(response, function (index, item) {
                    $dsInput[0].items.add({ content: { textContent: item.text }, value: item.value });
                });
            }
        });
    }

    function openQueryDialog(name, el, config, collection, selections) {
        const profile = ns.DataStore.getProfileName();
        const url = new URL(config.data.src, window.location);
        url.searchParams.set('profile', profile);
        const newConfig = {
            data: {
                nesting: 'hide',
                src: url.pathname + url.search
            }
        };
        ns.runAction('foundation.dialog', this, el, newConfig, collection, selections);
    }

    function completeQueryDialog(name, el) {
        const $dialog = $(el).closest('coral-dialog');
        const queryString = $dialog.find('[data-query]').data('query');

        $dialog.remove();
        if (!queryString) {
            return;
        }
        const queryParams = collectQueryParams($dialog);
        queryParams['-query'] = queryString;

        $.ajax({
            method: 'POST',
            url: PARSE_ENDPOINT,
            data: queryParams,
            traditional: true,
            beforeSend: function () {
                foundationUi.wait();
            },
            success: function (response) {
                ns.setEditorValue(response);
            },
            complete: function () {
                foundationUi.clearWait();
            }
        });
    }

    $(document).on('coral-overlay:open', '#queryDialog', function (e) {
        const $dialog = $(e.target);
        $dialog.find('[data-datasource-query]').each(function (index, dsInput) {
            populateItemSources($dialog, $(dsInput));
        });
    });

    /* -------
       Actions
       ------- */

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.dialog',
        handler: openQueryDialog
    });

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.toEditor',
        handler: completeQueryDialog
    });

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.toEditorAndRun',
        handler: completeQueryDialog
    });
})(window, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
