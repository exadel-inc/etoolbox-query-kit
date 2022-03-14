(function (ns, $) {
    'use strict';

    $(document).ready(function () {
        const registry = $(window).adaptTo('foundation-registry');

        /** Retrieves a registered action by name and then runs it with the provided content and parameters */
        ns.runAction = function (name, context, el, config, collection, selections) {
            const target = registry.get('foundation.collection.action.action').find(function (action) {
                return action.name === name;
            });
            if (!target) {
                return;
            }
            target.handler.call(context, name, el, config, collection, selections);
        };
    });
})(Granite.Eqk = (Granite.Eqk || {}), Granite.$);
