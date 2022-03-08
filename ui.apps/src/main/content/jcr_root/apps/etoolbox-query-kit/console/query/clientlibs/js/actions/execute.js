(function (document, $, ns) {
    'use strict';

    const TABLE_URL = '/apps/etoolbox-query-kit/console/query/jcr:content/content/items/columns/items/results/items/table.html';
    const DEFAULT_LIMIT = 10;
    const NUM_VISIBLE_PAGES = 5;

    let currentPage = 1;
    let offset = 0;
    const limit = DEFAULT_LIMIT;

    let totalCount;
    let pageCount;

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
            data: {'-query': query, '-offset': offset, '-limit': limit, '-measure': !totalCount},
            beforeSend: function () {
                foundationUi.wait();
            },
            success: function (data) {
                $(document).trigger('query-kit:success-response', query);
                $('.navigation-button').removeAttr('disabled');
                if (currentPage === 1) {
                    $('.previous').prop('disabled', true);
                } else if (currentPage === pageCount) {
                    $('.next').prop('disabled', true);
                }

                $('.pagination').prop('hidden', false);
                const $table = $(data);
                offset = 0;
                totalCount = $table.attr('data-foundation-layout-table-guesstotal');
                pageCount = totalCount / limit;
                updateTable($table);
                updatePagination();
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
        $('#resultsColumn').prepend($table);
    }

    function updatePagination() {
        let htmlContent = '';
        if (pageCount <= NUM_VISIBLE_PAGES) {
            htmlContent += getPageRangeElement(1, pageCount);
        } else if (currentPage < NUM_VISIBLE_PAGES) {
            htmlContent += getPageRangeElement(1, NUM_VISIBLE_PAGES);
            getLastPageElement();
        } else if (currentPage > pageCount - NUM_VISIBLE_PAGES + 1) {
            htmlContent = getFirstPageElement() + getPageRangeElement(pageCount - NUM_VISIBLE_PAGES + 1, pageCount);
        } else {
            htmlContent += getFirstPageElement() + getPageRangeElement(currentPage - 1, currentPage + 1) + getLastPageElement();
        }
        $('#pagesContainer')[0].innerHTML = htmlContent;
    }

    function getFirstPageElement() {
        return `<button ${currentPage === 1 ? 'disabled' : ''} class='first-button'>1</button><i class='first'>...</i>`;
    }

    function getPageRangeElement(start, end) {
        let result = '';
        for (let i = start; i <= end; i++) {
            result += `<button class='nav-page-button' value='${i}' ${i === currentPage ? 'disabled' : ''}>${i}</button>`;
        }
        return result;
    }

    function getLastPageElement() {
        return `<i class='last'>...</i><button ${pageCount === currentPage ? 'disabled' : ''} class='last-button'>${pageCount}</button>`;
    }

    $(document).on('click', '.nav-page-button', function (e) {
        currentPage = e.target.value;
        const query = ns.getEditorValue();
        offset = (currentPage - 1) * limit;
        updateResult(query);
    });

    $('.nav-button').on('click', function (e) {
        currentPage = e.target.value;
        if ($(e.target).is('.next')) {
            offset = currentPage * limit;
            currentPage++;
        } else {
            currentPage--;
            offset = currentPage * limit;
        }
        const query = ns.getEditorValue();
        updateTable(query);
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
