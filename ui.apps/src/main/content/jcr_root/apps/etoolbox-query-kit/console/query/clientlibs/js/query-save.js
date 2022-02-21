(function (document, $) {
    'use strict';

    const SAVED_QUERIES_KEY = 'eqk-saved-queries';
    const LATEST_QUERIES_KEY = 'eqk-latest-queries';

    let executeAction = null;
    let deleteAction = null;

    function saveSavedQueriesToLocalStorage() {
        const queryValue = getQueryValue();
        const savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);

        if (queryValue && queryValue.trim().length > 0) {
            savedQueries.push(queryValue);
            saveQueriesToLocalStorage(SAVED_QUERIES_KEY, savedQueries);
        }
    }

    function saveLatestQueriesToLocalStorage() {
        const queryValue = getQueryValue();
        const latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);

        latestQueries.unshift(queryValue);
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
        const table = key === SAVED_QUERIES_KEY ? $('#tableSavedQueries tbody') : $('#tableLastQueries tbody');
        populateTableValues(queries, table, key);
    }

    function getQueryValue() {
        const editor = document.querySelector('.CodeMirror').CodeMirror;
        return editor.getValue();
    }

    function populateTableValues(queries, table, key) {
        clearTable(table);
        queries && queries.length > 0 && queries.forEach(
            function (query, i) {
                table.append(`<tr is='coral-table-row'><td is='coral-table-cell' data-key=${key} data-index-number=${i}>${query}</td></tr>`);
            }
        );
    }

    function executeCallBack(selectedItem) {
        const dialogId = selectedItem.closest('coral-dialog').id;
        return function () {
            const $children = $(selectedItem).children();
            if (!$children) return;
            const editor = document.querySelector('.CodeMirror').CodeMirror;
            const value = $children.clone().children().remove().end().text();
            editor.setValue(value);
            closeDialog(`#${dialogId}`);
        };
    }

    function deleteCallBack(selectedItem) {
        return function () {
            const child = $(selectedItem).children();
            if (!child) return;
            const index = child.data('index-number');
            const key = child.data('key');
            deleteQueryInLocalStorage(key, index);
        };
    }

    function clearTable(table) {
        table && table.empty();
    }

    function closeDialog(selector) {
        const dialog = document.querySelector(selector);
        dialog && dialog.hide();
    }

    function toggleActionButtonsState(value) {
        $('.deleteQueryButton').prop('disabled', value);
        $('.executeQueryButton').prop('disabled', value);
    }

    $(document).on('coral-overlay:beforeopen', '#querySavedDialog', function () {
        const savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);
        populateTableValues(savedQueries, $('#tableSavedQueries tbody'), SAVED_QUERIES_KEY);
    });

    $(document).on('coral-overlay:beforeopen', '#querySuccessfulDialog', function () {
        const latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);
        populateTableValues(latestQueries, $('#tableLastQueries tbody'), LATEST_QUERIES_KEY);
    });

    $(document).on('coral-table:change', '#tableSavedQueries, #tableLastQueries', function (e) {
        const selectedItem = e.target.selectedItem;
        if (selectedItem) {
            toggleActionButtonsState(false);
            executeAction = executeCallBack(selectedItem);
            deleteAction = deleteCallBack(selectedItem);
        } else {
            toggleActionButtonsState(true);
        }
    });

    $(document).on('coral-overlay:close', '#querySavedDialog, #querySuccessfulDialog', function () {
        clearTable($('#tableSavedQueries tbody'));
        clearTable($('#tableLastQueries tbody'));
        executeAction = null;
        deleteAction = null;
        toggleActionButtonsState(true);
    });

    $(document).on('query-kit:success-response', function () {
        saveLatestQueriesToLocalStorage();
    });

    $(document).on('click', '#btnSave', function () {
        saveSavedQueriesToLocalStorage();
    });

    function openDialog(dialogSelector) {
        const dialog = document.querySelector(dialogSelector);
        dialog.center();
        dialog.show();
    }

    $(document).on('click', '.executeQueryButton', function () {
        executeAction && executeAction();
    });

    $(document).on('click', '.deleteQueryButton', function () {
        deleteAction && deleteAction();
    });
})(document, Granite.$);