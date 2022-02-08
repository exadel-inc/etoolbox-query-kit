(function(document, $) {
    "use strict";

    var SAVED_QUERIES_KEY = 'saved_queries',
        LATEST_QUERIES_KEY = 'latest_queries',
        executeAction = null,
        deleteAction = null;

    function saveSavedQueriesToLocalStorage() {
        var queryValue = getQueryValue(),
            savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);

        if (queryValue && queryValue.trim().length > 0) {
            savedQueries.push(queryValue);
            saveQueriesToLocalStorage(SAVED_QUERIES_KEY, savedQueries);
        }
    }

    function saveLatestQueriesToLocalStorage() {
        var queryValue = getQueryValue(),
            latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);

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
      var storageItem = localStorage.getItem(key);
      return storageItem ? JSON.parse(storageItem) : [];
    }

    function deleteQueryInLocalStorage(key, index) {
        var queries = getQueriesFromLocalStorage(key);
        queries.length > 0 && queries.splice(index, 1);
        saveQueriesToLocalStorage(key, queries);
        var table = key === 'saved_queries' ? $("#savedQueriesTable tbody") : $("#latestQueriesTable tbody");
        populateTableValues(queries, table, key);
    }

    function getQueryValue() {
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        return editor.getValue();
    }

    function populateTableValues(queries, table, key) {
      clearTable(table);
      queries && queries.length > 0 && queries.forEach(
          function(query, i) {
            table.append(`<tr is='coral-table-row'><td is='coral-table-cell' data-key=${key} data-index-number=${i}>${query}</td></tr>`);
          }
      );
    }

    function executeCallBack(selectedItem) {
        var dialogId = selectedItem.closest('coral-dialog').id;
        return function(){
            var $children = $(selectedItem).children();
            if (!$children) return;
            var editor = document.querySelector('.CodeMirror').CodeMirror;
            var value = $children.clone().children().remove().end().text();
            editor.setValue(value);
            closeDialog(`#${dialogId}`);
        }
    }

    function deleteCallBack(selectedItem) {
        return function(){
            var child = $(selectedItem).children();
            if (!child) return;
            var index = child.data("index-number");
            var key = child.data("key");
            deleteQueryInLocalStorage(key, index);
        }
    }

    function clearTable(table) {
        table && table.empty();
    }

    function closeDialog(selector) {
        var dialog = document.querySelector(selector);
        dialog && dialog.hide();
    }

    function toggleActionButtonsState(value) {
       $(".deleteQueryButton").prop('disabled', value);
       $(".executeQueryButton").prop('disabled', value);
    }

    $(document).on("coral-overlay:beforeopen", "#querySavedDialog", function() {
        var savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);
        populateTableValues(savedQueries, $("#savedQueriesTable tbody"), SAVED_QUERIES_KEY);
    });

    $(document).on("coral-overlay:beforeopen", "#querySuccessfulDialog", function() {
        var latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);
        populateTableValues(latestQueries, $("#latestQueriesTable tbody"), LATEST_QUERIES_KEY);
    });

    $(document).on("coral-table:change ", function(e) {
        var selectedItem = e.target.selectedItem;
        if (selectedItem) {
            toggleActionButtonsState(false);
            executeAction = executeCallBack(selectedItem);
            deleteAction = deleteCallBack(selectedItem);
        } else {
            toggleActionButtonsState(true);
        }
    });

    $(document).on("coral-overlay:close", "#querySavedDialog, #querySuccessfulDialog", function() {
        clearTable($("#savedQueriesTable tbody"));
        clearTable($("#latestQueriesTable tbody"));
        executeAction = null;
        deleteAction = null;
        toggleActionButtonsState(true);
    });

    $(document).on("query-kit:success-response", function() {
        saveLatestQueriesToLocalStorage();
    });

    $(document).on("click", "#saveButton", function() {
        saveSavedQueriesToLocalStorage();
    });

    $(document).on("click", "#openSavedQueriesButton", function() {
        openDialog('#querySavedDialog')
    });

    $(document).on("click", "#openLatestSuccessfulQueriesButton", function() {
        openDialog('#querySuccessfulDialog')
    });

    function openDialog(dialogSelector) {
        var dialog = document.querySelector(dialogSelector);
        dialog.center();
        dialog.show();
    }

    $(document).on("click", ".executeQueryButton", function() {
        executeAction && executeAction();
    });

    $(document).on("click", ".deleteQueryButton", function() {
        deleteAction && deleteAction();
    });

})(document, Granite.$);