(function(window, $) {
    'use strict';

    function collectQueryParams($dialog) {
        var result = {};
        $dialog.find('input[name]').each(function(index, namedInput) {
            var $namedInput = $(namedInput);
            var value = $namedInput.val();
            if (!value) {
                return;
            }
            var paramName = $namedInput.attr('name');
            var paramValue = result[paramName];
            if (Array.isArray(paramValue)) {
                paramValue.push(value);
            } else if (paramValue) {
                paramValue = [paramValue];
                paramValue.push(value);
            } else {
                paramValue = value;
            }
            result[paramName] = paramValue;
        });
        return result;
    }

    function populateItemSources($dialog, $dsInput) {
        if ($dsInput.data('items-loaded') || !$dsInput.data('datasource-query')) {
            return;
        }

        var queryParams = collectQueryParams($dialog);
        queryParams.q_query = $dsInput.data('datasource-query');

        $.ajax({
            url: '/apps/etoolbox-query-kit/datasources/itemlist.json',
            dataType: 'json',
            data: queryParams,
            traditional: true,
        })
            .success(function(response) {
                $dsInput.data('items-loaded', true);
                $dsInput[0].items.clear();
                Array.isArray(response) && $.each(response, function (index, item) {
                    $dsInput[0].items.add({content: {textContent: item.text}, value: item.value});
                })
            });
    }

    function completeQueryDialog(name, el) {
        var $dialog = $(el).closest('coral-dialog');
        var queryString = $dialog.find('[data-query]').data('query');

        $dialog.remove();
        if (!queryString) {
            return;
        }
        var queryParams = collectQueryParams($dialog);
        queryParams.q_query = queryString;
        queryParams.q_format = "sql2";

        var foundationUi = $(window).adaptTo('foundation-ui');
        foundationUi.wait();

        $.ajax({
            method: 'POST',
            url: '/apps/etoolbox-query-kit/services/parse',
            data: queryParams,
            traditional: true
        })
            .success(function(response) {
                $('#query-result').text(JSON.stringify(response));
            })
            .always(function() {
                foundationUi.clearWait();
            });
    }

    $(document).on('coral-overlay:open', '.eqk-query-dialog', function(e) {
        var $dialog = $(e.target);
        $dialog.find('[data-datasource-query]').each(function(index, dsInput) {
            populateItemSources($dialog, $(dsInput));
        });
    });

    $(window).adaptTo("foundation-registry").register("foundation.collection.action.action", {
        name: "etoolbox.querykit.queryToEditor",
        handler: completeQueryDialog
    });


})(window, Granite.$);

