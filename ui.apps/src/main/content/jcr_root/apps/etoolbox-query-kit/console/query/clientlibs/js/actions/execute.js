(function (document, $, ns) {
    'use strict';

    const TABLE_URL = '/apps/etoolbox-query-kit/components/console/tableHost/jcr:content/data.html';

    const DEFAULT_LIMIT = 2;

    const foundationUi = $(window).adaptTo('foundation-ui');
    const $executeButton = $('#btnExecute');
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
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
