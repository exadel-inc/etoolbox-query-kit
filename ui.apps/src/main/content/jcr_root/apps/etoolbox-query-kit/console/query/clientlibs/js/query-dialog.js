(function(window, ns, $) {
    'use strict';

    const DATASOURCE_ENDPOINT = '/apps/etoolbox-query-kit/datasources/itemlist.json';
    const PARSE_ENDPOINT = '/apps/etoolbox-query-kit/services/parse';

    const registry = $(window).adaptTo("foundation-registry");
    const foundationUi = $(window).adaptTo('foundation-ui');

    function collectQueryParams($dialog) {
        const result = {};
        $dialog.find('input[name]').each(function(index, namedInput) {
            const $namedInput = $(namedInput);
            const value = $namedInput.val();
            if (!value) {
                return;
            }
            let paramName = $namedInput.attr('name');
            let paramValue = result[paramName];
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

        const queryParams = collectQueryParams($dialog);
        queryParams['-query'] = $dsInput.data('datasource-query');

        $.ajax({
            url: DATASOURCE_ENDPOINT,
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

    function openQueryDialog(name, el, config, collection, selections) {
        const action = registry.get('foundation.collection.action.action').filter(function(action) {
            return action.name === 'foundation.dialog';
        })[0];
        if (!action) {
            return;
        }
        const profile = ns.DataStore.getProfileName();
        const url = new URL(config.data.src, window.location);
        url.searchParams.set('profile', profile);
        const newConfig = {
            data: {
                nesting: 'hide',
                src: url.pathname + url.search
            }
        };
        config.data.src = config.data.src + '&profile=' + profile;
        action.handler.call(this, 'foundation.dialog', el, newConfig, collection, selections);
    }

    function completeQueryDialog(name, el) {
        const $dialog = $(el).closest('coral-dialog');
        const queryString = $dialog.find('[data-query]').data('query');

        $dialog.remove();
        if (!queryString) {
            return;
        }
        const queryParams = collectQueryParams($dialog);
        queryParams['-query'] = queryString;

        foundationUi.wait();

        $.ajax({
            method: 'POST',
            url: PARSE_ENDPOINT,
            data: queryParams,
            traditional: true
        })
            .success(function(response) {
                const editor = $('.CodeMirror')[0].CodeMirror;
                editor.setValue(response);
            })
            .always(function() {
                foundationUi.clearWait();
            });
    }

    $(document).on('coral-overlay:open', '.eqk-dialog', function(e) {
        const $dialog = $(e.target);
        $dialog.find('[data-datasource-query]').each(function(index, dsInput) {
            populateItemSources($dialog, $(dsInput));
        });
    });

    registry.register("foundation.collection.action.action", {
        name: "eqk.query.dialog",
        handler: openQueryDialog
    });

    registry.register("foundation.collection.action.action", {
        name: "eqk.query.toEditor",
        handler: completeQueryDialog
    });

    registry.register("foundation.collection.action.action", {
        name: "eqk.query.toEditorAndRun",
        handler: completeQueryDialog
    });


})(window, Granite.Eqk = (Granite.Eqk || {}), Granite.$);

