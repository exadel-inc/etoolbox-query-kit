(function(document, $) {
    "use strict";

    const SAVED_QUERIES_KEY = 'saved_queries',
          LATEST_QUERIES_KEY = 'latest_queries';

    var executeAction = null,
        deleteAction = null;

    function saveSavedQueriesToLocalStorage() {
        const queryObject = getQueryObject();
        const savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);
        savedQueries.push(queryObject);
        saveQueriesToLocalStorage(SAVED_QUERIES_KEY, savedQueries);

        $('#saveButton')[0].setAttribute('icon', 'starFill');
        var coralIcon =  $('#saveButton coral-icon')[0];
        coralIcon.setAttribute('icon', 'starFill');
        coralIcon.classList.add('coral3-Icon--starFill');
        coralIcon.classList.remove('coral3-Icon--starStroke');
    }

    function saveLatestQueriesToLocalStorage() {
        const queryObject = getQueryObject();
        const latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);
        latestQueries.unshift(queryObject);
        if (latestQueries.length > 10) {
            latestQueries.pop();
        }
        saveQueriesToLocalStorage(LATEST_QUERIES_KEY, latestQueries);
    }

    function saveQueriesToLocalStorage(key, queries){
        localStorage.setItem(key, JSON.stringify(queries));
    }

    function getQueriesFromLocalStorage(key){
      var storageItem = localStorage.getItem(key);
      return storageItem ? JSON.parse(storageItem) : [];
    }

    function deleteQueryInLocalStorage(key, index){
        var queries = getQueriesFromLocalStorage(key);
        queries.length > 0 && queries.splice(index, 1);
        saveQueriesToLocalStorage(key, queries);
        populateTables();
    }

    function getQueryObject(){
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        var $languageSelect = $('#languageSelect')[0];
        return {
            language: $languageSelect.selectedItem.value,
            query: editor.getValue()
        }
    }

    function populateTableValues(queries, table, key){
      clearTable(table);
      queries && queries.length > 0 && queries.forEach(
          function(queryObject, i) {
            table.append("<tr is='coral-table-row'>" +
                    "<td is='coral-table-cell'" +
                        "data-key='" + key +
                        "' data-index-number='" + i +
                        "'>" + queryObject.query + "</td>" +
                    "</tr>");
          }
      );
    }

    function executeCallBack(selectedItem){
        return function(){
            const $child = $(selectedItem).children();
            if (!$child) return;
            var editor = document.querySelector('.CodeMirror').CodeMirror;
            const value = $child.clone().children().remove().end().text();
            editor.setValue(value);
            closeDialog();
        }
    }

    function deleteCallBack(selectedItem){
        return function(){
            const child = $(selectedItem).children();
            if (!child) return;
            const index = child.data("index-number");
            const key = child.data("key");
            deleteQueryInLocalStorage(key, index);
        }
    }

    function clearTable(table){
        table && table.empty();
    }

    function closeDialog(){
        var dialog = document.querySelector('#querySavedDialog');
        dialog && dialog.hide();
    }

    function disableActionButtons(value){
       $("#deleteSavedQueryButton").prop('disabled', value);
       $("#executeSavedQueryButton").prop('disabled', value);
    }

    function populateTables(){
      const savedQueries = getQueriesFromLocalStorage(SAVED_QUERIES_KEY);
      const latestQueries = getQueriesFromLocalStorage(LATEST_QUERIES_KEY);
      populateTableValues(savedQueries, $("#savedQueriesTable tbody"), SAVED_QUERIES_KEY);
      populateTableValues(latestQueries, $("#latestQueriesTable tbody"), LATEST_QUERIES_KEY);
    }

    $(document).on("coral-overlay:beforeopen", "#querySavedDialog", function() {
        populateTables();
    });

    $(document).on("coral-table:change ", function(e){
        var selectedItem = e.target.selectedItem;
        if (selectedItem) {
            disableActionButtons(false);
            executeAction = executeCallBack(selectedItem);
            deleteAction = deleteCallBack(selectedItem);
        } else {
            disableActionButtons(true);
        }
    });

    $(document).on("coral-overlay:close", "#querySavedDialog", function() {
        clearTable($("#savedQueriesTable tbody"));
        clearTable($("#latestQueriesTable tbody"));
        executeAction = null;
        deleteAction = null;
        disableActionButtons(true);
    });

    $(document).on("query-kit:success-response", function(){
        saveLatestQueriesToLocalStorage();
    });

    $(document).on("click", "#saveButton", function(){
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