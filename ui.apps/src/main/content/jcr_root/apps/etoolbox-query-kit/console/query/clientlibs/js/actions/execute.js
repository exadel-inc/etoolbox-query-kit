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

    const DEFAULT_LIMIT = 2;

    const $editRowForm = $('#editRowDialogForm');

    let currentPage = 1;
    let offset = 0;
    const limit = DEFAULT_LIMIT;

    let totalCount;

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.executeQuery',
        handler: function () {
            const query = ns.getEditorValue();
            if (!query) return;
            updateUrlParams(query);
            currentPage = 1;
            totalCount = 0;
            offset = 0;
            updateResult(query);
        }
    });

    function updateUrlParams(query) {
        if (query && query.trim().length > 0 && history.pushState) {
            const url = new URL(window.location);
            url.searchParams.set('-query', encodeURIComponent(query));
            url.searchParams.set('-measure', 'true');
            window.history.pushState({}, '', url.toString());
        }
    }

    function updateResult(query) {
        $.ajax({
            url: TABLE_URL,
            type: 'GET',
            data: { '-query': query, '-offset': offset, '-pageSize': limit, '-measure': !totalCount, '-typeaware': true },
            beforeSend: function () {
                foundationUi.wait();
            },
            success: function (data) {
                $(document).trigger('query-kit:success-response', query);
                const $table = $(data);
                offset = 0;
                totalCount = $table.attr('data-foundation-layout-table-guesstotal');
                updateTable($table);
            },
            error: function (error) {
                foundationUi.alert('EToolbox Query Kit', 'Could not retrieve results' + (error.responseText ? ': ' + error.responseText : ''), 'error');
            },
            complete: function () {
                foundationUi.clearWait();
            }
        });
    }

    function updateTable($table) {
        $('#resultsTable').remove();
        $('.pagination').remove();
        $('#resultsColumn').prepend($table);
    }

    $(document).on('click', '.nav-page-button', function (e) {
        currentPage = e.target.value;
        const query = ns.getEditorValue();
        offset = (currentPage - 1) * limit;
        updateResult(query);
    });

    $(document).on('click', '.nav-button', function (e) {
        if ($(e.target).is('.next')) {
            offset = currentPage * limit;
            currentPage++;
        } else {
            currentPage--;
            offset = (currentPage - 1) * limit;
        }
        const query = ns.getEditorValue();
        updateResult(query);
    });

    $editRowForm.submit(function (e) {
        e.preventDefault();
        const form = $(this);
        const url = form.attr('action');
        const data = form.serialize();
        $.ajax({
            url: url,
            type: 'POST',
            data: data,
            success: function () {
                foundationUi.notify('Row successfully edited');
            },
            error: function (error) {
                foundationUi.alert('EToolbox Query Kit', 'Edit row error' + (error.responseText ? ': ' + error.responseText : ''), 'error');
            }
        });
    });

    $(document).on('dblclick', '.result-table-cell', function (e) {
        const property = e.target.getAttribute('data-name');
        const type = e.target.getAttribute('data-type');
        const path = e.target.getAttribute('data-path');

        $.ajax({
            url: '/apps/etoolbox-query-kit/console/dialogs/editCell.html',
            type: 'GET',
            data: { path: path, property: property, type: type },
            beforeSend: function () {
                foundationUi.wait();
            },
            success: function (data) {
                const action = $(data).find('input[name="path"]')[0].value;
                const dialogContent = $(data).find('div[id="editCellDialogContainer"]');
                $('#editCellDialog form').attr('action', action);
                $('#editCellDialog div[id="editCellDialogContainer"]').remove();
                $('#editCellDialog div.coral-FixedColumn').append(dialogContent);
                openDialog('#editCellDialog');
            },
            error: function (error) {
                foundationUi.alert('EToolbox Query Kit', 'Could not retrieve results' + (error.responseText ? ': ' + error.responseText : ''), 'error');
            },
            complete: function () {
                foundationUi.clearWait();
            }
        });
    });

    function openDialog(dialogSelector) {
        const dialog = document.querySelector(dialogSelector);
        dialog.center();
        dialog.show();
    }
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
