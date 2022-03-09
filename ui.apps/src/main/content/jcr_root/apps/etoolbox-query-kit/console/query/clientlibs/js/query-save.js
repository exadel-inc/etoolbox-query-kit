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
(function (document, $) {
    'use strict';

    const SAVED_QUERIES_KEY = 'eqk-saved-queries';
    const LATEST_QUERIES_KEY = 'eqk-latest-queries';
    const foundationUi = $(window).adaptTo('foundation-ui');

    let executeAction = null;
    let deleteAction = null;
    let shareAction = null;

    function saveSavedQueriesToLocalStorage() {
        const queryValue = getQueryValue();
        const savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);

        if (savedQueries.indexOf(queryValue) !== -1) {
            foundationUi.notify('', 'Query has already been saved', 'notice');
        } else if (queryValue && queryValue.trim().length > 0) {
            savedQueries.push(queryValue);
            saveQueriesToLocalStorage(SAVED_QUERIES_KEY, savedQueries);
            foundationUi.notify('Query saved');
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

    function shareCallBack(selectedItem) {
        var  query = selectedItem.querySelector('td').innerText;
        return function () {
            foundationUi.notify('URL copied to clipboard');
            const urlWithoutParams = window.location.origin + window.location.pathname;
            navigator.clipboard.writeText(urlWithoutParams + '?query=' + encodeURIComponent(query));
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
        $('.delete-query-button').prop('disabled', value);
        $('.execute-query-button').prop('disabled', value);
        $('.share-query-button').prop('disabled', value);
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
            shareAction = shareCallBack(selectedItem);
        } else {
            toggleActionButtonsState(true);
        }
    });

    $(document).on('coral-overlay:close', '#querySavedDialog, #querySuccessfulDialog', function () {
        clearTable($('#tableSavedQueries tbody'));
        clearTable($('#tableLastQueries tbody'));
        executeAction = null;
        deleteAction = null;
        shareAction = null;
        toggleActionButtonsState(true);
    });

    $(document).on('query-kit:success-response', function () {
        saveLatestQueriesToLocalStorage();
    });

    $(document).on('click', '#btnSave', function () {
        saveSavedQueriesToLocalStorage();
    });

    $(document).on('click', '.execute-query-button', function () {
        executeAction && executeAction();
    });

    $(document).on('click', '.delete-query-button', function () {
        deleteAction && deleteAction();
    });

    $(document).on('click', '.share-query-button', function () {
        shareAction && shareAction();
    });
})(document, Granite.$);