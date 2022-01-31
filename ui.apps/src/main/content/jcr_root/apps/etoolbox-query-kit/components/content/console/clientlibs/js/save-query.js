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
            animateSaveIcon();
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
        populateTables();
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
        return function(){
            var $children = $(selectedItem).children();
            if (!$children) return;
            var editor = document.querySelector('.CodeMirror').CodeMirror;
            var value = $children.clone().children().remove().end().text();
            editor.setValue(value);
            closeDialog();
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

    function closeDialog() {
        var dialog = document.querySelector('#querySavedDialog');
        dialog && dialog.hide();
    }

    function toggleActionButtonsState(value) {
       $("#deleteSavedQueryButton").prop('disabled', value);
       $("#executeSavedQueryButton").prop('disabled', value);
    }

    function animateSaveIcon() {
       var coralIcon = $('#saveButton coral-icon')[0],
           saveButton = $('#saveButton')[0];

       saveButton.setAttribute('icon', 'starFill');
       coralIcon.setAttribute('icon', 'starFill');
       coralIcon.classList.add('coral3-Icon--starFill');
       coralIcon.classList.remove('coral3-Icon--starStroke');

       setTimeout(function(){
            saveButton.setAttribute('icon', 'starStroke');
            coralIcon.setAttribute('icon', 'starStroke');
            coralIcon.classList.add('coral3-Icon--starStroke');
            coralIcon.classList.remove('coral3-Icon--starFill');
       }, 2000);
    }

    function populateTables() {
      var savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY),
          latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);

      populateTableValues(savedQueries, $("#savedQueriesTable tbody"), SAVED_QUERIES_KEY);
      populateTableValues(latestQueries, $("#latestQueriesTable tbody"), LATEST_QUERIES_KEY);
    }

    $(document).on("coral-overlay:beforeopen", "#querySavedDialog", function() {
        populateTables();
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

    $(document).on("coral-overlay:close", "#querySavedDialog", function() {
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

    $(document).on("click", "#savedQueriesButton", function() {
        var dialog = document.querySelector('#querySavedDialog');
        dialog.center();
        dialog.show();
    });

    $(document).on("click", ".saved-type-tab", function() {
        $('#savedTypeTabs').find(":selected").trigger("click");
    });

    $(document).on("click", "#executeSavedQueryButton", function() {
        executeAction && executeAction();
    });

    $(document).on("click", "#deleteSavedQueryButton", function() {
        deleteAction && deleteAction();
    });

})(document, Granite.$);