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

    const TABLE_URL = '/apps/etoolbox-query-kit/components/console/tableHost/jcr:content/data.html';

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    function executeAndUpdateUi(args) {
        if (!args || !args.query) {
            return;
        }
        $.ajax({
            url: TABLE_URL,
            type: 'GET',
            data: {
                '-query': args.query,
                '-offset': args.offset || 0,
                '-pageSize': args.pageSize || ns.DataStore.getPageSize(),
                '-total': args.passedTotal || 0,
                '-typeaware': true
            },
            beforeSend: function () {
                foundationUi.wait();
            },
            success: function (data) {
                const $result = $(data);
                $('#resultsColumn').empty().prepend($result);
                $(document).trigger('eqk-success-response', args);
            },
            error: function (error) {
                foundationUi.alert('EToolbox Query Console', 'Could not retrieve results' + (error.responseText ? ': ' + error.responseText : ''), 'error');
            },
            complete: function () {
                foundationUi.clearWait();
            }
        });
    }

    $(document).on('click', '.nav-button', function (e) {
        const $button = $(e.target);
        if ($button.is('.coral3-Button--primary')) {
            return;
        }
        executeAndUpdateUi(getQueryArgs($button));
    });

    function getQueryArgs($navButton) {
        if (!$navButton.length) {
            return {};
        }
        return {
            query: ns.DataStore.getCurrentQuery(),
            offset: $navButton.data('query-offset'),
            pageSize: $navButton.data('query-size'),
            passedTotal: getRenderedTotal()
        };
    }

    function getRenderedTotal() {
        const $resultsTable = $('#resultsTable');
        return ($resultsTable.length && $resultsTable.attr('data-foundation-layout-table-guesstotal')) || 0;
    }

    /* --------------------
       Actions registration
       -------------------- */

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.execute',
        handler: function () {
            if (!ns.getEditorValue()) {
                return;
            }
            ns.DataStore.setCurrentQuery(ns.getEditorValue());
            executeAndUpdateUi({
                query: ns.getEditorValue()
            });
        }
    });

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.replay',
        handler: function () {
            const $button = $('.nav-button.coral3-Button--primary');
            if (!$button.length) {
                return;
            }
            const args = getQueryArgs($button);
            executeAndUpdateUi(args);
        }
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
