(function (document, $, ns) {
    'use strict';

    const FAVORITE_QUERIES = 'eqk-favorite-queries';
    const LATEST_QUERIES = 'eqk-latest-queries';

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    let $selectedQuery = null;

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.saveQuery',
        handler: saveFavoriteQueries
    });

    $(document).ready(function () {
        $(document).on('query-kit:success-response', saveLatestQueries);
    });

    function saveFavoriteQueries() {
        const query = ns.getEditorValue();
        const favoriteQueries = ns.DataStore.getQueries(FAVORITE_QUERIES);

        if (favoriteQueries.includes(query)) {
            foundationUi.notify('', 'Query has already been saved', 'notice');
        } else if (query && query.trim().length > 0) {
            favoriteQueries.push(query);
            ns.DataStore.setQueries(FAVORITE_QUERIES, favoriteQueries);
            foundationUi.notify('Query saved');
        }
    }

    function saveLatestQueries() {
        const query = ns.getEditorValue();
        const latestQueries = ns.DataStore.getQueries(LATEST_QUERIES);

        latestQueries.unshift(query);
        if (latestQueries.length > 10) {
            latestQueries.pop();
        }
        ns.DataStore.setQueries(LATEST_QUERIES, latestQueries);
    }

    function deleteQueryInLocalStorage(key, index) {
        const queries = ns.DataStore.getQueries(key);
        queries.length > 0 && queries.splice(index, 1);
        ns.DataStore.setQueries(key, queries);
        const table = key === FAVORITE_QUERIES ? $('#favoriteQueriesTable tbody') : $('#latestQueriesTable tbody');
        populateTable(queries, table, key);
    }

    function populateTable(queries, table, key) {
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
        foundationUi.notify('URL copied to clipboard');
        const urlWithoutParams = window.location.origin + window.location.pathname;
        navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
    }

    function clearTable(table) {
        table && table.empty();
    }

    function toggleActionBtnsDisabledState(value) {
        $('.delete-query-button').prop('disabled', value);
        $('.open-query-button').prop('disabled', value);
        $('.share-query-button').prop('disabled', value);
    }

    $(document).on('coral-overlay:beforeopen', '#favoriteQueriesDialog', function () {
        $selectedQuery = null;
        const favoriteQueries = ns.DataStore.getQueries(FAVORITE_QUERIES);
        populateTable(favoriteQueries, $('#favoriteQueriesTable tbody'), FAVORITE_QUERIES);
    });

    $(document).on('coral-overlay:beforeopen', '#latestQueriesDialog', function () {
        $selectedQuery = null;
        const latestQueries = ns.DataStore.getQueries(LATEST_QUERIES);
        populateTable(latestQueries, $('#latestQueriesTable tbody'), LATEST_QUERIES);
    });

    $(document).on('coral-table:change', '#favoriteQueriesTable, #latestQueriesTable', function (e) {
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
