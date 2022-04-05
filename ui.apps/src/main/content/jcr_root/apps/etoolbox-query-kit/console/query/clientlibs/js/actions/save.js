(function (document, $, ns) {
    'use strict';

    const registry = $(window).adaptTo('foundation-registry');

    let $selectedQuery = null;

    function saveToFavorites() {
        const query = ns.getEditorValue();
        const favoriteQueries = ns.DataStore.getFavoriteQueries();

        if (query && query.trim().length > 0) {
            if (!favoriteQueries.includes(query)) {
                favoriteQueries.push(query);
                ns.DataStore.setFavoriteQueries(favoriteQueries);
            }
            ns.notify('Query saved');
        }
    }

    function saveLatestQueries() {
        const query = ns.getEditorValue();
        const latestQueries = ns.DataStore.getLatestQueries();
        let duplicateIndex = latestQueries.indexOf(query);
        if (duplicateIndex !== -1) {
            latestQueries.splice(duplicateIndex, 1);
        }
        latestQueries.unshift(query);
        if (latestQueries.length > 10) {
            latestQueries.pop();
        }
        ns.DataStore.setLatestQueries(latestQueries);
    }

    function openQuery() {
        const dialog = $selectedQuery.closest('coral-dialog')[0];
        const $children = $selectedQuery.children();
        if (!$children) return;
        const value = $children.clone().children().remove().end().text();
        ns.setEditorValue(value);
        dialog.hide();
    }

    function shareQuery() {
        const query = $selectedQuery.find('td')[0].innerText;
        ns.notify('URL copied to clipboard');
        const urlWithoutParams = window.location.origin + window.location.pathname;
        navigator.clipboard.writeText(urlWithoutParams + '?-query=' + encodeURIComponent(query));
    }

    function deleteQuery() {
        const child = $selectedQuery.children();
        if (!child) return;
        const index = child.data('index-number');
        const key = child.data('key');
        deleteInLocalStorage(key, index);
    }

    function deleteInLocalStorage(key, index) {
        const queries = key === ns.DataStore.FAVORITE_QUERIES ? ns.DataStore.getFavoriteQueries() : ns.DataStore.getLatestQueries();
        const table = key === ns.DataStore.FAVORITE_QUERIES ? $('#favoriteQueriesTable tbody') : $('#latestQueriesTable tbody');
        queries.length > 0 && queries.splice(index, 1);
        if (key === ns.DataStore.FAVORITE_QUERIES) {
            ns.DataStore.setFavoriteQueries(queries);
        } else {
            ns.DataStore.setLatestQueries(queries);
        }
        updateQueriesTable(queries, table, key);
    }

    function updateQueriesTable(queries, table, key) {
        if (!table) {
            return;
        }
        table.empty();
        queries && queries.length > 0 && queries.forEach(
            function (query, i) {
                table.append(`<tr is='coral-table-row'><td is='coral-table-cell' data-key=${key} data-index-number=${i}>${query}</td></tr>`);
            }
        );
    }

    function toggleActionButtonsDisabledState(value) {
        $('.delete-query-button').prop('disabled', value);
        $('.open-query-button').prop('disabled', value);
        $('.share-query-button').prop('disabled', value);
    }

    /* --------------
       Event handlers
       -------------- */

    $(document).ready(function () {
        $(document).on('eqk-success-response', saveLatestQueries);
    });

    $(document).on('coral-overlay:beforeopen', '#favoriteQueriesDialog', function () {
        $selectedQuery = null;
        const favoriteQueries = ns.DataStore.getFavoriteQueries();
        updateQueriesTable(favoriteQueries, $('#favoriteQueriesTable tbody'), ns.DataStore.FAVORITE_QUERIES);
    });

    $(document).on('coral-overlay:beforeopen', '#latestQueriesDialog', function () {
        $selectedQuery = null;
        const latestQueries = ns.DataStore.getLatestQueries();
        updateQueriesTable(latestQueries, $('#latestQueriesTable tbody'), ns.DataStore.LATEST_QUERIES);
    });

    $(document).on('coral-table:change', '#favoriteQueriesTable, #latestQueriesTable', function (e) {
        const selectedItem = e.target.selectedItem;
        if (selectedItem) {
            toggleActionButtonsDisabledState(false);
            $selectedQuery = $(selectedItem);
        } else {
            toggleActionButtonsDisabledState(true);
        }
    });

    /* --------------------
       Actions registration
       -------------------- */

    registry.register('foundation.collection.action.action', {
        name: 'eqk.query.save',
        handler: saveToFavorites
    });

    // TODO this is Open button
    // TODO To Granite Foundation actions
    $(document).on('click', '.open-query-button', openQuery);

    // TODO second action
    $(document).on('click', '.delete-query-button', deleteQuery);

    // TODO third action
    $(document).on('click', '.share-query-button', shareQuery);
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
