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
$(function () {

    'use strict';

    const TABLE_URL = '/apps/etoolbox-query-kit/components/console/tableHost/jcr:content/data.html';

    const DEFAULT_LIMIT = 2;

    const foundationUi = $(window).adaptTo('foundation-ui');
    const $executeButton = $('#btnExecute');
    const $editRowForm = $('#editRowDialogForm');

    let currentPage = 1;
    let offset = 0;
    let limit = DEFAULT_LIMIT;

    let totalCount;

    function updateUrlParams(query) {
        if (query && query.trim().length > 0 && history.pushState) {
            const url = new URL(window.location);
            url.searchParams.set('-query', encodeURIComponent(query));
            url.searchParams.set('-measure', 'true');
            window.history.pushState({}, '', url);
        }
    }

    function updateResult(query) {
        $.ajax({
            url: TABLE_URL,
            type: "GET",
            data: {'-query': query, '-offset': offset, '-pageSize': limit, '-measure': !totalCount, '-typeaware':true},
            beforeSend: function(){
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
        })
    }

    function updateTable($table) {
        $('#tableResult').remove();
        $('.pagination').remove();
        $('#columnResult').prepend($table);
    }

    $executeButton.on("click", (function () {
        const query = $('.CodeMirror')[0].CodeMirror.getValue();
        updateUrlParams(query);
        currentPage = 1;
        totalCount = 0;
        offset = 0;
        updateResult(query);
    }));

    $(document).on('click', '.nav-page-button', function (e) {
        currentPage = e.target.value;
        const query = $('.CodeMirror')[0].CodeMirror.getValue();
        offset = (currentPage - 1) * limit;
        updateResult(query)
    });

    $(document).on('click', '.nav-button', function (e) {
        if ($(e.target).is('.next')) {
            offset = currentPage * limit;
            currentPage++;
        } else {
            currentPage--;
            offset = (currentPage - 1) * limit;
        }
        const query = $('.CodeMirror')[0].CodeMirror.getValue();
        updateResult(query)
    });

    $editRowForm.submit(function (e) {
        e.preventDefault();
        var form = $(this);
        var url = form.attr('action');
        var data = form.serialize();
        $.ajax({
            url: url,
            type: "POST",
            data: data,
            success: function (data) {
                console.log('success');
            },
            error: function (error) {
                console.log('LOL');
            }
        })
    });

    $(document).on("dblclick", ".result-table-cell", function(e) {
        const property = e.target.getAttribute('data-name')
        const type = e.target.getAttribute('data-type');
        const path = e.target.getAttribute('data-path');

        $.ajax({
            url: '/apps/etoolbox-query-kit/console/dialogs/editCell.html',
            type: 'GET',
            data: {'path': path, 'property': property, 'type': type},
            beforeSend: function(){
                foundationUi.wait();
            },
            success: function (data) {
                const action = $(data).find('input[name="path"]')[0].value;
                const dialogContent = $(data).find('div[id="editCellDialogContainer"]')
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
        })
    });

    function openDialog(dialogSelector) {
        const dialog = document.querySelector(dialogSelector);
        dialog.center();
        dialog.show();
    }
});