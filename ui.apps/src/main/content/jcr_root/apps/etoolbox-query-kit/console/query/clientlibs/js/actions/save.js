(function (document, $, ns) {
    'use strict';

    const SAVED_QUERIES_KEY = 'eqk-saved-queries';
    const LATEST_QUERIES_KEY = 'eqk-latest-queries';

    const foundationUi = $(window).adaptTo('foundation-ui');

    let $selectedQuery = null;

    $(document).ready(function () {
        $(document).on('query-kit:success-response', saveLatestQueriesToLocalStorage);
        $('#saveQueryButton').on('click', saveSavedQueriesToLocalStorage);
    });

    function saveSavedQueriesToLocalStorage() {
        const query = ns.getEditorValue();
        const savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);

        if (savedQueries.includes(query)) {
            foundationUi.notify('', 'Query already exist in saved', 'error');
        } else if (query && query.trim().length > 0) {
            savedQueries.push(query);
            saveQueriesToLocalStorage(SAVED_QUERIES_KEY, savedQueries);
            foundationUi.notify('Query successfully saved');
        }
    }

    function saveLatestQueriesToLocalStorage() {
        const query = ns.getEditorValue();
        const latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);

        latestQueries.unshift(query);
        if (latestQueries.length > 10) {
            latestQueries.pop();
        }
        saveQueriesToLocalStorage(LATEST_QUERIES_KEY, latestQueries);
    }

    function saveQueriesToLocalStorage(key, queries) {
        localStorage.setItem(key, JSON.stringify(queries));
    }

    function getQueriesFromLocalStorage(key) {
        const storageItem = localStorage.getItem(key);
        return storageItem ? JSON.parse(storageItem) : [];
    }

    function deleteQueryInLocalStorage(key, index) {
        const queries = getQueriesFromLocalStorage(key);
        queries.length > 0 && queries.splice(index, 1);
        saveQueriesToLocalStorage(key, queries);
        const table = key === SAVED_QUERIES_KEY ? $('#savedQueriesTable tbody') : $('#lastQueriesTable tbody');
        populateTableValues(queries, table, key);
    }

    function populateTableValues(queries, table, key) {
        clearTable(table);
        queries && queries.length > 0 && queries.forEach(
            function (query, i) {
                table.append(`<tr is='coral-table-row'><td is='coral-table-cell' data-key=${key} data-index-number=${i}>${query}</td></tr>`);
            }
        );
    }

    function openQuery() {
        const dialog = $selectedQuery.closest('coral-dialog')[0];
        const $children = $selectedQuery.children();
        if (!$children) return;
        const value = $children.clone().children().remove().end().text();
        ns.setEditorValue(value);
        dialog.hide();
    }

    function deleteQuery() {
        const child = $selectedQuery.children();
        if (!child) return;
        const index = child.data('index-number');
        const key = child.data('key');
        deleteQueryInLocalStorage(key, index);
    }

    function shareQuery() {
        const query = $selectedQuery.find('td')[0].innerText;
        return function () {
            foundationUi.notify('URL copied to clipboard');
            const urlWithoutParams = window.location.origin + window.location.pathname;
            navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
        };
    }

    function clearTable(table) {
        table && table.empty();
    }

    function toggleActionBtnsDisabledState(value) {
        $('.delete-query-button').prop('disabled', value);
        $('.open-query-button').prop('disabled', value);
        $('.share-query-button').prop('disabled', value);
    }

    $(document).on('coral-overlay:beforeopen', '#querySavedDialog', function () {
        $selectedQuery = null;
        const savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);
        populateTableValues(savedQueries, $('#savedQueriesTable tbody'), SAVED_QUERIES_KEY);
    });

    $(document).on('coral-overlay:beforeopen', '#querySuccessfulDialog', function () {
        $selectedQuery = null;
        const latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);
        populateTableValues(latestQueries, $('#lastQueriesTable tbody'), LATEST_QUERIES_KEY);
    });

    $(document).on('coral-table:change', '#savedQueriesTable, #lastQueriesTable', function (e) {
        const selectedItem = e.target.selectedItem;
        if (selectedItem) {
            toggleActionBtnsDisabledState(false);
            $selectedQuery = $(selectedItem);
        } else {
            toggleActionBtnsDisabledState(true);
        }
    });

    // TODO this is Open button
    // TODO To Granite Foundation actions
    $(document).on('click', '.open-query-button', openQuery);

    // TODO second action
    $(document).on('click', '.delete-query-button', deleteQuery);

    // TODO third action
    $(document).on('click', '.share-query-button', shareQuery);
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
