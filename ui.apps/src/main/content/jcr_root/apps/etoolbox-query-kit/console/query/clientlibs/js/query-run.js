$(function () {

    'use strict';

    const TABLE_URL = '/apps/etoolbox-query-kit/components/console/dataTable/jcr:content/data.html';

    const DEFAULT_LIMIT = 2;

    const foundationUi = $(window).adaptTo('foundation-ui');
    const $executeButton = $('#btnExecute');

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
            data: {'-query': query, '-offset': offset, '-pageSize': limit, '-measure': !totalCount},
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
});