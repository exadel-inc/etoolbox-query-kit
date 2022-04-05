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
                const $result = $('<div/>').html(data);
                const $errorMessage = $result.find('.error');
                const isBackendException = $errorMessage.length;
                if (!isBackendException) {
                    $('#resultsColumn').empty().prepend($result.html());
                    $(document).trigger('eqk-success-response', args);
                } else {
                    ns.alert('Could not retrieve results', $errorMessage.text());
                    $errorMessage.remove();
                    $('#resultsColumn').empty().prepend($result.html());
                }
            },
            error: function (e) {
                ns.alert('Could not retrieve results', e.responseText);
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

    $(document).on('change', '.select-current-page-pagination', function (e) {
        let $selectedPage = $(e.target);
        if ($selectedPage.is('.is-selected')) {
            return;
        }
        let limit = $selectedPage.closest('coral-select').data('query-size')
        $selectedPage.data('query-size', limit);
        $selectedPage.data('query-offset', ($selectedPage[0].value - 1) * limit);
        executeAndUpdateUi(getQueryArgs($selectedPage));
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
